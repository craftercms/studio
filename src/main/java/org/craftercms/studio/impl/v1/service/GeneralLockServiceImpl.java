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
package org.craftercms.studio.impl.v1.service;

import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class GeneralLockServiceImpl implements GeneralLockService {

    private static final Logger logger = LoggerFactory.getLogger(GeneralLockServiceImpl.class);

    private static final String KEY_TEMPLATE_CONTENT_ITEM = "CONTENT_ITEM_{site}_{path}";
    private static final String PATTERN_SITE = "\\{site\\}";
    private static final String PATTERN_PATH = "\\{path\\}";

    protected Map<String, ReentrantLock> nodeLocks = new HashMap<String, ReentrantLock>();

    @Override
    @ValidateParams
    public void lock(@ValidateStringParam(name = "objectId") String objectId) {
        ReentrantLock nodeLock;
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Obtaining lock for id " + objectId);
        }
        synchronized (this) {
            if (nodeLocks.containsKey(objectId)) {
                nodeLock = nodeLocks.get(objectId);
            } else {
                nodeLock = new ReentrantLock();
                nodeLocks.put(objectId, nodeLock);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock hold count " + nodeLock.getHoldCount() + " for id " + objectId + " (before lock)");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock: " + nodeLock.toString());
        }
        nodeLock.lock();
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock hold count " + nodeLock.getHoldCount() + " for id " + objectId + " (after lock)");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Locked all threads for id " + objectId);
        }
    }

    @Override
    @ValidateParams
    public boolean tryLock(@ValidateStringParam(name = "objectId") String objectId) {
        ReentrantLock nodeLock;
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Trying to get lock for id " + objectId);
        }
        synchronized (this) {
            if (nodeLocks.containsKey(objectId)) {
                nodeLock = nodeLocks.get(objectId);
            } else {
                nodeLock = new ReentrantLock();
                nodeLocks.put(objectId, nodeLock);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock hold count " + nodeLock.getHoldCount() + " for id " + objectId + " (before tryLock)");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock: " + nodeLock.toString());
        }
        boolean toRet = nodeLock.tryLock();
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock hold count " + nodeLock.getHoldCount() + " for id " + objectId + " (after tryLock)");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Result for tryLock on id " + objectId + " : " + toRet);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public void unlock(@ValidateStringParam(name = "objectId") String objectId) {
        ReentrantLock nodeLock = null;
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Unlocking id " + objectId);
        }
        synchronized (this) {
            nodeLock = nodeLocks.get(objectId);
        }
        if (nodeLock != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock hold count " + nodeLock.getHoldCount() + " for id " + objectId + " (before unlock)");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock: " + nodeLock.toString());
            }
            nodeLock.unlock();
            if (logger.isDebugEnabled()) {
                logger.debug("[" + Thread.currentThread().getName() + "]" + " Lock hold count " + nodeLock.getHoldCount() + " for id " + objectId + " (after unlock)");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[" + Thread.currentThread().getName() + "]" + " Finished unlocking id " + objectId);
        }

    }

    @Override
    public void lockContentItem(String siteId, String path) {
        lock(generateContentItemKey(siteId, path));
    }

    @Override
    public boolean tryLockContentItem(String siteId, String path) {
        return tryLock(generateContentItemKey(siteId, path));
    }

    @Override
    public void unlockContentItem(String siteId, String path) {
        unlock(generateContentItemKey(siteId, path));
    }

    private String generateContentItemKey(String siteId, String path) {
        return KEY_TEMPLATE_CONTENT_ITEM.replaceAll(PATTERN_SITE, siteId).replaceAll(PATTERN_PATH, path);
    }
}
