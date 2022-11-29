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

    protected Map<String, ReentrantLock> nodeLocks = new HashMap<>();

    @Override
    @ValidateParams
    public void lock(@ValidateStringParam(name = "objectId") String objectId) {
        ReentrantLock nodeLock;
        if (logger.isDebugEnabled()) {
            logger.debug("Thread '{}' will attempt to lock object '{}'", Thread.currentThread().getName(), objectId);
        }
        synchronized (this) {
            if (nodeLocks.containsKey(objectId)) {
                nodeLock = nodeLocks.get(objectId);
            } else {
                nodeLock = new ReentrantLock();
                nodeLocks.put(objectId, nodeLock);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Thread '{}' will attempt to lock object '{}' using nodeLock '{}' with holdCount '{}'",
                    Thread.currentThread().getName(), objectId, nodeLock, nodeLock.getHoldCount());
        }
        nodeLock.lock();
        if (logger.isTraceEnabled()) {
            logger.trace("Thread '{}' has locked object '{}' using nodeLock '{}' with holdCount '{}'",
                    Thread.currentThread().getName(), objectId, nodeLock, nodeLock.getHoldCount());
        }
    }

    @Override
    @ValidateParams
    public boolean tryLock(@ValidateStringParam(name = "objectId") String objectId) {
        ReentrantLock nodeLock;
        if (logger.isDebugEnabled()) {
            logger.debug("Thread '{}' will attempt to tryLock object '{}'", Thread.currentThread().getName(), objectId);
        }
        synchronized (this) {
            if (nodeLocks.containsKey(objectId)) {
                nodeLock = nodeLocks.get(objectId);
            } else {
                nodeLock = new ReentrantLock();
                nodeLocks.put(objectId, nodeLock);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Thread '{}' will attempt to tryLock object '{}' using nodeLock '{}' with holdCount '{}'",
                    Thread.currentThread().getName(), objectId, nodeLock, nodeLock.getHoldCount());
        }
        boolean toRet = nodeLock.tryLock();
        if (logger.isTraceEnabled()) {
            logger.trace("Thread '{}' has completed tryLock on object '{}' using nodeLock '{}' with holdCount '{}'",
                    Thread.currentThread().getName(), objectId, nodeLock, nodeLock.getHoldCount());
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public void unlock(@ValidateStringParam(name = "objectId") String objectId) {
        ReentrantLock nodeLock;
        if (logger.isDebugEnabled()) {
            logger.debug("Thread '{}' will attempt to unlock object '{}'", Thread.currentThread().getName(), objectId);
        }
        synchronized (this) {
            nodeLock = nodeLocks.get(objectId);
        }
        if (nodeLock != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Thread '{}' will attempt to unlock object '{}' using nodeLock '{}' with holdCount '{}'",
                        Thread.currentThread().getName(), objectId, nodeLock, nodeLock.getHoldCount());
            }
            nodeLock.unlock();
            if (logger.isTraceEnabled()) {
                logger.trace("Thread '{}' has completed unlock on object '{}' using nodeLock '{}' with holdCount '{}'",
                        Thread.currentThread().getName(), objectId, nodeLock, nodeLock.getHoldCount());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.error("Thread '{}' is unable to unlock object '{}' since the nodeLock was not found",
                        Thread.currentThread().getName(), objectId);
            }
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
