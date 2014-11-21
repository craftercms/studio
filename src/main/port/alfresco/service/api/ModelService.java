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
package org.craftercms.cstudio.alfresco.service.api;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Document;

import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.InvalidTypeException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ModelDataTO;
import org.craftercms.cstudio.alfresco.to.TaxonomyTypeTO;

/**
 * Provides content model related configuration and data. 
 * 
 * @author hyanghee
 *
 */
public interface ModelService {


	/**
	 * given a path get model data including child association
	 * 
	 * @param site
	 *            site
	 * @param modelName
	 *            model Name
	 * @param currentOnly
	 * 			include current models only? The model must have a 'cstudio-core:isCurrent' property 
	 * @param start 
	 * @param end 
	 * @return a list of model data
	 */
	public List<ModelDataTO> getModelData(String site, String modelName, boolean currentOnly, int start, int end);
	
	
	/**
	 * return a template of model as XML document 
	 * 
	 * @param site
	 * @param contentType
	 * 		content type
	 * @param includeDataType
	 * 			add java class attribute name to property elements if true
	 * @param addEmptyListDataTemplate 
	 * 			populate an empty data template in the list?
	 * @return model template in XML
	 * @throws InvalidTypeException 
	 * @throws ContentNotFoundException 
	 */
	public Document getModelTemplate(String site, String contentType, boolean includeDataType, 
			boolean addEmptyListDataTemplate) throws InvalidTypeException, ContentNotFoundException;


	/**
	 * given a site, return all valid taxonomies
	 *  
	 * @param site
	 * @return taxonomies metadata
	 * @throws ServiceException
	 */
	public List<TaxonomyTypeTO> getTaxonomies(String site) throws ServiceException;
	
	/**
	 * given a site, return all nodeRefs
	 * 
	 * @param site
	 * @return a list of all taxonomy nodeRefs from the site
	 * @throws ServiceException
	 */
	public List<String> getTaxonomiesNodeRefs(String site) throws ServiceException;
	
	/**
	 * get static site model data from files by the given site and the key
	 * 
	 * @param site
	 * @param key
	 * @return static site data
	 * @throws ServiceException 
	 */
	public String getStaticModelData(String site, String key) throws ServiceException;

	/**
	 * get the template version of the given content type
	 * 
	 * @param site
	 * @param contentType
	 * @return the template version
	 */
	public String getTemplateVersion(String site, String contentType);
	
}
