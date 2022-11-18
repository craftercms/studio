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
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.event.publish.PublishEvent;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressObserver;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.UncategorizedSQLException;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.PublishStatus.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED;

public class StudioPublisherTask extends StudioClockTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioPublisherTask.class);

    protected static final Map<String, Integer> retryCounter = new HashMap<>();

    protected static final Set<String> dbErrorNotifiedSites = new HashSet<>();
    private PublishingManager publishingManager;
    private ServicesConfig servicesConfig;
    private NotificationService notificationService;
    private AuditServiceInternal auditServiceInternal;
    private int maxRetryCounter;
    private PublishingProgressServiceInternal publishingProgressServiceInternal;
    private UserServiceInternal userServiceInternal;
    private ActivityStreamServiceInternal activityStreamServiceInternal;

    @Override
    protected void executeInternal(String siteId) {
        String siteState = siteService.getSiteState(siteId);
        if (!StringUtils.equals(siteState, STATE_READY)) {
            return;
        }
        String env = null;
        try {
            if (contentRepository.repositoryExists(siteId) && siteService.isPublishingEnabled(siteId)) {
                if (!publishingManager.isPublishingBlocked(siteId)) {
                    List<PublishRequest> itemsToDeploy = emptyList();
                    try {
                        if (!retryCounter.containsKey(siteId)) {
                            retryCounter.put(siteId, maxRetryCounter);
                        }
                        Set<String> environments = getAllPublishingEnvironments(siteId);
                        for (String environment : environments) {
                            env = environment;
                            logger.trace("Process content item ready for publishing in site '{}'", siteId);
                            itemsToDeploy = publishingManager.getItemsReadyForDeployment(siteId, environment);
                            while (CollectionUtils.isNotEmpty(itemsToDeploy)) {
                                logger.trace("Publish '{}' items in site '{}'", itemsToDeploy.size(), siteId);
                                publishingManager.markItemsProcessing(siteId, environment, itemsToDeploy);
                                List<String> commitIds = itemsToDeploy.stream()
                                        .map(PublishRequest::getCommitId)
                                        .distinct().collect(toList());

                                boolean allCommitsPresent = true;
                                StringBuilder sbMissingCommits = new StringBuilder();
                                for (String commit : commitIds) {
                                    if (StringUtils.isNotEmpty(commit)) {
                                        boolean commitPresent = contentRepository.commitIdExists(siteId,
                                                commit);
                                        if (!commitPresent) {
                                            sbMissingCommits.append(commit).append("; ");
                                            logger.trace("Commit ID '{}' is not in the git repo for " +
                                                    "site '{}'. Skip a publishing cycle and try " +
                                                    "again next cycle.", commit, siteId);
                                            allCommitsPresent = false;
                                        }
                                    }
                                }

                                if (allCommitsPresent) {
                                    logger.info("Publish started in site '{}' for target '{}' with '{}' items " +
                                                    "ready to be published",
                                            siteId, environment, itemsToDeploy.size());
                                    String packageId = itemsToDeploy.get(0).getPackageId();
                                    PublishingProgressObserver observer =
                                            new PublishingProgressObserver(siteId, packageId, environment,
                                                    itemsToDeploy.size());
                                    publishingProgressServiceInternal.addObserver(observer);
                                    doPublishing(siteId, itemsToDeploy, environment);
                                    applicationContext.publishEvent(new PublishEvent(siteId));
                                    retryCounter.remove(siteId);
                                    dbErrorNotifiedSites.remove(siteId);
                                    itemsToDeploy =
                                            publishingManager.getItemsReadyForDeployment(siteId, environment);
                                } else {
                                    publishingManager.markItemsReady(siteId, environment, itemsToDeploy);
                                    int retriesLeft = retryCounter.get(siteId) - 1;
                                    itemsToDeploy = null;
                                    if (retriesLeft > 0) {
                                        retryCounter.put(siteId, retriesLeft);
                                        logger.info("The commit IDs '{}' are not in the git repo for site '{}'. " +
                                                "Skip a publishing cycle and try again next cycle. " +
                                                "'{}' retries left.",
                                                sbMissingCommits, siteId, retriesLeft);
                                    } else {
                                        retryCounter.remove(siteId);
                                        siteService.enablePublishing(siteId, false);
                                        logger.error("Exhausted publish retries for site '{}' after '{}' attempts " +
                                                "due to missing commit IDs '{}'",
                                                siteId, maxRetryCounter, sbMissingCommits);
                                        throw new DeploymentException("Deployment failed after " + maxRetryCounter
                                                + " retries. The following commits are not present in local " +
                                                "repository " + sbMissingCommits);
                                    }
                                }

                            }
                        }
                    } catch (UncategorizedSQLException e) {
                        logger.error("Failed to publish items in site '{}' due to a database error",
                                siteId, e);
                        if (!dbErrorNotifiedSites.add(siteId)) {
                            notificationService.notifyDeploymentError(siteId, e);
                        }
                        publishingManager.resetProcessingQueue(siteId, env);
                    } catch (Exception e) {
                        logger.error("Failed to publish items in site '{}'", siteId, e);
                        publishingManager.resetProcessingQueue(siteId, env);
                        notificationService.notifyDeploymentError(siteId, e, itemsToDeploy);
                    }
                } else {
                    logger.warn("Publishing is currently blocked for site '{}'", siteId);
                }
            } else {
                logger.debug("Publishing is currently disabled for site '{}'", siteId);
            }
        } catch (UncategorizedSQLException e) {
            // TODO: SJ: This is the same catch as above, consolidate
            logger.error("Failed to publish items in site '{}' due to a database error",
                    siteId, e);
            if (!dbErrorNotifiedSites.add(siteId)) {
                notificationService.notifyDeploymentError(siteId, e);
            }
            publishingManager.resetProcessingQueue(siteId, env);
        } catch (Exception e) {
            logger.error("Failed to publish items in site '{}'", siteId, e);
            notificationService.notifyDeploymentError(siteId, e);
            publishingManager.resetProcessingQueue(siteId, env);
        } finally {
            // Unlock publishing if queue does not have packages ready for publishing
            publishingProgressServiceInternal.removeObserver(siteId);
        }
    }

    private void doPublishing(String siteId, List<PublishRequest> itemsToDeploy, String environment)
            throws DeploymentException, ServiceLayerException, UserNotFoundException {
        siteService.updatePublishingStatus(siteId, PROCESSING);
        String status;
        String author = itemsToDeploy.get(0).getUser();
        StringBuilder sbComment = new StringBuilder();
        List<DeploymentItemTO> completeDeploymentItemList = new ArrayList<>();
        Set<String> processedPaths = new HashSet<>();
        String currentPackageId = StringUtils.EMPTY;
        try {
            logger.info("Publish '{}' items in site '{}' to target '{}'", itemsToDeploy.size(), siteId, environment);
            Set<String> packageIds = new HashSet<>();
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
            generateWorkflowActivity(siteId, environment, packageIds,  author, OPERATION_PUBLISHED);
            publishingManager.markItemsCompleted(siteId, environment, itemsToDeploy);

            logger.info("Published '{}' items in site '{}' to target '{}'",
                    itemsToDeploy.size(), siteId, environment);

            if (publishingManager.isPublishingQueueEmpty(siteId)) {
                status = READY;
            } else {
                status = QUEUED;
            }
            siteService.updatePublishingStatus(siteId, status);
        } catch (Exception e) {
            logger.error("Failed to publish '{}' items in site '{}'", itemsToDeploy.size(), siteId, e);
            publishingManager.markItemsReady(siteId, environment, itemsToDeploy);
            siteService.enablePublishing(siteId, false);
            siteService.updatePublishingStatus(siteId, ERROR);
            throw e;
        }
    }

    private void processPublishingRequest(String siteId, String environment, PublishRequest item,
                                          List<DeploymentItemTO> completeDeploymentItemList, Set<String> processedPaths)
            throws ServiceLayerException, DeploymentException, UserNotFoundException {
        List<DeploymentItemTO> missingDependencies = new ArrayList<>();
        Set<String> missingDependenciesPaths = new HashSet<>();
        try {
            List<DeploymentItemTO> deploymentItemList = new ArrayList<>();

            logger.trace("Process item in site '{}' path '{}'", siteId, item.getPath());
            DeploymentItemTO deploymentItem = publishingManager.processItem(item);
            if (deploymentItem != null) {
                deploymentItemList.add(deploymentItem);
            }
            logger.trace("Processing completed for item in site '{}' path '{}'", siteId, item.getPath());

            if (isMandatoryDependenciesCheckEnabled()) {
                logger.trace("Start mandatory dependency processing for site '{}' path '{}'",
                        siteId, item.getPath());
                missingDependencies.addAll(publishingManager
                        .processMandatoryDependencies(item, processedPaths, missingDependenciesPaths));
                logger.trace("Mandatory dependency processing for site '{}' path '{}' completed",
                        siteId, item.getPath());
            }
            deploymentItemList.addAll(missingDependencies);
            completeDeploymentItemList.addAll(deploymentItemList);
        } catch (Exception e) {
            logger.error("Failed to publish items from site '{}' to target '{}'", siteId, environment, e);

            publishingManager.markItemsReady(siteId, environment, List.of(item));
            siteService.enablePublishing(siteId, false);
            siteService.updatePublishingStatus(siteId, ERROR);
            throw e;
        }
    }

    private void deploy(String site, String environment, List<DeploymentItemTO> items, String author, String comment)
            throws DeploymentException, SiteNotFoundException {
        logger.trace("Publish '{}' items from site '{}' to target '{}' by author '{}' with comment '{}'",
                items.size(), site, environment, author, comment);
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
        List<AuditLogParameter> auditLogParameters = new ArrayList<>();
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
        return Boolean.parseBoolean(studioConfiguration.getProperty(
                JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED));
    }

    private Set<String> getAllPublishingEnvironments(String site) {
        Set<String> environments = new HashSet<>();
        environments.add(servicesConfig.getLiveEnvironment(site));
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            environments.add(servicesConfig.getStagingEnvironment(site));
        }
        return environments;
    }

    public void setPublishingManager(PublishingManager publishingManager) {
        this.publishingManager = publishingManager;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setMaxRetryCounter(int maxRetryCounter) {
        this.maxRetryCounter = maxRetryCounter;
    }

    public void setPublishingProgressServiceInternal(PublishingProgressServiceInternal publishingProgressServiceInternal) {
        this.publishingProgressServiceInternal = publishingProgressServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setActivityStreamServiceInternal(ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }
}
