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
package org.craftercms.cstudio.alfresco.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.Rendition;
import org.craftercms.cstudio.alfresco.service.api.RenditionContainer;
import org.craftercms.cstudio.alfresco.service.api.RenditionService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.util.ContentUtils;

/**
 * implementation of the rendition service launches a server side javascript
 * which may manipulate any number of renditions within a rendition container
 */
public class RenditionServiceImpl extends AbstractRegistrableService implements RenditionService {

	/**
	 * mapping of beans and services to map in to the scripting environment during renditioning
	 */
	protected Map<String, Object> _scriptObjects;

	/**
	 * company home path
	 */
	protected String companyHomePath;

    @Override
    public void register() {
        getServicesManager().registerService(RenditionService.class, this);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.RenditionService#generateRendition(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.lang.String, org.craftercms.cstudio.alfresco.service.api.RenditionContainer)
      */
	public RenditionContainer generateRendition(NodeRef actionedUponNodeRef, String targetLocation, String scriptLocation, String scriptName,
			RenditionContainer renditionContainer) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		NodeRef spaceRef = persistenceManagerService.getPrimaryParent(actionedUponNodeRef).getParentRef();
		String query = "PATH:\"" + scriptLocation + "/" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + scriptName + "\"";
        SearchService searchService = getService(SearchService.class);
		NodeRef scriptNodeRef = searchService.findNode(CStudioConstants.STORE_REF, query);

		if (scriptLocation != null && (scriptNodeRef != null && persistenceManagerService.exists(scriptNodeRef) == true)) {
           
			String userName = persistenceManagerService.getCurrentUserName();
			NodeRef personRef = persistenceManagerService.getPerson(userName);
			NodeRef homeSpaceRef = (NodeRef) persistenceManagerService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
            
			Map<String, Object> model = persistenceManagerService.buildDefaultModel(personRef, searchService.findNode(
					CStudioConstants.STORE_REF, "PATH:\"/app:" + companyHomePath + "\""), homeSpaceRef, scriptNodeRef,
					actionedUponNodeRef, spaceRef);
			model.put("targetLocation", targetLocation);
			model.put("renditionContainer", renditionContainer);
			model.put("cstudioRenditionService", this);

			for (String scriptObjectName : _scriptObjects.keySet()) {
				model.put(scriptObjectName, _scriptObjects.get(scriptObjectName));				
			}
			
			try {
				persistenceManagerService.executeScript(scriptNodeRef, ContentModel.PROP_CONTENT, model);
			} catch (Exception e) {
				// if any script error, release content streams in all renditions
				List<Rendition> renditions = renditionContainer.getRenditions();
				if (renditions != null) {
					for (Rendition rendition : renditions) {
						InputStream contentStream = rendition.getRenditionContent();
						ContentUtils.release(contentStream);
					}
				}
				renditions.clear();
			}
		}
		return renditionContainer;
	}

	/**
	 * Make it easy for scripting languages to cast strings to input streams
	 * 
	 * @param stringValue
	 *            the value to convert
	 * @param targetEncoding
	 *            the target encoding
	 * @return an input stream for the given string
	 */
	public InputStream stringToInputStream(String stringValue, String targetEncoding) {

		// could rely on a util class but this method is here to make it easy
		// for scripting environments
		InputStream retStream = null;

		try {
			retStream = new ByteArrayInputStream(stringValue.getBytes(targetEncoding));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return retStream;
	}

	/**
	 * @return an empty collection of renditions
	 */
	public RenditionContainer createRenditionContainer() {
		return new RenditionContainerImpl();
	}

	/**
	 * create a rendition object
	 * 
	 * @return a new rendition instance
	 */
	public Rendition createRendition() {
		return new RenditionImpl();
	}

	public void setCompanyHomePath(String companyHomePath) {
		this.companyHomePath = companyHomePath;
	}

	/**
	 * map of objects to add to the scripting environment
	 * @param mapping
	 */
	public void setScriptObjects(Map<String, Object> mapping)
	{
		_scriptObjects = mapping;
	}
}
