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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.event.publish.PublishEvent;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressObserver;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.cluster.StudioClusterUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.springframework.jdbc.UncategorizedSQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PUBLISHED;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_PUBLISHING_PACKAGE;
import static org.craftercms.studio.api.v2.dal.PublishStatus.ERROR;
import static org.craftercms.studio.api.v2.dal.PublishStatus.PROCESSING;
import static org.craftercms.studio.api.v2.dal.PublishStatus.PUBLISHING;
import static org.craftercms.studio.api.v2.dal.PublishStatus.QUEUED;
import static org.craftercms.studio.api.v2.dal.PublishStatus.READY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED;

public class StudioPublisherTask extends StudioClockTask implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(StudioPublisherTask.class);

    protected static final Map<String, Integer> retryCounter = new HashMap<String, Integer>();

    protected static final Set<String> dbErrorNotifiedSites = new HashSet<String>();

    private StudioConfiguration studioConfiguration;
    private SiteService siteService;
    private ContentRepository contentRepository;
    private PublishingManager publishingManager;
    private ServicesConfig servicesConfig;
    private NotificationService notificationService;
    private AuditServiceInternal auditServiceInternal;
    private int maxRetryCounter;
    private StudioClusterUtils studioClusterUtils;
    private PublishingProgressServiceInternal publishingProgressServiceInternal;
    private UserServiceInternal userServiceInternal;
    private ActivityStreamServiceInternal activityStreamServiceInternal;
    private ApplicationContext applicationContext;

    @Override
    protected void executeInternal(String siteId) {
        String siteState = siteService.getSiteState(siteId);
        if (!StringUtils.equals(siteState, STATE_READY)) {
            return;
        }
        String env = null;
        String lockOwnerId = studioClusterUtils.getLockOwnerId();
        int lockTTL = studioClusterUtils.getLockTTL();
        try {

            // Check publishing lock status
            logger.debug("Try to lock site " + siteId + " for publishing by lock owner " + lockOwnerId);
            if (siteService.tryLockPublishingForSite(siteId, lockOwnerId, lockTTL)) {

                if (contentRepository.repositoryExists(siteId) && siteService.isPublishingEnabled(siteId)) {
                    if (!publishingManager.isPublishingBlocked(siteId)) {
                        try {
                            if (!retryCounter.containsKey(siteId)) {
                                retryCounter.put(siteId, maxRetryCounter);
                            }
                            Set<String> environments = getAllPublishingEnvironments(siteId);
                            for (String environment : environments) {
                                env = environment;
                                logger.debug("Processing content ready for deployment for site \"{0}\"", siteId);
                                List<PublishRequest> itemsToDeploy =
                                        publishingManager.getItemsReadyForDeployment(siteId, environment);
                                while (CollectionUtils.isNotEmpty(itemsToDeploy)) {
                                    logger.debug("Deploying " + itemsToDeploy.size() + " items for " +
                                            "site " + siteId);
                                    publishingManager.markItemsProcessing(siteId, environment, itemsToDeploy);
                                    List<String> commitIds = itemsToDeploy.stream()
                                            .map(PublishRequest::getCommitId)
                                            .distinct().collect(Collectors.toList());

                                    boolean allCommitsPresent = true;
                                    StringBuilder sbMissingCommits = new StringBuilder();
                                    for (String commit : commitIds) {
                                        if (StringUtils.isNotEmpty(commit)) {
                                            boolean commitPresent = contentRepository.commitIdExists(siteId,
                                                    commit);
                                            if (!commitPresent) {
                                                sbMissingCommits.append(commit).append("; ");
                                                logger.debug("Commit with ID: " + commit + " is not present in " +
                                                        "local repo for site " + siteId + ". " +
                                                        "Publisher task will skip this cycle.");
                                                allCommitsPresent = false;
                                            }
                                        }
                                    }

                                    if (allCommitsPresent) {
                                        logger.info("Starting publishing on environment " + environment +
                                                " for site " + siteId);
                                        logger.debug("Site \"{0}\" has {1} items ready for deployment",
                                                siteId, itemsToDeploy.size());
                                        String packageId = itemsToDeploy.get(0).getPackageId();
                                        PublishingProgressObserver observer =
                                                new PublishingProgressObserver(siteId, packageId, environment,
                                                        itemsToDeploy.size());
                                        publishingProgressServiceInternal.addObserver(observer);
                                        doPublishing(siteId, itemsToDeploy, environment);
                                        applicationContext.publishEvent(new PublishEvent(siteId));
                                        retryCounter.remove(siteId);
                                        dbErrorNotifiedSites.remove(siteId);
                                        siteService.updatePublishingLockHeartbeatForSite(siteId);
                                        itemsToDeploy =
                                                publishingManager.getItemsReadyForDeployment(siteId, environment);
                                    } else {
                                        publishingManager.markItemsReady(siteId, environment, itemsToDeploy);
                                        int retriesLeft = retryCounter.get(siteId) - 1;
                                        itemsToDeploy = null;
                                        if (retriesLeft > 0) {
                                            retryCounter.put(siteId, retriesLeft);
                                            logger.info("Following commits are not present in local " +
                                                    "repository " + sbMissingCommits + " Publisher task " +
                                                    "will retry in next cycle. Number of retries left: " + retriesLeft);
                                        } else {
                                            retryCounter.remove(siteId);
                                            siteService.enablePublishing(siteId, false);
                                            throw new DeploymentException("Deployment failed after " + maxRetryCounter
                                                    + " retries. Following commits are not present in local " +
                                                    "repository " + sbMissingCommits);
                                        }
                                    }

                                }
                            }
                        } catch (UncategorizedSQLException  dbErr) {
                            logger.error("DB error while executing deployment to environment store for site "
                                            + siteId, dbErr);
                            if (!dbErrorNotifiedSites.add(siteId)) {
                                notificationService.notifyDeploymentError(siteId, dbErr);
                            }
                            publishingManager.resetProcessingQueue(siteId, env);
                        } catch (Exception err) {
                            logger.error("Error while executing deployment to environment store for site: "
                                    + siteId, err);
                            publishingManager.resetProcessingQueue(siteId, env);
                            notificationService.notifyDeploymentError(siteId, err);
                        }
                    } else {
                        logger.info("Publishing is blocked for site " + siteId);
                    }
                } else {
                    logger.debug("Publishing is disabled for site {0}", siteId);
                }
            }
        } catch (UncategorizedSQLException  dbErr) {
            logger.error("DB error while executing deployment to environment store for site " + siteId, dbErr);
            if (!dbErrorNotifiedSites.add(siteId)) {
                notificationService.notifyDeploymentError(siteId, dbErr);
            }
            publishingManager.resetProcessingQueue(siteId, env);
        } catch (Exception err) {
            logger.error("Error while executing deployment to environment store for site " + siteId, err);
            notificationService.notifyDeploymentError(siteId, err);
            publishingManager.resetProcessingQueue(siteId, env);
        } finally {
            // Unlock publishing if queue does not have packages ready for publishing
            publishingProgressServiceInternal.removeObserver(siteId);
            logger.debug("Unlocking publishing for site " + siteId + " by lock owner " + lockOwnerId);
            siteService.unlockPublishingForSite(siteId, lockOwnerId);
        }
    }

    private void doPublishing(String siteId, List<PublishRequest> itemsToDeploy, String environment)
            throws DeploymentException, ServiceLayerException, UserNotFoundException {
        siteService.updatePublishingStatus(siteId, PROCESSING);
        String status;
        String author = itemsToDeploy.get(0).getUser();
        StringBuilder sbComment = new StringBuilder();
        List<DeploymentItemTO> completeDeploymentItemList = new ArrayList<DeploymentItemTO>();
        Set<String> processedPaths = new HashSet<String>();
        String currentPackageId = StringUtils.EMPTY;
        try {
            logger.debug("Mark items as processing for site \"{0}\"", siteId);
            Set<String> packageIds = new HashSet<String>();
            for (PublishRequest item : itemsToDeploy) {
                processPublishingRequest(siteId, environment, item, completeDeploymentItemList, processedPaths);
                if (!StringUtils.equals(currentPackageId, item.getPackageId())) {
                    currentPackageId = item.getPackageId();
                    publishingProgressServiceInternal.updateObserver(siteId, currentPackageId);
                } else {
                    publishingProgressServiceInternal.updateObserver(siteId);
                }

                if (packageIds.add(item.getPackageId())) {
                    sbComment.append(item.getSubmissionComment()).append("\n");
                }
            }
            publishingProgressServiceInternal.removeObserver(siteId);
            siteService.updatePublishingStatus(siteId, PUBLISHING);
            String pkgId = completeDeploymentItemList.get(0).getPackageId();
            PublishingProgressObserver observer = new PublishingProgressObserver(siteId, pkgId, environment,
                    completeDeploymentItemList.size());
            publishingProgressServiceInternal.addObserver(observer);
            deploy(siteId, environment, completeDeploymentItemList, author,
                    sbComment.toString());
            StringBuilder sbPackIds = new StringBuilder("Package(s): ");
            for (String packageId : packageIds) {
                sbPackIds.append(packageId).append(";");
            }
            generateWorkflowActivity(siteId, environment, packageIds,  author, OPERATION_PUBLISHED);
            publishingManager.markItemsCompleted(siteId, environment, itemsToDeploy);
            logger.debug("Mark deployment completed for processed items for site \"{0}\"", siteId);
            logger.info("Finished publishing environment " + environment + " for site " + siteId);

            if (publishingManager.isPublishingQueueEmpty(siteId)) {
                status = READY;
            } else {
                status = QUEUED;
            }
            siteService.updatePublishingStatus(siteId, status);
        } catch (DeploymentException err) {
            logger.error("Error while executing deployment to environment store " +
                            "for site \"{0}\", number of items \"{1}\"", err, siteId,
                    itemsToDeploy.size());
            publishingManager.markItemsReady(siteId, environment, itemsToDeploy);
            siteService.enablePublishing(siteId, false);
            siteService.updatePublishingStatus(siteId, ERROR);
            throw err;
        } catch (Exception err) {
            logger.error("Unexpected error while executing deployment to environment " +
                            "store for site \"{0}\", number of items \"{1}\"", err, siteId,
                    itemsToDeploy.size());
            publishingManager.markItemsReady(siteId, environment, itemsToDeploy);
            siteService.enablePublishing(siteId, false);
            siteService.updatePublishingStatus(siteId, ERROR);
            throw err;
        }
    }

    private void processPublishingRequest(String siteId, String environment, PublishRequest item,
                                          List<DeploymentItemTO> completeDeploymentItemList, Set<String> processedPaths)
            throws ServiceLayerException, DeploymentException, UserNotFoundException {
        List<DeploymentItemTO> missingDependencies = new ArrayList<DeploymentItemTO>();
        Set<String> missingDependenciesPaths = new HashSet<String>();
        try {
            List<DeploymentItemTO> deploymentItemList = new ArrayList<DeploymentItemTO>();


            logger.debug("Processing [{0}] content item for site \"{1}\"", item
                    .getPath(), siteId);
            DeploymentItemTO deploymentItem = publishingManager.processItem(item);
            if (deploymentItem != null) {
                deploymentItemList.add(deploymentItem);
            }
            logger.debug("Processing COMPLETE [{0}] content item for site \"{1}\"",
                    item.getPath(), siteId);

            if (isMandatoryDependenciesCheckEnabled()) {
                logger.debug("Processing Mandatory Deps [{0}] content item for site "
                        + "\"{1}\"", item.getPath(), siteId);
                missingDependencies.addAll(publishingManager
                        .processMandatoryDependencies(item, processedPaths, missingDependenciesPaths));
                logger.debug("Processing Mandatory Dependencies COMPLETE [{0}]"
                        + " content item for site \"{1}\"", item.getPath(), siteId);
            }
            deploymentItemList.addAll(missingDependencies);
            completeDeploymentItemList.addAll(deploymentItemList);
        } catch (DeploymentException err) {
            logger.error("Error while executing deployment to environment store for site \"{0}\",", err, siteId);
            publishingManager.markItemsReady(siteId, environment, List.of(item));
            siteService.enablePublishing(siteId, false);
            siteService.updatePublishingStatus(siteId, ERROR);
            throw err;
        } catch (Exception err){
            logger.error("Unexpected error while executing deployment to environment " +
                    "store for site \"{0}\", ", err, siteId);
            publishingManager.markItemsReady(siteId, environment, List.of(item));
            siteService.enablePublishing(siteId, false);
            siteService.updatePublishingStatus(siteId, ERROR);
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
                                            String operation) throws ServiceLayerException, UserNotFoundException {
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
            auditLogParameter.setTargetType(TARGET_TYPE_PUBLISHING_PACKAGE);
            auditLogParameter.setTargetValue(packageId);
            auditLogParameters.add(auditLogParameter);
        }
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);

        User user = userServiceInternal.getUserByIdOrUsername(-1, username);
        packageIds.forEach(packageId ->
                activityStreamServiceInternal.insertActivity(siteFeed.getId(), user.getId(), operation,
                        DateUtils.getCurrentTime(), null, packageId)
        );
    }

    private boolean isMandatoryDependenciesCheckEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(
                JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED));
        return toReturn;
    }

    private Set<String> getAllPublishingEnvironments(String site) {
        Set<String> environments = new HashSet<String>();
        environments.add(servicesConfig.getLiveEnvironment(site));
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            environments.add(servicesConfig.getStagingEnvironment(site));
        }
        return environments;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public PublishingManager getPublishingManager() {
        return publishingManager;
    }

    public void setPublishingManager(PublishingManager publishingManager) {
        this.publishingManager = publishingManager;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public int getMaxRetryCounter() {
        return maxRetryCounter;
    }

    public void setMaxRetryCounter(int maxRetryCounter) {
        this.maxRetryCounter = maxRetryCounter;
    }

    public StudioClusterUtils getStudioClusterUtils() {
        return studioClusterUtils;
    }

    public void setStudioClusterUtils(StudioClusterUtils studioClusterUtils) {
        this.studioClusterUtils = studioClusterUtils;
    }

    public PublishingProgressServiceInternal getPublishingProgressServiceInternal() {
        return publishingProgressServiceInternal;
    }

    public void setPublishingProgressServiceInternal(PublishingProgressServiceInternal publishingProgressServiceInternal) {
        this.publishingProgressServiceInternal = publishingProgressServiceInternal;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public ActivityStreamServiceInternal getActivityStreamServiceInternal() {
        return activityStreamServiceInternal;
    }

    public void setActivityStreamServiceInternal(ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }
}
