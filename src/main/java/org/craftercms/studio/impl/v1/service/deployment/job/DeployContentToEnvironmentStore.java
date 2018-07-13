/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */
package org.craftercms.studio.impl.v1.service.deployment.job;


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
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.impl.v1.job.RepositoryJob;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOYMENT_MASTER_PUBLISHING_NODE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_PROCESSING_CHUNK_SIZE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_IDLE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_PUBLISHING;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_QUEUED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT_CONFIG_ENABLED;


public class DeployContentToEnvironmentStore extends RepositoryJob {

    private static final Logger logger = LoggerFactory.getLogger(DeployContentToEnvironmentStore.class);

    protected static final ReentrantLock singleWorkerLock = new ReentrantLock();

    private static boolean stopSignaled = false;
    private static boolean running = false;

    protected PublishingManager publishingManager;
    protected ContentRepository contentRepository;
    protected SiteService siteService;
    protected NotificationService notificationService;
    protected GeneralLockService generalLockService;
    protected EventService eventService;
    protected StudioConfiguration studioConfiguration;
    protected ActivityService activityService;
    protected ServicesConfig servicesConfig;

    public static synchronized void signalToStop(boolean toStop) {
        stopSignaled = toStop;
    }

    public synchronized static boolean isRunning() {
        return running;
    }

    public synchronized static void setRunning(boolean isRunning) {
        running = isRunning;
    }

    public void execute() {
        if (isMasterPublishingNode() && !stopSignaled) {
            setRunning(true);
            if (singleWorkerLock.tryLock()) {
                try {
                    processJobs();
                } catch (Throwable err) {
                    logger.error("unable to execute job", err);
                } finally {
                    singleWorkerLock.unlock();
                }
            }
            setRunning(false);
        }
    }

