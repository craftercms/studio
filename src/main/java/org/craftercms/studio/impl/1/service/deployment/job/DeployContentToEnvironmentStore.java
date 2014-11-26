/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.impl.service.deployment.job;

import org.craftercms.cstudio.api.job.Job;
import org.craftercms.cstudio.api.log.Logger;
import org.craftercms.cstudio.api.log.LoggerFactory;
import org.craftercms.cstudio.api.repository.ContentRepository;
import org.craftercms.cstudio.api.service.authentication.AuthenticationService;
import org.craftercms.cstudio.api.service.deployment.CopyToEnvironmentItem;
import org.craftercms.cstudio.api.service.deployment.DeploymentException;
import org.craftercms.cstudio.api.service.transaction.TransactionService;
import org.craftercms.cstudio.api.util.ListUtils;
//import org.craftercms.cstudio.impl.service.deployment.PublishingManager;

import javax.transaction.UserTransaction;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class DeployContentToEnvironmentStore implements Job {

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

    public synchronized static void setRunning(boolean isRunning){
        running = isRunning;
    }

    public void execute() {
        if (_masterPublishingNode && !stopSignaled) {
            setRunning(true);
            if (singleWorkerLock.tryLock()) {
                try {
                    Method processJobMethod = this.getClass().getMethod("processJobs", new Class[0]);
                    String adminUser = _authenticationService.getAdministratorUser();
                    _authenticationService.runAs(adminUser, this, processJobMethod);
                }
                catch(Throwable err) {
                    logger.error("unable to execute job", err);
                } finally {
                    singleWorkerLock.unlock();
                }
            }
            setRunning(false);
        }
    }

    public void processJobs() {
/*
        try {

            UserTransaction tx = _transactionService.getTransaction();

            try {
                // USE MANAGER TO DO ALL OF THIS, MOST OF THESE ARE PROTECTED MANAGER METHODS!
                tx.begin();

                Set<String> siteNames = _publishingManager.getAllAvailableSites();
                tx.commit();
                if (siteNames != null && siteNames.size() > 0){
                    for (String site : siteNames) {
                        Set<String> environments = _contentRepository.getAllPublishingEnvironments(site);
                        for (String environment : environments) {
                            logger.debug("Processing content ready for deployment for site \"{0}\"", site);
                            List<CopyToEnvironmentItem> itemsToDeploy = _publishingManager.getItemsReadyForDeployment(site, environment);
                            List<String> pathsToDeploy = getPaths(itemsToDeploy);
                            Set<String> missingDependenciesPaths = new HashSet<String>();

                            if (itemsToDeploy != null && itemsToDeploy.size() > 0) {
                                logger.debug("Site \"{0}\" has {1} items ready for deployment", site, itemsToDeploy.size());
                                logger.debug("Splitting items into chunks for processing", site, itemsToDeploy.size());
                                List<List<CopyToEnvironmentItem>> chunks = ListUtils.partition(itemsToDeploy, _processingChunkSize);

                                for (int i = 0; i < chunks.size(); i++) {

                                    List<CopyToEnvironmentItem> itemList = chunks.get(i);
                                    List<CopyToEnvironmentItem> missingDependencies = new ArrayList<CopyToEnvironmentItem>();
                                    for (CopyToEnvironmentItem item : itemList) {
                                        _contentRepository.lockItem(item.getSite(), item.getPath());
                                    }
                                    tx = _transactionService.getTransaction();
                                    tx.begin();
                                    try {
                                        logger.debug("Mark items as processing for site \"{0}\"", site);

                                        _publishingManager.markItemsProcessing(site, environment, itemList);
                                        for (CopyToEnvironmentItem item : itemList) {
                                            _contentRepository.lockItem(item.getSite(), item.getPath());
                                            try {
                                                _publishingManager.setLockBehaviourEnabled(false);
                                                logger.debug("Processing [{0}] content item for site \"{1}\"", item.getPath(), site);
                                                _publishingManager.processItem(item);
                                                if (_mandatoryDependenciesCheckEnabled) {
                                                    missingDependencies.addAll(_publishingManager.processMandatoryDependencies(item, pathsToDeploy, missingDependenciesPaths));
                                                }
                                            } finally {
                                                _contentRepository.unLockItem(item.getSite(), item.getPath());
                                                }
                                        }
                                        logger.debug("Setting up items for publishing synchronization for site \"{0}\"", site);
                                        if (_mandatoryDependenciesCheckEnabled && missingDependencies.size() > 0) {
                                            List<CopyToEnvironmentItem> mergedList = new ArrayList<CopyToEnvironmentItem>(itemList);
                                            mergedList.addAll(missingDependencies);
                                            _publishingManager.setupItemsForPublishingSync(site, environment, mergedList);
                                        } else {
                                            _publishingManager.setupItemsForPublishingSync(site, environment, itemList);
                                            }
                                        logger.debug("Mark deployment completed for processed items for site \"{0}\"", site);
                                        _publishingManager.markItemsCompleted(site, environment, itemList);
                                        tx.commit();
                                    } catch (DeploymentException err) {
                                        logger.error("Error while executing deployment to environment store for site \"{0}\", number of items \"{1}\", chunk number \"{2}\" (chunk size {3})", err, site, itemsToDeploy.size(), i, _processingChunkSize);
                                        _publishingManager.markItemsReady(site, environment, itemList);
                                        throw err;
                                    } catch (Exception err) {
                                        logger.error("Unexpected error while executing deployment to environment " +
                                            "store for site \"{0}\", number of items \"{1}\", chunk number \"{2}\" (chunk size {3})", err, site, itemsToDeploy.size(), i, _processingChunkSize);
                                        _publishingManager.markItemsReady(site, environment, itemList);
                                        throw err;
                                    } finally {
                                        for (CopyToEnvironmentItem item : itemList) {
                                            _contentRepository.unLockItem(item.getSite(), item.getPath());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } catch(Exception err) {
                logger.error("Error while executing deployment to environment store", err);
                tx.rollback();
            }
        }
        catch(Exception err) {
            logger.error("Error while executing deployment to environment store", err);
        }*/
    }

    private List<String> getPaths(List<CopyToEnvironmentItem> itemsToDeploy) {
        List<String> paths = new ArrayList<String>(itemsToDeploy.size());
        if (_mandatoryDependenciesCheckEnabled) {
            for (CopyToEnvironmentItem item : itemsToDeploy) {
                paths.add(_contentRepository.getFullPath(item.getSite(), item.getPath()));
            }
        }
        return paths;
    }

    /** getter auth service */
    public AuthenticationService getAuthenticationService() { return _authenticationService; }
    /** setter for auth service */
    public void setAuthenticationService(AuthenticationService service) { _authenticationService = service; }

    /** getter transaction service */
    public TransactionService getTransactionService() { return _transactionService; }
    /** setter for transaction service */
    public void setTransactionService(TransactionService service) { _transactionService = service; }
/*
    public PublishingManager getPublishingManager() { return _publishingManager; }
    public void setPublishingManager(PublishingManager publishingManager) { this._publishingManager = publishingManager; }
*/
    public ContentRepository getContentRepository() { return _contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this._contentRepository = contentRepository; }

    public int getProcessingChunkSize() {  return _processingChunkSize; }
    public void setProcessingChunkSize(int processingChunkSize) { this._processingChunkSize = processingChunkSize; }

    public boolean isMasterPublishingNode() { return _masterPublishingNode; }
    public void setMasterPublishingNode(boolean masterPublishingNode) { this._masterPublishingNode = masterPublishingNode; }

    public boolean isMandatoryDependenciesCheckEnabled() { return _mandatoryDependenciesCheckEnabled; }
    public void setMandatoryDependenciesCheckEnabled(boolean mandatoryDependenciesCheckEnabled) { this._mandatoryDependenciesCheckEnabled = mandatoryDependenciesCheckEnabled; }

    protected TransactionService _transactionService;
    protected AuthenticationService _authenticationService;
    //protected PublishingManager _publishingManager;
    protected ContentRepository _contentRepository;
    protected int _processingChunkSize;
    protected boolean _masterPublishingNode;
    protected boolean _mandatoryDependenciesCheckEnabled;

}