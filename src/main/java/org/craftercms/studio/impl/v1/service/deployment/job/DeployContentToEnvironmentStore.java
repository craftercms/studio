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


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.PublishingChannelGroupConfigTO;
import org.craftercms.studio.api.v1.util.ListUtils;
import org.craftercms.studio.impl.v1.job.RepositoryJob;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class DeployContentToEnvironmentStore extends RepositoryJob {

    private static final Logger logger = LoggerFactory.getLogger(DeployContentToEnvironmentStore.class);

    private static final String LIVE_ENVIRONMENT = "live";

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

    public void executeAsSignedInUser() {
        if (masterPublishingNode && !stopSignaled) {
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
                        Set<String> environments = getAllPublishingEnvironments(site);
                        for (String environment : environments) {
                            logger.debug("Processing content ready for deployment for site \"{0}\"", site);
                            List<CopyToEnvironment> itemsToDeploy = publishingManager.getItemsReadyForDeployment(site, environment);
                            List<String> pathsToDeploy = getPaths(itemsToDeploy);
                            Set<String> missingDependenciesPaths = new HashSet<String>();

                            if (itemsToDeploy != null && itemsToDeploy.size() > 0) {
                                logger.debug("Site \"{0}\" has {1} items ready for deployment", site, itemsToDeploy.size());
                                logger.debug("Splitting items into chunks for processing", site, itemsToDeploy.size());
                                List<List<CopyToEnvironment>> chunks = ListUtils.partition(itemsToDeploy, processingChunkSize);

                                for (int i = 0; i < chunks.size(); i++) {

                                    List<CopyToEnvironment> itemList = chunks.get(i);
                                    List<CopyToEnvironment> missingDependencies = new ArrayList<CopyToEnvironment>();
                                    
                                    for (CopyToEnvironment item : itemList) {
                                        String lockKey = item.getSite() + ":" + item.getPath();
                                        generalLockService.lock(lockKey);
                                        if (contentService.contentExists(item.getSite(), item.getPath())) {
                                            contentRepository.lockItem(item.getSite(), item.getPath());
                                        }
                                    }
                                    
                                    try {
                                        logger.debug("Mark items as processing for site \"{0}\"", site);

                                        publishingManager.markItemsProcessing(site, environment, itemList);
                                        for (CopyToEnvironment item : itemList) {
                                            String lockKey = item.getSite() + ":" + item.getPath();
                                            generalLockService.lock(lockKey);
                                            if (contentService.contentExists(item.getSite(), item.getPath())) {
                                                contentRepository.lockItem(item.getSite(), item.getPath());
                                            }
                                            try {
                                                logger.debug("Processing [{0}] content item for site \"{1}\"", item.getPath(), site);
                                                publishingManager.processItem(item);
                                                logger.debug("Processing COMPLETE [{0}] content item for site \"{1}\"", item.getPath(), site);
                                                
                                                if (mandatoryDependenciesCheckEnabled) {
                                                    logger.debug("Processing Mandatory Deps [{0}] content item for site \"{1}\"", item.getPath(), site);
                                                    missingDependencies.addAll(publishingManager.processMandatoryDependencies(item, pathsToDeploy, missingDependenciesPaths));
                                                    logger.debug("Processing Mandatory Deps COMPLETE [{0}] content item for site \"{1}\"", item.getPath(), site);

                                                }
                                                
                                            }
                                            finally {
                                                generalLockService.unlock(lockKey);
                                                contentRepository.unLockItem(item.getSite(), item.getPath());
                                            }
                                        }
                                       

                                        logger.debug("Setting up items for publishing synchronization for site \"{0}\"", site);
                                        if (mandatoryDependenciesCheckEnabled && missingDependencies.size() > 0) {
                                            List<CopyToEnvironment> mergedList = new ArrayList<CopyToEnvironment>(itemList);
                                            mergedList.addAll(missingDependencies);
                                            publishingManager.setupItemsForPublishingSync(site, environment, mergedList);
                                        } else {
                                            publishingManager.setupItemsForPublishingSync(site, environment, itemList);
                                        }
                                        
                                        logger.debug("Mark deployment completed for processed items for site \"{0}\"", site);
                                        publishingManager.markItemsCompleted(site, environment, itemList);
 
                                    
                                    }
                                    catch (DeploymentException err) {
                                        logger.error("Error while executing deployment to environment store for site \"{0}\", number of items \"{1}\", chunk number \"{2}\" (chunk size {3})", err, site, itemsToDeploy.size(), i, processingChunkSize);
                                        publishingManager.markItemsReady(site, environment, itemList);
                                        throw err;
                                    }
                                    catch (Exception err) {
                                        logger.error("Unexpected error while executing deployment to environment " +
                                                "store for site \"{0}\", number of items \"{1}\", chunk number \"{2}\" (chunk size {3})", err, site, itemsToDeploy.size(), i, processingChunkSize);
                                        publishingManager.markItemsReady(site, environment, itemList);
                                        throw err;
                                    }
                                    finally {
                                        for (CopyToEnvironment item : itemList) {
                                            String itemSite = item.getSite();
                                            String itemPath = item.getPath();
                                            String lockKey =  itemSite + ":" + itemPath;
                                            
                                            try {
                                                generalLockService.unlock(lockKey);
                                                contentRepository.unLockItem(itemSite, itemPath);
                                            }
                                            catch(Exception eUnlockError) {
                                                logger.error("Unble to unlock item after deploy site:{0} path:{1} error:{2}", itemSite, itemPath,""+eUnlockError);
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    } catch (Exception err) {
                        logger.error("Error while executing deployment to environment store for site: " + site, err);
                        notificationService.sendDeploymentFailureNotification(site, err);
                        notificationService2.notifyDeploymentError(site,err);
                        logger.info("Continue executing deployment for other sites.");
                    }
                }
            }
        } catch (Exception err) {
            logger.error("Error while executing deployment to environment store", err);
            notificationService.sendDeploymentFailureNotification("UNKNOWN", err);
        }
    }




    private List<String> getPaths(List<CopyToEnvironment> itemsToDeploy) {
        List<String> paths = new ArrayList<String>(itemsToDeploy.size());
        if (mandatoryDependenciesCheckEnabled) {
            for (CopyToEnvironment item : itemsToDeploy) {
                paths.add(contentService.expandRelativeSitePath(item.getSite(), item.getPath()));
            }
        }
        return paths;
    }

    private Set<String> getAllPublishingEnvironments(String site) {
        Map<String, PublishingChannelGroupConfigTO> groupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        Set<String> environments = new HashSet<String>();
        if (groupConfigTOs != null && groupConfigTOs.size() > 0) {
            for (PublishingChannelGroupConfigTO groupConfigTO : groupConfigTOs.values()) {
                if (StringUtils.isNotEmpty(groupConfigTO.getName())) {
                    environments.add(groupConfigTO.getName());
                }
            }
        }
        return environments;
    }


    /** getter transaction service */
    public org.craftercms.studio.api.v1.service.transaction.TransactionService getTransactionService() { return transactionService; }
    /** setter for transaction service */
    public void setTransactionService(org.craftercms.studio.api.v1.service.transaction.TransactionService service) { transactionService = service; }

    public PublishingManager getPublishingManager() { return publishingManager; }
    public void setPublishingManager(PublishingManager publishingManager) { this.publishingManager = publishingManager; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public int getProcessingChunkSize() { return processingChunkSize; }
    public void setProcessingChunkSize(int processingChunkSize) { this.processingChunkSize = processingChunkSize; }

    public boolean isMasterPublishingNode() { return masterPublishingNode; }
    public void setMasterPublishingNode(boolean masterPublishingNode) { this.masterPublishingNode = masterPublishingNode; }

    public boolean isMandatoryDependenciesCheckEnabled() { return mandatoryDependenciesCheckEnabled; }
    public void setMandatoryDependenciesCheckEnabled(boolean mandatoryDependenciesCheckEnabled) { this.mandatoryDependenciesCheckEnabled = mandatoryDependenciesCheckEnabled; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public NotificationService getNotificationService() { return notificationService; }
    public void setNotificationService(NotificationService notificationService) { this.notificationService = notificationService; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }


    public void setNotificationService2(final org.craftercms.studio.api.v2.service.notification.NotificationService
                                            notificationService2) {
        this.notificationService2 = notificationService2;
    }

    public void setObjectMetadataManager(final ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    protected org.craftercms.studio.api.v1.service.transaction.TransactionService transactionService;
    protected PublishingManager publishingManager;
    protected ContentRepository contentRepository;
    protected int processingChunkSize;
    protected boolean masterPublishingNode;
    protected boolean mandatoryDependenciesCheckEnabled;
    protected SiteService siteService;
    protected ContentService contentService;
    protected NotificationService notificationService;
    protected org.craftercms.studio.api.v2.service.notification.NotificationService notificationService2;
    protected GeneralLockService generalLockService;
    protected ObjectMetadataManager objectMetadataManager;
}
