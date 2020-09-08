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

package org.craftercms.studio.impl.v1.service.deployment.job;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_PUBLISHING_LOCK_OWNER_ID;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_UUID_FILENAME;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PUBLISHED;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_BUSY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_PUBLISHING;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_QUEUED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_READY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHING_SITE_LOCK_TTL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

public class PublisherTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PublisherTask.class);

    protected static final Map<String, ReentrantLock> singleWorkerLockMap = new HashMap<String, ReentrantLock>();

    private String site;
    private StudioConfiguration studioConfiguration;
    private SiteService siteService;
    private PublishingManager publishingManager;
    private ServicesConfig servicesConfig;
    private ContentRepository contentRepository;
    private NotificationService notificationService;
    private AuditServiceInternal auditServiceInternal;

    public PublisherTask(String site,
                         StudioConfiguration studioConfiguration,
                         SiteService siteService,
                         PublishingManager publishingManager,
                         ServicesConfig servicesConfig,
                         ContentRepository contentRepository,
                         NotificationService notificationService,
                         AuditServiceInternal auditServiceInternal) {
        this.site = site;
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
        this.publishingManager = publishingManager;
        this.servicesConfig = servicesConfig;
        this.contentRepository = contentRepository;
        this.notificationService = notificationService;
        this.auditServiceInternal = auditServiceInternal;
    }

    @Override
    public void run() {
        logger.debug("Running Publisher Task for site " + site);
        ReentrantLock singleWorkerLock = singleWorkerLockMap.get(site);
        if (singleWorkerLock == null) {
            singleWorkerLock = new ReentrantLock();
            singleWorkerLockMap.put(site, singleWorkerLock);
        }
        String env = null;
        if (singleWorkerLock.tryLock()) {
            try {
                try {
                    syncRepository(site);
                } catch (Exception e) {
                    logger.error("Failed to sync database from repository for site " + site, e);
                    siteService.enablePublishing(site, false);
                }

                // Check publishing lock status
                if (siteService.tryLockPublishingForSite(site, getLockOwnerId(), getLockTTL())) {
                    if (contentRepository.repositoryExists(site) && siteService.isPublishingEnabled(site)) {
                        if (!publishingManager.isPublishingBlocked(site)) {
                            try {
                                Set<String> environments = getAllPublishingEnvironments(site);
                                for (String environment : environments) {
                                    env = environment;
                                    logger.debug("Processing content ready for deployment for site \"{0}\"", site);
                                    List<PublishRequest> itemsToDeploy =
                                            publishingManager.getItemsReadyForDeployment(site, environment);
                                    while (CollectionUtils.isNotEmpty(itemsToDeploy)) {
                                        if (itemsToDeploy != null && itemsToDeploy.size() > 0) {
                                            publishingManager.markItemsProcessing(site, environment, itemsToDeploy);
                                            List<String> commitIds = itemsToDeploy.stream()
                                                    .map(PublishRequest::getCommitId)
                                                    .distinct().collect(Collectors.toList());

                                            boolean allCommitsPresent = true;
                                            for (String commit : commitIds) {
                                                boolean commitPresent = contentRepository.commitIdExists(site, commit);
                                                if (!commitPresent) {
                                                    logger.debug("Commit with ID: " + commit + " is not present in local repo" +
                                                            " for site " + site + ". Publisher task will skip this cycle.");
                                                    allCommitsPresent = false;
                                                }
                                            }

                                            if (allCommitsPresent) {
                                                logger.info("Starting publishing on environment " + environment +
                                                        " for site " + site);
                                                logger.debug("Site \"{0}\" has {1} items ready for deployment",
                                                        site, itemsToDeploy.size());

                                                doPublishing(itemsToDeploy, environment);
                                            } else {
                                                publishingManager.markItemsReady(site, environment, itemsToDeploy);
                                            }
                                        }
                                        siteService.updatePublishingLockHeartbeatForSite(site);
                                        itemsToDeploy =
                                                publishingManager.getItemsReadyForDeployment(site, environment);
                                    }
                                }
                            } catch (Exception err) {
                                logger.error("Error while executing deployment to environment store for site: "
                                        + site, err);
                                publishingManager.resetProcessingQueue(site, env);
                                notificationService.notifyDeploymentError(site, err);
                                logger.info("Continue executing deployment for other sites.");
                            }
                        } else {
                            logger.info("Publishing is blocked for site " + site);
                        }
                    } else {
                        logger.info("Publishing is disabled for site " + site);
                    }
                }
            } catch (Exception err) {
                logger.error("Error while executing deployment to environment store", err);
                notificationService.notifyDeploymentError(site, err);
                publishingManager.resetProcessingQueue(site, env);
            } finally {
                // Unlock publishing if queue does not have packages ready for publishing
                siteService.unlockPublishingForSite(site);
                singleWorkerLock.unlock();
            }
        }
    }

    private void syncRepository(String site) throws ServiceLayerException, UserNotFoundException {
        logger.debug("Getting last verified commit for site: " + site);
        SiteFeed siteFeed = siteService.getSite(site);
        if (checkSiteUuid(site, siteFeed.getSiteUuid())) {
            String lastProcessedCommit = siteFeed.getLastVerifiedGitlogCommitId();
            if (StringUtils.isNotEmpty(lastProcessedCommit)) {
                logger.debug("Syncing database with repository for site " + site + " from last processed commit "
                        + lastProcessedCommit);
                siteService.syncDatabaseWithRepo(site, lastProcessedCommit);
            }
        }
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

    private Set<String> getAllPublishingEnvironments(String site) {
        Set<String> environments = new HashSet<String>();
        environments.add(servicesConfig.getLiveEnvironment(site));
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            environments.add(servicesConfig.getStagingEnvironment(site));
        }
        return environments;
    }

    private void doPublishing(List<PublishRequest> itemsToDeploy, String environment) {
        try {
            String statusMessage = StringUtils.EMPTY;
            String author = itemsToDeploy.get(0).getUser();
            StringBuilder sbComment = new StringBuilder();
            List<DeploymentItemTO> completeDeploymentItemList = new ArrayList<DeploymentItemTO>();
            Set<String> processedPaths = new HashSet<String>();
            SimpleDateFormat sdf =
                    new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            String messagePath = StringUtils.EMPTY;
            String currentPackageId = StringUtils.EMPTY;
            try {
                logger.debug("Mark items as processing for site \"{0}\"", site);
                Set<String> packageIds = new HashSet<String>();
                for (PublishRequest item : itemsToDeploy) {
                    processPublishingRequest(site, environment, item,
                            completeDeploymentItemList, processedPaths);
                    if (!StringUtils.equals(currentPackageId, item.getPackageId())) {
                        currentPackageId = item.getPackageId();
                        statusMessage = studioConfiguration.getProperty
                                (JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_PUBLISHING);
                        statusMessage =
                                statusMessage.replace("{package_id}", currentPackageId)
                                        .replace("{datetime}", ZonedDateTime.now(ZoneOffset.UTC)
                                                .format(DateTimeFormatter.ofPattern(sdf.toPattern())));
                        siteService.updatePublishingStatusMessage(site, statusMessage);
                    }
                    if (packageIds.add(item.getPackageId())) {
                        sbComment.append(item.getSubmissionComment()).append("\n");
                    }
                }
                deploy(site, environment, completeDeploymentItemList, author,
                        sbComment.toString());
                StringBuilder sbPackIds = new StringBuilder("Package(s): ");
                for (String packageId : packageIds) {
                    sbPackIds.append(packageId).append(";");
                }
                generateWorkflowActivity(site, environment, packageIds,  author, OPERATION_PUBLISHED);
                publishingManager.markItemsCompleted(site, environment, itemsToDeploy);
                logger.debug("Mark deployment completed for processed items for site \"{0}\"", site);
                logger.info("Finished publishing environment " + environment + " for site " + site);

                if (publishingManager.isPublishingQueueEmpty(site)) {
                    statusMessage = studioConfiguration.getProperty
                            (JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_READY);
                    statusMessage = statusMessage.replace("{package_id}", currentPackageId)
                            .replace("{datetime}", ZonedDateTime.now(ZoneOffset.UTC)
                                    .format(DateTimeFormatter.ofPattern(sdf.toPattern())))
                            .replace("{package_size}",
                                    Integer.toString(itemsToDeploy.size()));
                } else {
                    statusMessage =
                            studioConfiguration.getProperty
                                    (JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_QUEUED);
                }
                siteService.updatePublishingStatusMessage(site, statusMessage);
            } catch (DeploymentException err) {
                logger.error("Error while executing deployment to environment store " +
                                "for site \"{0}\", number of items \"{1}\"", err, site,
                        itemsToDeploy.size());
                publishingManager.markItemsReady(site, environment, itemsToDeploy);
                siteService.enablePublishing(site, false);
                statusMessage = studioConfiguration.getProperty
                        (JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR);
                statusMessage = statusMessage.replace("{item_path}", messagePath)
                        .replace("{datetime}", ZonedDateTime.now(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ofPattern(sdf.toPattern())));
                siteService.updatePublishingStatusMessage(site, statusMessage);
                throw err;
            } catch (Exception err) {
                logger.error("Unexpected error while executing deployment to environment " +
                                "store for site \"{0}\", number of items \"{1}\"", err, site,
                        itemsToDeploy.size());
                publishingManager.markItemsReady(site, environment, itemsToDeploy);
                siteService.enablePublishing(site, false);
                statusMessage = studioConfiguration.getProperty
                        (JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR);
                statusMessage = statusMessage.replace("{item_path}", messagePath)
                        .replace("{datetime}", ZonedDateTime.now(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ofPattern(sdf.toPattern())));
                siteService.updatePublishingStatusMessage(site, statusMessage);
                throw err;
            }
        } catch (Exception err) {
            logger.error("Error while executing deployment to environment store for site: "
                    + site, err);
            notificationService.notifyDeploymentError(site, err);
            logger.info("Continue executing deployment for other sites.");
        }
    }

    private void processPublishingRequest(String site, String environment, PublishRequest item,
            List<DeploymentItemTO> completeDeploymentItemList,
            Set<String> processedPaths)
    throws ServiceLayerException, DeploymentException {
        List<DeploymentItemTO> missingDependencies = new ArrayList<DeploymentItemTO>();
        Set<String> missingDependenciesPaths = new HashSet<String>();
        SimpleDateFormat sdf = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
        String messagePath = item.getPath();
        String statusMessage = studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_BUSY);
        statusMessage = statusMessage.replace("{item_path}", messagePath).replace("{datetime}",
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(sdf.toPattern())));
        siteService.updatePublishingStatusMessage(site, statusMessage);
        try {
            List<DeploymentItemTO> deploymentItemList = new ArrayList<DeploymentItemTO>();


            logger.debug("Processing [{0}] content item for site \"{1}\"", item
                    .getPath(), site);
            DeploymentItemTO deploymentItem = publishingManager.processItem(item);
            if (deploymentItem != null) {
                deploymentItemList.add(deploymentItem);
            }
            logger.debug("Processing COMPLETE [{0}] content item for site \"{1}\"",
                    item.getPath(), site);

            if (isMandatoryDependenciesCheckEnabled()) {
                logger.debug("Processing Mandatory Deps [{0}] content item for site "
                        + "\"{1}\"", item.getPath(), site);
                missingDependencies.addAll(publishingManager
                        .processMandatoryDependencies(item, processedPaths, missingDependenciesPaths));
                logger.debug("Processing Mandatory Dependencies COMPLETE [{0}]"
                        + " content item for site \"{1}\"", item.getPath(), site);
            }
            deploymentItemList.addAll(missingDependencies);
            completeDeploymentItemList.addAll(deploymentItemList);
        } catch (DeploymentException err) {
            logger.error("Error while executing deployment to environment store for site \"{0}\",", err, site);
            publishingManager.markItemsReady(site, environment, Arrays.asList(item));
            siteService.enablePublishing(site, false);
            statusMessage = studioConfiguration.getProperty(
                    JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR);
            statusMessage = statusMessage.replace("{item_path}", messagePath).replace("{datetime}",
                    ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(sdf.toPattern())));
            siteService.updatePublishingStatusMessage(site, statusMessage);
            throw err;
        } catch (Exception err){
            logger.error("Unexpected error while executing deployment to environment " +
                    "store for site \"{0}\", ", err, site);
            publishingManager.markItemsReady(site, environment, Arrays.asList(item));
            siteService.enablePublishing(site, false);
            statusMessage = studioConfiguration.getProperty(
                    JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR);
            statusMessage = statusMessage.replace("{item_path}", messagePath).replace("{datetime}",
                    ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(sdf.toPattern())));
            siteService.updatePublishingStatusMessage(site, statusMessage);
            throw err;
        }
    }

    private void deploy(String site, String environment, List<DeploymentItemTO> items, String author, String comment)
            throws DeploymentException, SiteNotFoundException {
        logger.debug("Deploying " + items.size() + " item(s)");
        SiteFeed siteFeed = siteService.getSite(site);
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            String liveEnvironment = servicesConfig.getLiveEnvironment(site);
            if (StringUtils.equals(liveEnvironment, environment)) {
                String stagingEnvironment = servicesConfig.getStagingEnvironment(site);
                contentRepository.publish(site, siteFeed.getSandboxBranch(), items, stagingEnvironment, author, comment);
            }
        }
        contentRepository.publish(site, siteFeed.getSandboxBranch(), items, environment, author, comment);

    }

    protected void generateWorkflowActivity(String site, String environment, Set<String> packageIds, String username,
                                            String operation) throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(site);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(operation);
        auditLog.setActorId(username);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(site + ":" + environment);
        auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
        auditLog.setPrimaryTargetValue(environment);
        List<AuditLogParameter> auditLogParameters = new ArrayList<AuditLogParameter>();
        for (String packageId : packageIds) {
            AuditLogParameter auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(site + ":" + environment);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(packageId);
            auditLogParameters.add(auditLogParameter);
        }
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    public boolean isMandatoryDependenciesCheckEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(
                JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED));
        return toReturn;
    }
    private String getLockOwnerId() {
        HierarchicalConfiguration<ImmutableNode> clusterConfig =
                studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
        String clusterNodeId = StringUtils.EMPTY;
        if (Objects.nonNull(clusterConfig)) {
            clusterNodeId = clusterConfig.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
        }
        if  (StringUtils.isEmpty(clusterNodeId)) {
            try {
                clusterNodeId = InetAddress.getLocalHost().toString();
            } catch (UnknownHostException e) {
                clusterNodeId = DEFAULT_PUBLISHING_LOCK_OWNER_ID;
            }
        }
        return clusterNodeId;
    }

    private int getLockTTL() {
        return studioConfiguration.getProperty(PUBLISHING_SITE_LOCK_TTL, Integer.class);
    }

}
