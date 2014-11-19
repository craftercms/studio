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
package org.craftercms.cstudio.alfresco.dm.listener.impl;

import org.craftercms.cstudio.alfresco.cache.Scope;
import org.craftercms.cstudio.alfresco.cache.ThreadSafeCacheManager;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.listener.DmWorkflowListener;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.impl.GoLiveQueue;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.to.DmDependencyTO;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DmWorkflowListenerImpl implements DmWorkflowListener {

    private static final Logger logger = LoggerFactory.getLogger(DmWorkflowListenerImpl.class);

    protected ThreadSafeCacheManager _cache;
    public ThreadSafeCacheManager getCache() {
        return this._cache;
    }
    public void setCache(ThreadSafeCacheManager cache) {
        this._cache = cache;
    }

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public void postGolive(String site, DmDependencyTO submittedItem) {
        _cache.getLock().writeLock().lock();
        try {
            if (submittedItem == null) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Go live complete [" + submittedItem.getUri() + "]");
            }
            if (!submittedItem.isDeleted()) {
                updateReferredObjects(site, submittedItem);
            }
        } finally {
            _cache.getLock().writeLock().unlock();
        }
    }

    protected void updateReferredObjects(String site, DmDependencyTO submittedItem) {
        DmContentItemTO to = updateCache(site, submittedItem);
    }

    protected DmContentItemTO updateCache(String site, DmDependencyTO to) {
        DmContentService dmContentService = getServicesManager().getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, to.getUri());

        DmContentItemTO thisItem = updateCacheToGoliveState(fullPath, to.getScheduledDate(), site);
        if (logger.isDebugEnabled()) {
            logger.debug("update cache for url [" + fullPath + "]");
        }
        if (fullPath.endsWith(DmConstants.INDEX_FILE)) {
            String parentFolder = getParent(fullPath);
            updateCacheToGoliveState(parentFolder, to.getScheduledDate(), site);
            //GoLiveQueue queue = (GoLiveQueue) cache.get(Scope.WCM_SUBMITTED_ITEMS, "goliveQueue");

            if (logger.isDebugEnabled()) {
                logger.debug("update cache for url [" + parentFolder + "]");
            }
        }
        return thisItem;
    }

    protected DmContentItemTO updateCacheToGoliveState(String fullPath, Date scheduleDate, String site) {
        DmContentItemTO to = (DmContentItemTO) _cache.get(Scope.DM_CONTENT_ITEM, fullPath);
        if (to != null) {
            DmContentItemTO updatedTo = new DmContentItemTO(to);
            updatedTo.setInProgress(false);
            updatedTo.setSubmitted(false);
            updatedTo.setScheduledDate(scheduleDate);
            //$Review$ issues here.
            if (scheduleDate != null) {
                updatedTo.setScheduled(true);
            } else {
                updatedTo.setNewFile(false);
                updatedTo.setScheduled(false);
            }
            _cache.put(Scope.DM_CONTENT_ITEM, fullPath, updatedTo);
            GoLiveQueue queue = (GoLiveQueue) _cache.get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY,site);
            if (null != queue) {
                queue.remove(to.getUri());
            }
        }
        return to;
    }


    protected String getParent(String fullPath) {
        String s = "/" + DmConstants.INDEX_FILE;
        int i = fullPath.length() - s.length();
        String parentFolder = fullPath.substring(0, i);
        return parentFolder;
    }

    protected void warmTheCache(String site, DmDependencyTO submitted, String fullPath, boolean add) {
        GoLiveQueue queue = (GoLiveQueue) _cache.get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY,site);
        if (queue != null) {
            if (null != fullPath) {
                try {
                    if (!submitted.isDeleted()) {
                        try {
                            PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
                            DmContentItemTO to = persistenceManagerService.getContentItem(fullPath);
                            if (add) {
                                queue.add(to);
                            }
                        } catch (ContentNotFoundException e) {
                            logger.warn("warmTheCache: No Content found for [" + fullPath + "]");
                        }
                    }
                    if (!add) {
                        String uri = submitted.getUri();
                        if(uri != null) {
                            String indexOrParentUri;
                            //if Uri ends with index.xml then get parent and remove else create URI with index.xml and remove.
                            if(uri.endsWith(DmConstants.INDEX_FILE)) {
                                indexOrParentUri = getParent(uri);
                            } else {
                                indexOrParentUri = uri.endsWith("/")? (uri + DmConstants.INDEX_FILE) : (uri + "/" + DmConstants.INDEX_FILE);
                            }
                            queue.remove(indexOrParentUri);
                        }
                        queue.remove(uri);
                    }

                } catch (ServiceException e) {
                    logger.error("Error while warming up the cache.", e);
                }
            }
        }
    }

    @Override
    public void postSubmitToGolive(String site, DmDependencyTO dependencyTO) {
        if (logger.isDebugEnabled()) {
            logger.debug("Submit to Go live complete [" + dependencyTO + "]");
        }
    }

    @Override
    public void postReject(String site, DmDependencyTO submittedItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Reject complete [" + submittedItem.getUri() + "]");
        }
    }
}
