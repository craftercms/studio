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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.api.SequenceService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.api.TaxonomyService;
import org.craftercms.cstudio.alfresco.service.exception.SequenceException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ModelConfigTO;
import org.craftercms.cstudio.alfresco.to.TaxonomyTO;

/**
 * @author videepkumar1
 * 
 */
public class TaxonomyServiceImpl extends AbstractRegistrableService implements TaxonomyService {

    @Override
    public void register() {
        getServicesManager().registerService(TaxonomyService.class, this);
    }

    /*
    * (non-Javadoc)
    * @see org.craftercms.cstudio.alfresco.service.api.TaxonomyService#updateTaxonomies(java.lang.String, java.util.List)
    */
	public void updateTaxonomies(String site, List<TaxonomyTO> taxonomies) throws ServiceException {
		if (taxonomies != null) {
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            ServicesConfig servicesConfig = getService(ServicesConfig.class);
			for (TaxonomyTO taxonomy : taxonomies) {
				String nodeStr = taxonomy.getNodeRef();
				// if no immediate parent is provided, create it at the top level of the taxonomy type
				Map<QName, ModelConfigTO> configs = servicesConfig.getModelConfig(site);
				QName type = persistenceManagerService.createQName(taxonomy.getType());
				ModelConfigTO config = null;
				if (type != null) {
					config = configs.get(type);
					if (config == null) {
						throw new ServiceException("No model configuration found for " + taxonomy.getType());
					}
				} else {
					throw new ServiceException(type + " is not a valid type.");
				}
				if (StringUtils.isEmpty(nodeStr)) { // create case
					NodeRef nodeRef = createTaxonomy(site, taxonomy.getName(), taxonomy.getParent(), type, config);
					updateProperties(nodeRef, taxonomy, config, true);
				} else { // update
					// if the noderef is valid, update the node properties
					if (NodeRef.isNodeRef(nodeStr)) {
						updateProperties(new NodeRef(nodeStr), taxonomy, config, false);
					}
				}
			}
		}
	}
	
	/**
	 * update node properties with the given taxonomy value
	 * 
	 * @param nodeRef 
	 * 			the target node reference
	 * @param taxonomy
	 * @throws ServiceException 
	 */
    protected void updateProperties(NodeRef nodeRef, TaxonomyTO taxonomy, ModelConfigTO config, boolean created) throws ServiceException {
		try {
            SequenceService sequenceService = getService(SequenceService.class);
			long id = (taxonomy.getId() > 0) ? taxonomy.getId() : sequenceService.next(config.getNamespace(), true);
	    	Map<QName, Serializable> properties = new FastMap<QName, Serializable>();
	    	properties.put(ContentModel.PROP_NAME, taxonomy.getName());
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_LABEL, taxonomy.getLabel());
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_NAMESPACE, config.getNamespace());
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_ID, id);
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_NEW, created);
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_UPDATED, !(!created || taxonomy.isDeleted()));
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_DELETED, taxonomy.isDeleted());
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_CURRENT, taxonomy.isCurrent());
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_ORDER, taxonomy.getOrder());
	    	properties.put(CStudioContentModel.PROP_IDENTIFIABLE_ICON_PATH, taxonomy.getIconPath());
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
	    	persistenceManagerService.setProperties(nodeRef, properties);
		} catch (SequenceException e) {
			throw new ServiceException(e);
		}
	}

    /**
     * create taxonomy node. The parent must be an immediate parent or empty 
     * if it should be created at the top level folder of the taxonomy type. 
     * 
     * @param site
     * @param name
     * @param parent
     * 			the noderef of an immediate parent or empty
     * @param type
     * @param config
     * @return the newly created node reference
     * @throws ServiceException 
     */
	protected NodeRef createTaxonomy(String site, String name, String parent, QName type, ModelConfigTO config) throws ServiceException {
		NodeRef parentRef = null;
		// find the parent node
        SearchService searchService = getService(SearchService.class);
		if (StringUtils.isEmpty(parent)) {
            String path = config.getPath();
            String query = "PATH:\"" + path + "\"";
            parentRef = searchService.findNode(CStudioConstants.STORE_REF, query);
            if (parentRef == null) {
				throw new ServiceException("Failed to create " + name + ". No top level folder found at " + path);
            }
		} else {
			if (NodeRef.isNodeRef(parent)) {
				parentRef = new NodeRef(parent);
			} else {
				throw new ServiceException(parent + " is not a valid noderef.");
			}
 		}
		// create a taxonomy instance under the parent node found
        NamespaceService namespaceService = getService(NamespaceService.class);
        QName assocQName = namespaceService.createContentName(name);
       
        FileInfo folder = getService(PersistenceManagerService.class).create(parentRef, name, type, assocQName);
        return folder.getNodeRef();
	}
}
