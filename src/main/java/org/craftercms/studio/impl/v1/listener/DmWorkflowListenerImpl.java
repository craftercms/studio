/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.listener;


import org.craftercms.studio.api.v1.cache.Scope;
import org.craftercms.studio.api.v1.cache.ThreadSafeCacheManager;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.listener.DmWorkflowListener;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.to.GoLiveQueue;

import java.util.Date;

public class DmWorkflowListenerImpl implements DmWorkflowListener {

    private static final Logger logger = LoggerFactory.getLogger(DmWorkflowListenerImpl.class);

    @Override
    public void postGolive(String site, DmDependencyTO submittedItem) {
        cache.getLock().writeLock().lock();
        try {
            if (submittedItem == null) {
                return;
            }
            logger.debug("Go live complete [" + submittedItem.getUri() + "]");
            if (!submittedItem.isDeleted()) {
                updateReferredObjects(site, submittedItem);
            }
        } finally {
            cache.getLock().writeLock().unlock();
        }
    }

    protected void updateReferredObjects(String site, DmDependencyTO submittedItem) {
        ContentItemTO to = updateCache(site, submittedItem);
    }

    protected ContentItemTO updateCache(String site, DmDependencyTO to) {

        ContentItemTO thisItem = updateCacheToGoliveState(to.getUri(), to.getScheduledDate(), site);
        logger.debug("update cache for url [" + to.getUri() + "]");
        if (to.getUri().endsWith(DmConstants.INDEX_FILE)) {
            String parentFolder = getParent(to.getUri());
            updateCacheToGoliveState(parentFolder, to.getScheduledDate(), site);
            logger.debug("update cache for url [" + parentFolder + "]");
        }
        return thisItem;
    }

    protected ContentItemTO updateCacheToGoliveState(String fullPath, Date scheduleDate, String site) {
        ContentItemTO to = (ContentItemTO) cache.get(Scope.DM_CONTENT_ITEM, fullPath);
        if (to != null) {
            ContentItemTO updatedTo = new ContentItemTO(to);
            updatedTo.setInProgress(false);
            updatedTo.setSubmitted(false);
            updatedTo.setScheduledDate(scheduleDate);
            //$Review$ issues here.
            if (scheduleDate != null) {
                updatedTo.setScheduled(true);
            } else {
                updatedTo.setNew(false);
                updatedTo.setScheduled(false);
            }
            cache.put(Scope.DM_CONTENT_ITEM, fullPath, updatedTo);
            GoLiveQueue queue = (GoLiveQueue) cache.get(Scope.DM_SUBMITTED_ITEMS, StudioConstants.DM_GO_LIVE_CACHE_KEY,site);
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

    protected void warmTheCache(String site, DmDependencyTO submitted, String path, boolean add) {
        GoLiveQueue queue = (GoLiveQueue) cache.get(Scope.DM_SUBMITTED_ITEMS, StudioConstants.DM_GO_LIVE_CACHE_KEY,site);
        if (queue != null) {
            if (null != path) {
                if (!submitted.isDeleted()) {
                    ContentItemTO to = contentService.getContentItem(site, path);
                    if (add) {
                        queue.add(to);
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
            }
        }
    }

    @Override
    public void postSubmitToGolive(String site, DmDependencyTO dependencyTO) {
        logger.debug("Submit to Go live complete [" + dependencyTO + "]");
    }

    @Override
    public void postReject(String site, DmDependencyTO submittedItem) {
        logger.debug("Reject complete [" + submittedItem.getUri() + "]");
    }

    protected ThreadSafeCacheManager cache;
    protected ContentService contentService;

    public ThreadSafeCacheManager getCache() {
        return this.cache;
    }
    public void setCache(ThreadSafeCacheManager cache) {
        this.cache = cache;
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }
}
