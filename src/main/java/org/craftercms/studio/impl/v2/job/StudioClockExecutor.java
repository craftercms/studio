/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.job.SiteJob;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_UUID_FILENAME;
import static org.craftercms.studio.api.v1.constant.StudioConstants.STUDIO_CLOCK_EXECUTOR_SITE_LOCK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

public class StudioClockExecutor implements Job {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockExecutor.class);
    private static final ReentrantLock singleWorkerLock = new ReentrantLock();
    private static final Map<String, ReentrantLock> singleWorkerSiteTasksLockMap = new HashMap<String, ReentrantLock>();

    private final static Map<String, String> deletedSitesMap = new HashMap<String, String>();

    private static boolean stopSignaled = false;
    private static boolean running = false;

    public static synchronized void signalToStop() {
        stopSignaled = true;
    }

    public static synchronized void signalToStart() {
        stopSignaled = false;
    }

    public synchronized static boolean isRunning() {
        return running;
    }

    public synchronized static void setRunning(boolean isRunning) {
        running = isRunning;
    }

    private StudioConfiguration studioConfiguration;
    private TaskExecutor taskExecutor;
    private SiteService siteService;
    private ContentRepository contentRepository;
    private Deployer deployer;
    private GeneralLockService generalLockService;
    private List<Job> globalTasks;
    private List<SiteJob> siteTasks;
    private static int threadCounter = 0;

    public StudioClockExecutor(StudioConfiguration studioConfiguration,
                               TaskExecutor taskExecutor,
                               SiteService siteService,
                               ContentRepository contentRepository,
                               Deployer deployer,
                               GeneralLockService generalLockService,
                               List<Job> globalTasks,
                               List<SiteJob> siteTasks) {

        this.studioConfiguration = studioConfiguration;
        this.taskExecutor = taskExecutor;
        this.siteService = siteService;
        this.contentRepository = contentRepository;
        this.deployer = deployer;
        this.generalLockService = generalLockService;
        this.globalTasks = globalTasks;
        this.siteTasks = siteTasks;
    }

    @Override
    public void execute() {
        threadCounter++;
        if (!stopSignaled) {
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
        }
    }

    private void executeTasks() {
        for (Job job : globalTasks) {
            job.execute();
        }

        cleanupDeletedSites();

        List<String> sites = siteService.getAllCreatedSites();
        for (String site : sites) {
            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {
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
                if (contentRepository.contentExists(siteFeed.getName(), FILE_SEPARATOR) &&
                        checkSiteUuid(siteFeed.getSiteId(), siteFeed.getSiteUuid())) {
                    deployer.deleteTargets(siteFeed.getName());
                    destroySitePreviewContext(siteFeed.getName());
                    contentRepository.deleteSite(siteFeed.getName());
                }
                StudioClusterSandboxRepoSyncTask.remotesMap.remove(siteFeed.getSiteId());
                StudioClusterPublishedRepoSyncTask.remotesMap.remove(siteFeed.getSiteId());
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
}
