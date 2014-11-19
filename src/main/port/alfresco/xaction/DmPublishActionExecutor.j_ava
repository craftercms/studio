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
package org.craftercms.cstudio.alfresco.action;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.dm.util.api.DmImportService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmPublishActionExecutor extends ActionExecuterAbstractBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(DmPublishActionExecutor.class);
	
    /** action name **/
    public static final String NAME = "dm-publish-action";
    
    public static final String PARAM_SITE = "site";
    public static final String PARAM_PUBLISH_CHANNEL_GROUP = "publishChannelGroup";
    public static final String PARAM_TARGET_REF = "targetRef";
    public static final String PARAM_TARGET_LOCATION = "targetLocation";
    public static final String PARAM_START_PATH = "startPath";
    public static final String PARAM_CHUNK_SIZE = "chunkSize";
       
    /**
     * Services Manager
     */
    protected ServicesManager servicesManager;
	
    /** 
     * import serivce 
     */
    protected DmImportService importService;
    
	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
		// nodeRef is the target wem-project root
		String site = (String) action.getParameterValue(PARAM_SITE);
		String publishChannelGroup = (String) action.getParameterValue(PARAM_PUBLISH_CHANNEL_GROUP);
		String targetLocation = (String) action.getParameterValue(PARAM_TARGET_LOCATION);
		String startPath = (String) action.getParameterValue(PARAM_START_PATH);
		int chunkSize = (Integer) action.getParameterValue(PARAM_CHUNK_SIZE);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("[IMPORT] starting publishing " + site + ", publishing channel group: " +  publishChannelGroup
					+ ", target wem-project root: " + nodeRef + " chunk size: " + chunkSize);
		}
		this.importService.publishImprotedContents(site, publishChannelGroup, targetLocation, nodeRef, startPath, chunkSize);
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_SITE, DataTypeDefinition.TEXT, true,
				getParamDisplayLabel(PARAM_SITE)));
		paramList.add(new ParameterDefinitionImpl(PARAM_PUBLISH_CHANNEL_GROUP, DataTypeDefinition.TEXT, true,
				getParamDisplayLabel(PARAM_PUBLISH_CHANNEL_GROUP)));
		paramList.add(new ParameterDefinitionImpl(PARAM_TARGET_LOCATION, DataTypeDefinition.TEXT, true,
				getParamDisplayLabel(PARAM_TARGET_LOCATION)));
		paramList.add(new ParameterDefinitionImpl(PARAM_START_PATH, DataTypeDefinition.TEXT, true,
				getParamDisplayLabel(PARAM_START_PATH)));
		paramList.add(new ParameterDefinitionImpl(PARAM_CHUNK_SIZE, DataTypeDefinition.INT, true,
				getParamDisplayLabel(PARAM_CHUNK_SIZE)));
	}

	/**
	 * @return the servicesManager
	 */
	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	/**
	 * @param servicesManager the servicesManager to set
	 */
	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	/**
	 * @return the importService
	 */
	public DmImportService getImportService() {
		return importService;
	}

	/**
	 * @param importService the importService to set
	 */
	public void setImportService(DmImportService importService) {
		this.importService = importService;
	}

}
