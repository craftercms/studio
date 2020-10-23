/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_UUID_FILENAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_STATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

public class SyncClusterTask {

    private static final Logger logger = LoggerFactory.getLogger(SyncClusterTask.class);

    private StudioConfiguration studioConfiguration;
    private ClusterDAO clusterDao;
    private ContentRepository contentRepository;
    private TextEncryptor encryptor;
    private SiteService siteService;
    private Deployer deployer;
    private ServicesConfig servicesConfig;
    private DeploymentService deploymentService;
    private EventService eventService;

    private final static Map<String, String> deletedSitesMap = new HashMap<String, String>();

    public SyncClusterTask(StudioConfiguration studioConfiguration, ClusterDAO clusterDao,
                           ContentRepository contentRepository, TextEncryptor encryptor, SiteService siteService,
                           Deployer deployer, ServicesConfig servicesConfig, DeploymentService deploymentService,
                           EventService eventService) {
        this.studioConfiguration = studioConfiguration;
        this.clusterDao = clusterDao;
        this.contentRepository = contentRepository;
        this.encryptor = encryptor;
        this.siteService = siteService;
        this.deployer = deployer;
        this.servicesConfig = servicesConfig;
        this.deploymentService = deploymentService;
        this.eventService = eventService;
    }

    public void execute() {
        logger.debug("Starting Cluster Sync");
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        if (registrationData != null && !registrationData.isEmpty()) {
            String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
            logger.debug("Cluster is configured.");
            List<ClusterMember> cm = clusterDao.getAllMembers();
            boolean memberRemoved =
                    !cm.stream().anyMatch(clusterMember -> {
                        return clusterMember.getLocalAddress().equals(localAddress);
                    });
            if (!memberRemoved) {
                logger.debug("Cluster members count " + cm.size());
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(CLUSTER_LOCAL_ADDRESS, localAddress);
                    params.put(CLUSTER_STATE, ClusterMember.State.ACTIVE.toString());
                    List<ClusterMember> clusterMembers = clusterDao.getOtherMembers(params);

                    // sync global repo
                    StudioNodeSyncGlobalRepoTask nodeGlobalRepoSyncTask =
                            new StudioNodeSyncGlobalRepoTask();
                    nodeGlobalRepoSyncTask.setClusterNodes(clusterMembers);
                    nodeGlobalRepoSyncTask.setContentRepository(contentRepository);
                    nodeGlobalRepoSyncTask.setStudioConfiguration(studioConfiguration);
                    nodeGlobalRepoSyncTask.setEncryptor(encryptor);
                    nodeGlobalRepoSyncTask.run();

                    cleanupDeletedSites();

                    Set<String> siteNames = siteService.getAllAvailableSites();


                    if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
                        int numActiveMembers = clusterDao.countActiveMembers(params);
                        logger.debug("Number of active cluster members: " + numActiveMembers);
                    }

                    if ((clusterMembers != null && clusterMembers.size() > 0) && (siteNames != null && siteNames.size() > 0)) {
                        for (String site : siteNames) {
                            SiteFeed siteFeed = siteService.getSite(site);
                            logger.debug("Creating task thread to sync cluster node for site " + site);

                            // sync sandbox
                            StudioNodeSyncSandboxTask nodeSandobxSyncTask = new StudioNodeSyncSandboxTask();
                            nodeSandobxSyncTask.setSiteId(site);
                            nodeSandobxSyncTask.setSiteUuid(siteFeed.getSiteUuid());
                            nodeSandobxSyncTask.setSearchEngine(siteFeed.getSearchEngine());
                            nodeSandobxSyncTask.setDeployer(deployer);
                            nodeSandobxSyncTask.setStudioConfiguration(studioConfiguration);
                            nodeSandobxSyncTask.setContentRepository(contentRepository);
                            nodeSandobxSyncTask.setSiteService(siteService);
                            nodeSandobxSyncTask.setServicesConfig(servicesConfig);
                            nodeSandobxSyncTask.setClusterNodes(clusterMembers);
                            nodeSandobxSyncTask.setDeploymentService(deploymentService);
                            nodeSandobxSyncTask.setEventService(eventService);
                            nodeSandobxSyncTask.setEncryptor(encryptor);
                            nodeSandobxSyncTask.setClusterDao(clusterDao);
                            nodeSandobxSyncTask.setLocalAddress(localAddress);
                            nodeSandobxSyncTask.run();

                            // sync published
                            StudioNodeSyncPublishedTask nodePublishedSyncTask = new StudioNodeSyncPublishedTask();
                            nodePublishedSyncTask.setSiteId(site);
                            nodePublishedSyncTask.setSiteUuid(siteFeed.getSiteUuid());
                            nodePublishedSyncTask.setSearchEngine(siteFeed.getSearchEngine());
                            nodePublishedSyncTask.setDeployer(deployer);
                            nodePublishedSyncTask.setStudioConfiguration(studioConfiguration);
                            nodePublishedSyncTask.setContentRepository(contentRepository);
                            nodePublishedSyncTask.setSiteService(siteService);
                            nodePublishedSyncTask.setServicesConfig(servicesConfig);
                            nodePublishedSyncTask.setClusterNodes(clusterMembers);
                            nodePublishedSyncTask.setDeploymentService(deploymentService);
                            nodePublishedSyncTask.setEncryptor(encryptor);
                            nodePublishedSyncTask.run();

                        }
                    }

                } catch (Exception err) {
                    logger.error("Error while executing cluster sync job", err);
                }
            }
        }
    }

    private HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
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
                StudioNodeSyncSandboxTask.createdSites.remove(siteFeed.getSiteId());
                StudioNodeSyncSandboxTask.remotesMap.remove(siteFeed.getSiteId());
                StudioNodeSyncPublishedTask.createdSites.remove(siteFeed.getSiteId());
                StudioNodeSyncPublishedTask.remotesMap.remove(siteFeed.getSiteId());
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
            logger.info("Invalid site UUID. Local copy will not be deleted");
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
