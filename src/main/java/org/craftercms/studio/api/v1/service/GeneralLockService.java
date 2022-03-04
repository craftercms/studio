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
package org.craftercms.studio.api.v1.service;

public interface GeneralLockService {

    String MASTER_LOCK = "MASTER LOCK";

    void lock(String objectId);

    void unlock(String objectId);

    boolean tryLock(String objectId);

    /**
     * Lock content item for synchronized access. Thread is blocked until lock is obtained.
     *
     * @param siteId site identifier
     * @param path content item path
     */
    void lockContentItem(String siteId, String path);

    /**
     * Try to lock item for synchronized access. If lock obtained returns true, otherwise false. Does not block
     * thread if not available lock.
     * @param siteId
     * @param path
     * @return
     */
    boolean tryLockContentItem(String siteId, String path);

    /**
     * Release lock on content item.
     *
     * @param siteId site identifier
     * @param path path of the content item
     */
    void unlockContentItem(String siteId, String path);
}
