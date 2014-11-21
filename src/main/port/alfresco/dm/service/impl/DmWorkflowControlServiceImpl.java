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
package org.craftercms.cstudio.alfresco.dm.service.impl;


import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowControlService;
import org.craftercms.cstudio.alfresco.dm.util.FileLockService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmWorkflowControlServiceImpl extends AbstractRegistrableService implements DmWorkflowControlService {

    private static final Logger logger = LoggerFactory.getLogger(DmWorkflowControlServiceImpl.class);


    @Override
    public void register() {
        getServicesManager().registerService(DmWorkflowControlService.class, this);
    }

    public void unlock(String site, String path) {
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, path);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        if (node == null) {
            // Node does not exist (e.g. Cancel a create-new-content page).
            return;
        }
        persistenceManagerService.setSystemProcessing(node, true);
        FileLockService fileLockService = getService(FileLockService.class);
        fileLockService.unlockLock(fullPath);
        try {
            persistenceManagerService.unlock(fullPath);
            persistenceManagerService.transition(node, ObjectStateService.TransitionEvent.UNLOCK);
        } catch (UnableToReleaseLockException e) {
            logger.error("Unable to release lock for content at site: " + site + " path: " + path, e);
        } finally {
            persistenceManagerService.setSystemProcessing(node, false);
        }
    }
}
