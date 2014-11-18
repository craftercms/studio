/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.util.impl.lock;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.craftercms.cstudio.alfresco.dm.util.FileLockService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FileLockServiceImpl extends AbstractRegistrableService implements FileLockService {

    private static final Logger logger = LoggerFactory.getLogger(FileLockServiceImpl.class);

    protected Map<String, String> lockedFiles = new HashMap<String, String>();

    @Override
    public synchronized void lock(AVMNodeDescriptor node, String user) {
        if (node != null) {
            if(isLocked(node)){
                String owner = getLockOwner(node);
                if(!user.equals(owner)){
                    throw new AccessDeniedException("The content is already checked out by " + owner + ".");
                }
            }
            lockedFiles.put(node.getPath(), user);
            if (logger.isDebugEnabled()) {
                logger.debug("[" + user + "] locked file [" + node.getPath() + "]");
            }
        }
    }

    @Override
    public void register() {
        getServicesManager().registerService(FileLockService.class, this);
    }

    @Override
    public boolean isLocked(AVMNodeDescriptor node) {
        return node != null && lockedFiles.containsKey(node.getPath());
    }

    @Override
    public String getLockOwner(AVMNodeDescriptor node) {
        if (node != null) {
            String owner = lockedFiles.get(node.getPath());
            if (logger.isDebugEnabled()) {
                logger.debug("getting the lock owner of " + node.getPath() + ": " + owner);
            }
            return owner;
        } else {
            return null;
        }
    }

    @Override
    public synchronized void unlockLock(AVMNodeDescriptor node) {
        if (node != null) {
            lockedFiles.remove(node.getPath());
            if (logger.isDebugEnabled()) {
                logger.debug("unlocked file [" + node.getPath() + "]");
            }
        }
    }

    @Override
    public synchronized void unlockLock(String fullpath) {
        lockedFiles.remove(fullpath);
        if (logger.isDebugEnabled()) {
            logger.debug("unlocked file [" + fullpath + "]");
        }
    }
}
