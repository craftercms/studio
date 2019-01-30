/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.cluster;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.service.cluster.StudioClusterSyncJob;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_UUID_FILENAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SITES_REPOS_PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_STATE;

public class StudioClusterSyncJobImpl implements StudioClusterSyncJob {

    private final static Logger logger = LoggerFactory.getLogger(StudioClusterSyncJobImpl.class);

    private SiteService siteService;
    private TaskExecutor taskExecutor;
    private PreviewDeployer previewDeployer;
    private StudioConfiguration studioConfiguration;
    private ContentRepository contentRepository;
    private ClusterDAO clusterDAO;
    private ServicesConfig servicesConfig;
    private GitRepositories repositoryType;

    private ReentrantLock singleWorkerLock = new ReentrantLock();
    private final static Map<String, String> deletedSitesMap = new HashMap<String, String>();

    @Override
    public void run() {
        if (singleWorkerLock.tryLock()) {
            try {
                logger.debug("Starting Cluster Sync worker");
                HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
                if (registrationData != null && !registrationData.isEmpty()) {
                    String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
                    logger.debug("Cluster is configured.");
                    List<ClusterMember> cm = clusterDAO.getAllMembers();
                    boolean memberRemoved =
                            !cm.stream().anyMatch(clusterMember -> {
                                return clusterMember.getLocalAddress().equals(localAddress);
                            });
                    if (memberRemoved) {
                        logger.info("Cluster member " + localAddress + " is removed from cluster. Not syncing with other " +
                                "members");
                    } else {
                        logger.debug("Cluster members count " + cm.size());
                        try {
                            cleanupDeletedSites();

                            Set<String> siteNames = siteService.getAllAvailableSites();

                            Map<String, String> params = new HashMap<String, String>();
                            params.put(CLUSTER_LOCAL_ADDRESS, localAddress);
                            params.put(CLUSTER_STATE, ClusterMember.State.ACTIVE.toString());
                            List<ClusterMember> clusterMembers = clusterDAO.getOtherMembers(params);
                            if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
                                int numActiveMembers = clusterDAO.countActiveMembers(params);
                                logger.debug("Number of active cluster members: " + numActiveMembers);
                            }
                            if ((clusterMembers != null && clusterMembers.size() > 0) && (siteNames != null && siteNames.size() > 0)) {
                                for (String site : siteNames) {
                                    SiteFeed siteFeed = siteService.getSite(site);
                                    logger.debug("Creating task thread to sync cluster node for site " + site);
                                    switch (repositoryType) {
                                        case SANDBOX:
                                            StudioNodeSyncSandboxTask nodeSandobxSyncTask = new StudioNodeSyncSandboxTask();
                                            nodeSandobxSyncTask.setSiteId(site);
                                            nodeSandobxSyncTask.setSiteUuid(siteFeed.getSiteUuid());
                                            nodeSandobxSyncTask.setPreviewDeployer(previewDeployer);
                                            nodeSandobxSyncTask.setStudioConfiguration(studioConfiguration);
                                            nodeSandobxSyncTask.setContentRepository(contentRepository);
                                            nodeSandobxSyncTask.setSiteService(siteService);
                                            nodeSandobxSyncTask.setServicesConfig(servicesConfig);
                                            nodeSandobxSyncTask.setClusterNodes(clusterMembers);
                                            taskExecutor.execute(nodeSandobxSyncTask);
                                            break;
                                        case PUBLISHED:
                                            StudioNodeSyncPublishedTask nodePublishedSyncTask = new StudioNodeSyncPublishedTask();
                                            nodePublishedSyncTask.setSiteId(site);
                                            nodePublishedSyncTask.setSiteUuid(siteFeed.getSiteUuid());
                                            nodePublishedSyncTask.setPreviewDeployer(previewDeployer);
                                            nodePublishedSyncTask.setStudioConfiguration(studioConfiguration);
                                            nodePublishedSyncTask.setContentRepository(contentRepository);
                                            nodePublishedSyncTask.setSiteService(siteService);
                                            nodePublishedSyncTask.setServicesConfig(servicesConfig);
                                            nodePublishedSyncTask.setClusterNodes(clusterMembers);
                                            taskExecutor.execute(nodePublishedSyncTask);
                                    }
                                }
                            }
                        } catch (Exception err) {
                            logger.error("Error while executing cluster sync job", err);
                        }
                    }
                }
            } finally {
                singleWorkerLock.unlock();
            }
            logger.debug("Cluster Sync worker finished");
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
                    previewDeployer.deleteTarget(siteFeed.getName());
                    destroySitePreviewContext(siteFeed.getName());
                    contentRepository.deleteSite(siteFeed.getName());
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
            logger.info("Invaid site UUID. Local copy will not be deleted");
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

    private HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public PreviewDeployer getPreviewDeployer() {
        return previewDeployer;
    }

    public void setPreviewDeployer(PreviewDeployer previewDeployer) {
        this.previewDeployer = previewDeployer;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public ClusterDAO getClusterDAO() {
        return clusterDAO;
    }

    public void setClusterDAO(ClusterDAO clusterDAO) {
        this.clusterDAO = clusterDAO;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public GitRepositories getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(GitRepositories repositoryType) {
        this.repositoryType = repositoryType;
    }
}
