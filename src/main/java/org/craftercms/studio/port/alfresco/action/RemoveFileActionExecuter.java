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
package org.craftercms.cstudio.alfresco.action;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes the actioned node
 */
public class RemoveFileActionExecuter extends ActionExecuterAbstractBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveFileActionExecuter.class);

    /** action name **/
    public static final String NAME = "remove-file-action";
    
    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /*
    * (non-Javadoc)
    *
    * @seeorg.alfresco.repo.action.ParameterizedItemAbstractBase#
    * addParameterDefinitions(java.util.List)
    */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    }

    /**
     * Deletes the inbound node. 
     * @see
     * org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl
     * (org.alfresco.service.cmr.action.Action,
     * org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
    	persistenceManagerService.deleteNode(nodeRef);
    }
    
    
}
