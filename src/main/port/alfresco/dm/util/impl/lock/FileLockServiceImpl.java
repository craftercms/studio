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
    public void register() {
        getServicesManager().registerService(FileLockService.class, this);
    }


    @Override
    public synchronized void unlockLock(String fullpath) {
        lockedFiles.remove(fullpath);
        if (logger.isDebugEnabled()) {
            logger.debug("unlocked file [" + fullpath + "]");
        }
    }
}
