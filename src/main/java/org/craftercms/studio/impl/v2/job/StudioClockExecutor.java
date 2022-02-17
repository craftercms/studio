/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.job;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.job.Job;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.job.SiteJob;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.spring.context.SystemStatusProvider;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_UUID_FILENAME;
import static org.craftercms.studio.api.v1.constant.StudioConstants.STUDIO_CLOCK_EXECUTOR_SITE_LOCK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

public class StudioClockExecutor implements Job {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockExecutor.class);
    private static final ReentrantLock singleWorkerLock = new ReentrantLock();

    private final static Map<String, String> deletedSitesMap = new HashMap<String, String>();

    private static boolean running = false;

    private SystemStatusProvider systemStatusProvider;

    public synchronized static boolean isRunning() {
        return running;
    }

    public synchronized static void setRunning(boolean isRunning) {
        running = isRunning;
    }

    private StudioConfiguration studioConfiguration;
    private TaskExecutor taskExecutor;
    private SiteService siteService;
    private org.craftercms.studio.api.v1.repository.ContentRepository contentRepositoryV1;
    private Deployer deployer;
    private GeneralLockService generalLockService;
    private List<Job> globalTasks;
    private List<SiteJob> siteTasks;
    private ContentRepository contentRepositoryV2;
    private static int threadCounter = 0;

    @Override
    public void execute() {
        threadCounter++;
        if (systemStatusProvider.isSystemReady()) {
            if (singleWorkerLock.tryLock()) {
                try {
                    setRunning(true);
                    logger.debug("Executing tasks thread num " + threadCounter);
                    executeTasks();
                } catch (Exception e) {
                    logger.error("Error executing Studio Clock Job", e);
                } finally {
                    setRunning(false);
                    singleWorkerLock.unlock();
                }
            }
        } else {
            logger.debug("System not ready yet. Skipping cycle");
        }
    }

    private void executeTasks() {
        for (Job job : globalTasks) {
            job.execute();
        }

        cleanupDeletedSites();

        List<String> sites = siteService.getAllCreatedSites();
        for (String site : sites) {
            taskExecutor.execute(() -> {
                String tasksLock = STUDIO_CLOCK_EXECUTOR_SITE_LOCK.replaceAll(PATTERN_SITE, site);
                if (generalLockService.tryLock(tasksLock)) {
                    try {
                        for (SiteJob siteTask : siteTasks) {
                            siteTask.execute(site);
                        }
                    } finally {
                        generalLockService.unlock(tasksLock);
                    }
                }
            });
        }
    }

    private void cleanupDeletedSites() {
        logger.debug("Remove local copies of deleted sites if present");
        List<SiteFeed> deletedSites = siteService.getDeletedSites();
        deletedSites.forEach(siteFeed -> {
            String key = siteFeed.getSiteId() + ":" + siteFeed.getSiteUuid();
            if (!deletedSitesMap.containsKey(key)) {
                if (contentRepositoryV2.repositoryExists(siteFeed.getName()) &&
                        checkSiteUuid(siteFeed.getSiteId(), siteFeed.getSiteUuid())) {
                    deployer.deleteTargets(siteFeed.getName());
                    destroySitePreviewContext(siteFeed.getName());
                    contentRepositoryV1.deleteSite(siteFeed.getName());
                }
                deletedSitesMap.put(key, siteFeed.getName());
            }
        });
    }

    private boolean checkSiteUuid(String siteId, String siteUuid) {
        boolean toRet = false;
        try {
            Path path = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                    studioConfiguration.getProperty(SITES_REPOS_PATH), siteId, SITE_UUID_FILENAME);
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (!StringUtils.startsWith(line, "#") && StringUtils.equals(line, siteUuid)) {
                    toRet = true;
                    break;
                }
            }
        } catch (IOException e) {
            logger.info("Invalid site UUID for site " + siteId + ". Local copy will not be deleted");
        }
        return toRet;
    }

    private boolean destroySitePreviewContext(String site) {
        boolean toReturn = true;
        String requestUrl = getDestroySitePreviewContextUrl(site);

        HttpGet getRequest = new HttpGet(requestUrl);
        RequestConfig requestConfig = RequestConfig.custom().setExpectContinueEnabled(true).build();
        getRequest.setConfig(requestConfig);

        CloseableHttpClient client = HttpClientBuilder.create().build();
        try {
            CloseableHttpResponse response = client.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                toReturn = false;
            }
        } catch (IOException e) {
            logger.error("Error while sending destroy preview context request for site " + site, e);
            toReturn = false;
        } finally {
            getRequest.releaseConnection();
        }
        return toReturn;
    }

    private String getDestroySitePreviewContextUrl(String site) {
        String url = studioConfiguration.getProperty(CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL);
        url = url.replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, site);
        return url;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public org.craftercms.studio.api.v1.repository.ContentRepository getContentRepositoryV1() {
        return contentRepositoryV1;
    }

    public void setContentRepositoryV1(org.craftercms.studio.api.v1.repository.ContentRepository contentRepositoryV1) {
        this.contentRepositoryV1 = contentRepositoryV1;
    }

    public Deployer getDeployer() {
        return deployer;
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public List<Job> getGlobalTasks() {
        return globalTasks;
    }

    public void setGlobalTasks(List<Job> globalTasks) {
        this.globalTasks = globalTasks;
    }

    public List<SiteJob> getSiteTasks() {
        return siteTasks;
    }

    public void setSiteTasks(List<SiteJob> siteTasks) {
        this.siteTasks = siteTasks;
    }

    public void setSystemStatusProvider(SystemStatusProvider systemStatusProvider) {
        this.systemStatusProvider = systemStatusProvider;
    }

    public ContentRepository getContentRepositoryV2() {
        return contentRepositoryV2;
    }

    public void setContentRepositoryV2(ContentRepository contentRepositoryV2) {
        this.contentRepositoryV2 = contentRepositoryV2;
    }
}
