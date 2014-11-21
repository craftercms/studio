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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.ModelService;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.InvalidTypeException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.to.ModelConfigTO;
import org.craftercms.cstudio.alfresco.to.ModelDataTO;
import org.craftercms.cstudio.alfresco.to.TaxonomyTO;
import org.craftercms.cstudio.alfresco.to.TaxonomyTypeTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ModelService implementation for DM
 * 
 * @author hyanghee
 * 
 */
public class ModelServiceImpl extends AbstractRegistrableService implements ModelService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceImpl.class);
	
	/** 
	 * site static model data path
	 */
	protected String _siteDataPath;

    /**
     * @param siteDataPath the siteDataPath to set
     */
    public void setSiteDataPath(String siteDataPath) {
        this._siteDataPath = siteDataPath;
    }

    @Override
    public void register() {
        getServicesManager().registerService(ModelService.class, this);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ModelService#getTemplateVersion(java.lang.String, java.lang.String)
      */
	public String getTemplateVersion(String site, String contentType) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
		ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
        if (!DmConstants.CONTENT_TYPE_CONFIG_FORM_PATH_SIMPLE.equalsIgnoreCase(config.getFormPath())) {
            CStudioNodeService cStudioNodeService = getService(CStudioNodeService.class);
            NodeRef nodeRef = cStudioNodeService.getNodeRef(config.getModelInstancePath());
            if (nodeRef != null) {

                return (String) getService(PersistenceManagerService.class).getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
            } else {
                if(LOGGER.isWarnEnabled()) {
                    LOGGER.warn("the template path of " + contentType + " " + config.getModelInstancePath() + " does not exsit in " + site);
                }
                return null;
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Simple form engine does not use model templates");
            }
            return null;
        }
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ModelService#getModelTemplate(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public Document getModelTemplate(String site, String contentType, boolean includeDataType, boolean addEmptyListDataTemplate) throws InvalidTypeException, ContentNotFoundException {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
		ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
		if (config != null) {
			String path = config.getModelInstancePath();
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef templateNodeRef = persistenceManagerService.getNodeRef(path);
			if (templateNodeRef != null) {
				return persistenceManagerService.loadXml(templateNodeRef);
			} else {
				throw new ContentNotFoundException("no template instance found at " + path);
			}
		} else {
			throw new InvalidTypeException(contentType + " is not found in " + site);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ModelService#getStaticModelData(java.lang.String, java.lang.String)
	 */
	public String getStaticModelData(String site, String key) throws ServiceException {
		if (!StringUtils.isEmpty(_siteDataPath)) {
			// find the data file by replacing site and key values in the path
			String path = _siteDataPath.replaceAll(CStudioConstants.PATTERN_SITE, site).replaceAll(CStudioConstants.PATTERN_KEY, key);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef nodeRef = persistenceManagerService.getNodeRef(path);
			if (nodeRef != null) {
				return persistenceManagerService.getContentAsString(nodeRef);
			} else {
				throw new ServiceException("Failed to get static model data. No static model data found at " + path);
			}
		} else {
			throw new ServiceException("Failed to get static model data. Site data path is not configured.");
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ModelService#getModelData(java.lang.String, java.lang.String, boolean, int, int)
	 */
	public List<ModelDataTO> getModelData(String site, String modelName, boolean currentOnly, int start, int end) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
		Map<QName, ModelConfigTO> models = servicesConfig.getModelConfig(site);
		if (models != null) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			QName modelQName = persistenceManagerService.createQName(modelName);
			ModelConfigTO config = models.get(modelQName);
			if (config != null) {
				String path = config.getPath();
				int depth = config.getDepth();
				if (end > 0 && end <= depth) {
					depth = end;
				}
				start = (start <= 0 && start > depth) ? 1 : start;
                SearchService searchService = getService(SearchService.class);
				NodeRef parentRef = searchService.findNode(CStudioConstants.STORE_REF, "PATH:\"" + path + "\"");
				if (parentRef == null) {
					return null;
				} else {
					QName modelType = persistenceManagerService.createQName(modelName);
					Set<QName> childTypes = new HashSet<QName>(1);
					if (modelType != null) {
						childTypes.add(modelType);
					}
                    
					List<ChildAssociationRef> children = persistenceManagerService.getChildAssocs(parentRef, childTypes);
					int curr = 1;
					return createModelData(curr, start, depth, children, currentOnly);
				}
			}
		}
		return null;
	}

	/**
	 * create model data given the model data
	 * 
	 * @param curr
	 * 			current level 
	 * @param start
	 * @param depth
	 * @param modelRefs
	 * @param currentOnly
	 * @return
	 */
	protected List<ModelDataTO> createModelData(int curr, int start, int depth, List<ChildAssociationRef> modelRefs, boolean currentOnly) {
		if (modelRefs != null && modelRefs.size() > 0) {
			List<ModelDataTO> modelData = new ArrayList<ModelDataTO>(modelRefs.size());
            NamespaceService namespaceService = getService(NamespaceService.class);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			for (ChildAssociationRef modelRef : modelRefs) {
				NodeRef childRef = modelRef.getChildRef();
				boolean addModelData = true;
				if (currentOnly) {
					Serializable isCurrent = (Serializable)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_IS_CURRENT);
					// check only if the model has the isCurrent property
					if (isCurrent != null) {
						addModelData = ((Boolean)isCurrent);
					}
				} 
				if (addModelData) {
					if (curr >= start) {
						ModelDataTO model = new ModelDataTO();
						Long idProp = (Long)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_IDENTIFIABLE_ID);
						long id = (idProp != null) ? idProp.longValue() : -1L;
						QName type = persistenceManagerService.getType(childRef);
						String modelType = namespaceService.getPrefixedTypeName(type);
						String description = (String)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_LABEL);
						String name = (String)persistenceManagerService.getProperty(childRef, ContentModel.PROP_NAME);
						description = (description == null) ? "" : description ;
						Long orderProp = (Long)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_IDENTIFIABLE_ORDER);
						long order = (orderProp != null) ? orderProp.longValue() : -1L;
						boolean deleted = false; //(Boolean) _nodeService.getProperty(childRef, CStudioContentModel.PROP_IDENTIFIABLE_DELETED);
						
						model.setDescription(description);
						model.setType(modelType);
						model.setLabel(name);
						model.setValue(childRef.toString());
						model.setDeleted(deleted);
						model.setOrder(order);
						model.setId(id);
						
						if (depth > 1) {
							List<ChildAssociationRef> childModelRefs = persistenceManagerService.getChildAssocs(childRef);
							List<ModelDataTO> children = createModelData(curr + 1, start, depth - 1, childModelRefs, currentOnly);
							model.setChildren(children);
						}
						modelData.add(model);
					} else {
						if (depth > 1) {
							List<ChildAssociationRef> childModelRefs = persistenceManagerService.getChildAssocs(childRef);
							List<ModelDataTO> children = createModelData(curr + 1, start, depth - 1, childModelRefs, currentOnly);
							modelData.addAll(children);
							//model.setChildren(children);
						}
					}
				}
			}
			return modelData;
		}
		return null;
	}
	
	
	/**
	 * get taxonomies metadata
	 * 
	 * @param site
	 */
	@SuppressWarnings("unchecked")
	public List<TaxonomyTypeTO> getTaxonomies(String site) throws ServiceException {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
		Map<QName, ModelConfigTO> models = servicesConfig.getModelConfig(site);
		if (models != null) {
			List<TaxonomyTypeTO> taxonomyTypes = new ArrayList<TaxonomyTypeTO>(models.size());			
			Iterator modelIterator = models.entrySet().iterator();
            SearchService searchService = getService(SearchService.class);
			while (modelIterator.hasNext()) {
		        Map.Entry<QName, ModelConfigTO> pairs = (Map.Entry<QName, ModelConfigTO>) modelIterator.next();
		        QName modelQName = (QName) pairs.getKey();
		        ModelConfigTO modelConfig = (ModelConfigTO) pairs.getValue();
		        if (modelConfig != null) {		        	
					String path = modelConfig.getPath();
					String displayName = modelConfig.getDisplayName();					
					int depth = modelConfig.getDepth();								

					NodeRef parentRef = searchService.findNode(CStudioConstants.STORE_REF, "PATH:\"" + path + "\"");
					
					
					
					TaxonomyTypeTO taxonomyType = createTaxonomyType(modelQName, displayName, depth, parentRef);					
					taxonomyTypes.add(taxonomyType);					
				}
		    }
			return taxonomyTypes;
		}
		return null;
	}
	
	/**
	 * create child elements of the given taxonomy
	 * 
	 * @param modelQName
	 * @param displayName
	 * @param depth
	 * @param parentRef
	 * 
	 * @return taxonomy type
	 */
	protected TaxonomyTypeTO createTaxonomyType(QName modelQName, String displayName, int depth, NodeRef parentRef) {
		TaxonomyTypeTO taxonomyType = new TaxonomyTypeTO();
		taxonomyType.setName(displayName);		
		taxonomyType.setTerms(createTaxonomies(modelQName, depth, parentRef, true));		
		return taxonomyType;
	}
	
	/**
	 * Create taxonomies for a given type
	 * 
	 * @param modelQtype
	 * @param depth
	 * @param parentRef
	 * 
	 * 
	 * @return
	 */
	protected List<TaxonomyTO> createTaxonomies(QName modelQtype, int depth, NodeRef parentRef, boolean firstTime) {
		if (parentRef == null) {
			return null;
		} else {
			List<TaxonomyTO> taxonomies = null;
				
			List<ChildAssociationRef> children = null;
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			if (firstTime) {
				Set<QName> childTypes = new HashSet<QName>(1);
				if (modelQtype != null) {
					childTypes.add(modelQtype);
				}
				children = persistenceManagerService.getChildAssocs(parentRef, childTypes);
			} else {
				children = persistenceManagerService.getChildAssocs(parentRef);
			}
			if (children != null) {
				taxonomies = new ArrayList<TaxonomyTO>(children.size());
                NamespaceService namespaceService = getService(NamespaceService.class);
				for (ChildAssociationRef child : children) {
					NodeRef childRef = child.getChildRef();					
					String name = (String)persistenceManagerService.getProperty(childRef, ContentModel.PROP_NAME);
					QName childQType = persistenceManagerService.getType(childRef);
					String type = namespaceService.getPrefixedTypeName(childQType);
					Long id = (Long)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_IDENTIFIABLE_ID);
					String nodeRef = childRef.toString();
					Boolean created = (Boolean)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_IDENTIFIABLE_NEW);
					Boolean updated = (Boolean)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_IDENTIFIABLE_UPDATED);
					Boolean deleted = (Boolean)persistenceManagerService.getProperty(childRef, CStudioContentModel.PROP_IDENTIFIABLE_DELETED);
					
					TaxonomyTO taxonomy = new TaxonomyTO();
					taxonomy.setName(name);
					taxonomy.setType(type);
					taxonomy.setId(id);
					taxonomy.setNodeRef(nodeRef);
					taxonomy.setCreated(created);
					taxonomy.setUpdated(updated);
					taxonomy.setDeleted(deleted);
					
					List<TaxonomyTO> childrenTaxonomies = null;
					if (depth > 1) {
						childrenTaxonomies = createTaxonomies(childQType, depth - 1, childRef, false);
					}
					taxonomy.setChildren(childrenTaxonomies);
					taxonomies.add(taxonomy);
				}
			}
			
			return taxonomies;
		}
	}
	
	
	/**
	 * get nodeRef for taxonomies in a site
	 * 
	 * @param site
	 */
	@SuppressWarnings("unchecked")
	public List<String> getTaxonomiesNodeRefs(String site) throws ServiceException {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
		Map<QName, ModelConfigTO> models = servicesConfig.getModelConfig(site);
		if (models != null) {
			List<String> taxonomies = new ArrayList<String>();			
			Iterator modelIterator = models.entrySet().iterator();
            SearchService searchService = getService(SearchService.class);
			while (modelIterator.hasNext()) {
		        Map.Entry<QName, ModelConfigTO> pairs = (Map.Entry<QName, ModelConfigTO>) modelIterator.next();
		        ModelConfigTO modelConfig = (ModelConfigTO) pairs.getValue();
		        if (modelConfig != null) {		        	
					String path = modelConfig.getPath();
					int depth = modelConfig.getDepth();								
					if(path == null || path.isEmpty() || "".equals(path))
						continue;
					NodeRef parentRef = searchService.findNode(CStudioConstants.STORE_REF, "PATH:\"" + path + "\"");
					
					List<String> taxonomy = getTaxonomyNodeRefs(parentRef, depth);					
					if (taxonomy != null)
						taxonomies.addAll(taxonomy);					
				}
		    }
			return taxonomies;
		}
		return null;
	}
	
	/**
	 * get nodeRef for a particular taxonomy in a site
	 * 
	 * @param parentRef
	 * @param depth
	 * @return
	 */
	protected List<String> getTaxonomyNodeRefs(NodeRef parentRef, int depth) {
		List<String> taxonomy = null;
		
		if (depth > 0) {
			taxonomy = new ArrayList<String>();
			List<ChildAssociationRef> children = getService(PersistenceManagerService.class).getChildAssocs(parentRef);
			if (children != null) {
				for (ChildAssociationRef child : children) {
					NodeRef childRef = child.getChildRef();					
					String nodeRef = childRef.toString();
					taxonomy.add(nodeRef);

					List<String> childTaxonomy = getTaxonomyNodeRefs(childRef, depth-1);
					if (childTaxonomy != null)
						taxonomy.addAll(childTaxonomy);
				}
			}			
		}
		
		return taxonomy;
	}
}