    public void processJobs() {

        try {
            Set<String> siteNames = siteService.getAllAvailableSites();
            if (siteNames != null && siteNames.size() > 0) {
                for (String site : siteNames) {
                    try {
                        syncRepository(site);
                    } catch (Exception e) {
                        logger.error("Failed to sync database from repository for site " + site, e);
                        siteService.enablePublishing(site, false);
                    }
                    if (siteService.isPublishingEnabled(site)) {
                        if (!publishingManager.isPublishingBlocked(site)) {
                            String statusMessage = StringUtils.EMPTY;
                            try {
                                Set<String> environments = getAllPublishingEnvironments(site);
                                for (String environment : environments) {
                                    logger.debug("Processing content ready for deployment for site \"{0}\"", site);
                                    List<PublishRequest> itemsToDeploy =
                                            publishingManager.getItemsReadyForDeployment(site, environment);

                                    if (itemsToDeploy != null && itemsToDeploy.size() > 0) {
                                        logger.info("Starting publishing on environment " + environment + " for site " + site);
                                        logger.debug("Site \"{0}\" has {1} items ready for deployment",
                                                site, itemsToDeploy.size());

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
                                            generateWorkflowActivity(site, sbPackIds.toString(), author,
                                                    ActivityService .ActivityType .PUBLISHED);
                                            publishingManager.markItemsCompleted(site, environment, itemsToDeploy);
                                            logger.debug("Mark deployment completed for processed items for site \"{0}\"", site);
                                            logger.info("Finished publishing environment " + environment + " for site " + site);

                                            if (publishingManager.isPublishingQueueEmpty(site)) {
                                                statusMessage = studioConfiguration.getProperty
                                                        (JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_IDLE);
                                                statusMessage = statusMessage.replace("{package_id}", currentPackageId)
                                                        .replace("{datetime}", ZonedDateTime.now(ZoneOffset.UTC)
                                                                .format(DateTimeFormatter.ofPattern(sdf.toPattern())));
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
                                        } catch (Exception err){
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
                                    }
                                }
                            } catch (Exception err) {
                                logger.error("Error while executing deployment to environment store for site: "
                                        + site, err);
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
            }
        } catch (Exception err) {
            logger.error("Error while executing deployment to environment store", err);
            notificationService.notifyDeploymentError("UNKNOWN", err);
        }
    }

    protected void generateWorkflowActivity(String site, String path, String username,
                                            ActivityService.ActivityType activityType) {
        Map<String, String> extraInfo = new HashMap<String, String>();
        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
        activityService.postActivity(site, username, path, activityType,
                ActivityService.ActivitySource.API, extraInfo);
    }

    private void processPublishingRequest(String site, String environment, PublishRequest item,
                                          List<DeploymentItemTO> completeDeploymentItemList,
                                          Set<String> processedPaths) throws ServiceException, DeploymentException {
        List<DeploymentItemTO> missingDependencies = new ArrayList<DeploymentItemTO>();
        Set<String> missingDependenciesPaths = new HashSet<String>();
        SimpleDateFormat sdf = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
        String messagePath = item.getPath();
        String statusMessage = StringUtils.EMPTY;
        publishingManager.markItemsProcessing(site, environment, Arrays.asList(item));
        try {
            List<DeploymentItemTO> deploymentItemList = new ArrayList<DeploymentItemTO>();


            logger.debug("Processing [{0}] content item for site \"{1}\"", item
                    .getPath(), site);
            DeploymentItemTO deploymentItem = publishingManager.processItem(item);
            deploymentItemList.add(deploymentItem);
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
        boolean siteEnvironmentConfigEnabled = Boolean.parseBoolean(
                studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT_CONFIG_ENABLED));
        SiteFeed siteFeed = siteService.getSite(site);
        if (!siteEnvironmentConfigEnabled) {
            String liveEnvironment = servicesConfig.getLiveEnvironment(site);
            if (StringUtils.equals(liveEnvironment, environment)) {
                String stagingEnvironment = servicesConfig.getStagingEnvironment(site);
                contentRepository.publish(site, siteFeed.getSandboxBranch(), items, stagingEnvironment, author, comment);
            }
        }
        contentRepository.publish(site, siteFeed.getSandboxBranch(), items, environment, author, comment);

    }

    private Set<String> getAllPublishingEnvironments(String site) {
        Set<String> environments = new HashSet<String>();
        boolean siteEnvironmentConfigEnabled = Boolean.parseBoolean(
                studioConfiguration.getProperty(CONFIGURATION_SITE_ENVIRONMENT_CONFIG_ENABLED));
        if (!siteEnvironmentConfigEnabled) {
            environments.add(servicesConfig.getLiveEnvironment(site));
            environments.add(servicesConfig.getStagingEnvironment(site));
        } else {
            List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(site);

            if (publishingTargets != null && publishingTargets.size() > 0) {
                for (PublishingTargetTO target : publishingTargets) {
                    if (StringUtils.isNotEmpty(target.getRepoBranchName())) {
                        environments.add(target.getRepoBranchName());
                    }
                }
            }
        }
        return environments;
    }

    private void syncRepository(String site) throws SiteNotFoundException {
        logger.debug("Getting last verified commit for site: " + site);
        SiteFeed siteFeed = siteService.getSite(site);
        String lastProcessedCommit = siteFeed.getLastVerifiedGitlogCommitId();
        if (StringUtils.isNotEmpty(lastProcessedCommit)) {
            logger.debug("Syncing database with repository for site " + site + " from last processed commit "
                    + lastProcessedCommit);
            siteService.syncDatabaseWithRepo(site, lastProcessedCommit);
        } else {
            logger.debug("Syncing database with repository for site " + site + " from initial commit");
            siteService.syncDatabaseWithRepo(site, contentRepository.getRepoFirstCommitId(site));
        }
    }

    public boolean isMasterPublishingNode() {
        boolean toReturn =
                Boolean.parseBoolean(studioConfiguration.getProperty(JOB_DEPLOYMENT_MASTER_PUBLISHING_NODE));
        return toReturn;
    }

    public boolean isMandatoryDependenciesCheckEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(
                JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED));
        return toReturn;
    }

    public int getProcessingChunkSize() {
        int toReturn = Integer.parseInt(studioConfiguration.getProperty(
                JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_PROCESSING_CHUNK_SIZE));
        return toReturn;
    }

    public PublishingManager getPublishingManager() {
        return publishingManager;
    }

    public void setPublishingManager(PublishingManager publishingManager) {
        this.publishingManager = publishingManager;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ActivityService getActivityService() {
        return activityService;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }
}
