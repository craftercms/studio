/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;

public class GeneralLockServiceImpl implements GeneralLockService {

    private static final Logger logger = LoggerFactory.getLogger(GeneralLockServiceImpl.class);

    private static final String KEY_FORMAT_CONTENT_ITEM = "CONTENT_ITEM_%s_%s";

    protected Map<String, ReentrantLock> nodeLocks = new HashMap<>();

    @Override
    @Valid
    public void lock(@ValidateStringParam String objectId) {
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
    @Valid
    public boolean tryLock(@ValidateStringParam String objectId) {
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
    @Valid
    public void unlock(@ValidateStringParam String objectId) {
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
        return format(KEY_FORMAT_CONTENT_ITEM, siteId, path);
    }
}
