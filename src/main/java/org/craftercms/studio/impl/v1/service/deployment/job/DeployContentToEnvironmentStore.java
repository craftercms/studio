/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.service.deployment.job;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.ebus.DeploymentItem;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.impl.v1.job.RepositoryJob;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

public class DeployContentToEnvironmentStore extends RepositoryJob {

    private static final Logger logger = LoggerFactory.getLogger(DeployContentToEnvironmentStore.class);

    protected static final ReentrantLock singleWorkerLock = new ReentrantLock();

    private static boolean stopSignaled = false;
    private static boolean running = false;

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
                    if (siteService.isPublishingEnabled(site)) {
                        if (!publishingManager.isPublishingBlocked(site)) {
                            String statusMessage = StringUtils.EMPTY;
                            try {
                                Set<String> environments = getAllPublishingEnvironments(site);
                                for (String environment : environments) {
                                    logger.debug("Processing content ready for deployment for site \"{0}\"", site);
                                    List<CopyToEnvironment> itemsToDeploy = publishingManager.getItemsReadyForDeployment(site, environment);
                                    List<String> pathsToDeploy = getPaths(itemsToDeploy);

                                    if (itemsToDeploy != null && itemsToDeploy.size() > 0) {
                                        logger.debug("Site \"{0}\" has {1} items ready for deployment", site, itemsToDeploy.size());

                                        List<DeploymentItem> missingDependencies = new ArrayList<DeploymentItem>();

                                        for (CopyToEnvironment item : itemsToDeploy) {
                                            String lockKey = item.getSite() + ":" + item.getPath();
                                            generalLockService.lock(lockKey);
                                            contentRepository.lockItem(item.getSite(), item.getPath());
                                        }

                                        try {
                                            logger.debug("Mark items as processing for site \"{0}\"", site);
                                            for (CopyToEnvironment item : itemsToDeploy) {
                                                SimpleDateFormat sdf = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
                                                statusMessage = studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_BUSY);
                                                statusMessage = statusMessage.replace("{item_path}", item.getPath()).replace("{datetime}", sdf.format(new Date()));
                                                siteService.updatePublishingStatusMessage(site, statusMessage);
                                                publishingManager.markItemsProcessing(site, environment, Arrays.asList(item));
                                                String lockKey = item.getSite() + ":" + item.getPath();
                                                generalLockService.lock(lockKey);
                                                contentRepository.lockItem(item.getSite(), item.getPath());

                                                Set<String> missingDependenciesPaths = new HashSet<String>();
                                                List<DeploymentItem> deploymentItemList = new ArrayList<DeploymentItem>();
                                                String author = item.getUser();
                                                String comment = item.getSubmissionComment();

                                                try {
                                                    logger.debug("Processing [{0}] content item for site \"{1}\"", item
                                                            .getPath(), site);
                                                    DeploymentItem deploymentItem = publishingManager.processItem(item);
                                                    deploymentItemList.add(deploymentItem);
                                                    logger.debug("Processing COMPLETE [{0}] content item for site \"{1}\"",
                                                            item.getPath(), site);

                                                    if (isMandatoryDependenciesCheckEnabled()) {
                                                        logger.debug("Processing Mandatory Deps [{0}] content item for site "
                                                                + "\"{1}\"", item.getPath(), site);
                                                        missingDependencies.addAll(publishingManager
                                                                .processMandatoryDependencies(item, pathsToDeploy, missingDependenciesPaths));
                                                        logger.debug("Processing Mandatory Dependencies COMPLETE [{0}]"
                                                                + " content item for site \"{1}\"", item.getPath(), site);
                                                    }
                                                    deploymentItemList.addAll(missingDependencies);
                                                    deploy(site, environment, deploymentItemList, author, comment);
                                                    publishingManager.markItemsCompleted(site, environment, Arrays.asList(item));
                                                    statusMessage = studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_IDLE);
                                                    statusMessage = statusMessage.replace("{item_path}", item.getPath()).replace("{datetime}", sdf.format(new Date()));
                                                } catch (DeploymentException err) {
                                                    logger.error("Error while executing deployment to environment store for site \"{0}\", number of items \"{1}\"", err, site, itemsToDeploy.size());
                                                    publishingManager.markItemsReady(site, environment, Arrays.asList(item));
                                                    siteService.enablePublishing(site, false);
                                                    statusMessage = studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR);
                                                    statusMessage = statusMessage.replace("{item_path}", item.getPath()).replace("{datetime}", sdf.format(new Date()));
                                                    siteService.updatePublishingStatusMessage(site, statusMessage);
                                                    throw err;
                                                } catch (Exception err) {
                                                    logger.error("Unexpected error while executing deployment to environment " +
                                                            "store for site \"{0}\", number of items \"{1}\"", err, site, itemsToDeploy.size());
                                                    publishingManager.markItemsReady(site, environment, Arrays.asList(item));
                                                    siteService.enablePublishing(site, false);
                                                    statusMessage = studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_ERROR);
                                                    statusMessage = statusMessage.replace("{item_path}", item.getPath()).replace("{datetime}", sdf.format(new Date()));
                                                    siteService.updatePublishingStatusMessage(site, statusMessage);
                                                    throw err;
                                                } finally {
                                                    generalLockService.unlock(lockKey);
                                                    contentRepository.unLockItem(item.getSite(), item.getPath());
                                                }
                                            }
                                            siteService.updatePublishingStatusMessage(site, statusMessage);
                                            logger.debug("Mark deployment completed for processed items for site \"{0}\"", site);
                                        } finally {
                                            for (CopyToEnvironment item : itemsToDeploy) {
                                                String itemSite = item.getSite();
                                                String itemPath = item.getPath();
                                                String lockKey = itemSite + ":" + itemPath;

                                                try {
                                                    generalLockService.unlock(lockKey);
                                                    contentRepository.unLockItem(itemSite, itemPath);
                                                } catch (Exception eUnlockError) {
                                                    logger.error("Unable to unlock item after deploy site:{0} path:{1} error:{2}", itemSite, itemPath, "" + eUnlockError);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception err) {
                                logger.error("Error while executing deployment to environment store for site: " + site, err);
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

    private void deploy(String site, String environment, List<DeploymentItem> items, String author, String comment) throws DeploymentException {
        logger.debug("Deploying " + items.size() + " item(s)");
        List<String> commitIds = new ArrayList<String>(items.size());
        for (DeploymentItem item : items) {
            logger.debug("Getting commit ids from sandbox for item " + item.getPath() + " site: " + item.getSite() + " <commitId = " + item.getCommitId() + ">");
            List<String> itemCommitIds = contentRepository.getEditCommitIds(item.getSite(), item.getPath(), item.getLastPublishedCommitId(), item.getCommitId());
            if (itemCommitIds.size() < 1) {
                throw new DeploymentException("Commit ids not found in sandbox for " + item.getPath() + " site: " + item.getSite() + " (commit ID: " + item.getCommitId() + ", last published commit ID: " + item.getLastPublishedCommitId() + ")");
            }
            String logMessage = "Found " + itemCommitIds.size() + " since last published commit id";
            for (String itemCID : itemCommitIds) {
                logMessage += "\n\t\t" + itemCID;
            }
            logger.debug(logMessage);
            commitIds.addAll(itemCommitIds);
        }
        contentRepository.publish(site, commitIds, environment, author, comment);
    }

    private List<String> getPaths(List<CopyToEnvironment> itemsToDeploy) {
        List<String> paths = new ArrayList<String>(itemsToDeploy.size());
        if (isMandatoryDependenciesCheckEnabled()) {
            for (CopyToEnvironment item : itemsToDeploy) {
                paths.add(item.getPath());
            }
        }
        return paths;
    }

    private Set<String> getAllPublishingEnvironments(String site) {
        List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(site);
        Set<String> environments = new HashSet<String>();
        if (publishingTargets != null && publishingTargets.size() > 0) {
            for (PublishingTargetTO target : publishingTargets) {
                if (StringUtils.isNotEmpty(target.getRepoBranchName())) {
                    environments.add(target.getRepoBranchName());
                }
            }
        }
        return environments;
    }

    public boolean isMasterPublishingNode() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(JOB_DEPLOYMENT_MASTER_PUBLISHING_NODE));
        return toReturn;
    }

    public boolean isMandatoryDependenciesCheckEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_MANDATORY_DEPENDENCIES_CHECK_ENABLED));
        return toReturn;
    }

    public int getProcessingChunkSize() {
        int toReturn = Integer.parseInt(studioConfiguration.getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_PROCESSING_CHUNK_SIZE));
        return toReturn;
    }

    public PublishingManager getPublishingManager() { return publishingManager; }
    public void setPublishingManager(PublishingManager publishingManager) { this.publishingManager = publishingManager; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }


    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public EventService getEventService() { return eventService; }
    public void setEventService(EventService eventService) { this.eventService = eventService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected PublishingManager publishingManager;
    protected ContentRepository contentRepository;
    protected SiteService siteService;
    protected NotificationService notificationService;
    protected GeneralLockService generalLockService;
    protected EventService eventService;
    protected StudioConfiguration studioConfiguration;
}
