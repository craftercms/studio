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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.ContentTypesConfig;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.to.ContentTypePathTO;
import org.craftercms.cstudio.alfresco.to.CopyDependencyConfigTO;
import org.craftercms.cstudio.alfresco.to.DeleteDependencyConfigTO;
import org.craftercms.cstudio.alfresco.to.SearchColumnTO;
import org.craftercms.cstudio.alfresco.to.SearchConfigTO;
import org.craftercms.cstudio.alfresco.to.SiteContentTypePathsTO;
import org.craftercms.cstudio.alfresco.to.TimeStamped;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentTypesConfigImpl extends ConfigurableServiceBase implements ContentTypesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypesConfigImpl.class);
	
	/** a map of keys (site,content-type-name) and their configuration **/
	protected Map<String, ContentTypeConfigTO> _contentTypeMap = new FastMap<String, ContentTypeConfigTO>();

	/** a map of noderefs as string and keys (site, content-type-name) **/
	protected Map<String, String> _contentTypeNodeMap = new FastMap<String, String>();
	
    /**
     * a list of content type path configuration
     */
    protected Map<String, SiteContentTypePathsTO> _pathMapping = new FastMap<String, SiteContentTypePathsTO>();
    
    @Override
    public void register() {
        this._servicesManager.registerService(ContentTypesConfig.class, this);
    }

	@Override
	public SiteContentTypePathsTO getPathMapping(String site) {
		return _pathMapping.get(site);
	}


    /*
     * (non-Javadoc)
     * @see org.craftercms.cstudio.alfresco.service.api.ContentTypeConfig#getContentTypeConfig(java.lang.String, java.lang.String)
     */
	public ContentTypeConfigTO getContentTypeConfig(String key) {
		checkForUpdate(key);
		return _contentTypeMap.get(key);
	}

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.service.api.ContentTypeConfig#getContentTypeConfig(java.lang.String, java.lang.String)
      */
	public ContentTypeConfigTO getContentTypeConfig(String site, String contentType) {
		String key = createKey(site, contentType);
		checkForUpdate(key);
		return _contentTypeMap.get(key);
	}

	/**
	 * create configuration key using the site and the content type given
	 * : or / in content type name will be replace with - 
	 * 
	 * @param site
	 * @param contentType
	 * @return
	 */
	protected String createKey(String site, String contentType) {
		//contentType = ContentUtils.getContentTypeKey(contentType);
		return site + "," + contentType;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#getConfigRef(java.lang.String)
	 */
	protected NodeRef getConfigRef(String key) {
		if (!StringUtils.isEmpty(key)) {
			// key is a combination of site,content-type
			String [] keys = key.split(",");
			if (keys.length == 2) {
				String site = keys[0];
				String contentType = keys[1];
				String siteConfigPath = _configPath.replaceAll(CStudioConstants.PATTERN_SITE, site)
											.replaceAll(CStudioConstants.PATTERN_CONTENT_TYPE, contentType);

                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                return persistenceManagerService.getNodeRef(siteConfigPath + "/" + _configFileName);
			} else {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Invalid content type config key provided: " + key + " site, content type is expected.");
				}
			}
		} else {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Key cannot be empty. site, content type is expected.");
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#getConfiguration(java.lang.String)
	 */
	protected TimeStamped getConfiguration(String key) {
		return _contentTypeMap.get(key);
	}

    /*
     * (non-Javadoc)
     * @see org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase#loadConfiguration(java.lang.String)
     */
	protected void loadConfiguration(String key) {
		NodeRef configRef = getConfigRef(key);
		if (configRef != null) {
			ContentTypeConfigTO contentTypeConfig = loadConfigurationFile(configRef);
			this.addContentType(key, configRef, contentTypeConfig);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.ContentTypesConfig#loadConfiguration(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
	 */
	public ContentTypeConfigTO loadConfiguration(String site, NodeRef nodeRef) {
		String key = _contentTypeNodeMap.get(nodeRef.toString());
		// if key is found, check the timestamp 
		if (!StringUtils.isEmpty(key)) {
			ContentTypeConfigTO contentTypeConfig = _contentTypeMap.get(key);
			if (contentTypeConfig != null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			    Serializable modifiedDateVal = persistenceManagerService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
                if (modifiedDateVal != null) {
	                Date modifiedDate = (Date) modifiedDateVal;
				    if (! (modifiedDate.after(contentTypeConfig.getLastUpdated()))) {
				    	// if the node modified date is not after the timestamp, no need to load again
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Skipping loading " + key + " since it is previsouly loaded and no change was made.");
						}
				    	return contentTypeConfig;
				    }
                }
			}
		} 
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Loading configuration from " + nodeRef + " since it is not loaded or configuration file is updated.");
		}
		// otherwise load the configuration file
		ContentTypeConfigTO contentTypeConfig = loadConfigurationFile(nodeRef);
		key = this.createKey(site, contentTypeConfig.getName());
		this.addContentType(key, nodeRef, contentTypeConfig);
		return contentTypeConfig;
	}

	/**
	 * add a content type to the mapping
	 * 
	 * @param key
	 * @param nodeRef
	 * @param contentTypeConfig
	 */
	protected void addContentType(String key, NodeRef nodeRef, ContentTypeConfigTO contentTypeConfig) {
		if (!StringUtils.isEmpty(key) && contentTypeConfig != null) {
			if (_contentTypeMap.get(key) != null) {
				removeConfiguration(key);
			}
			_contentTypeMap.put(key, contentTypeConfig);
			_contentTypeNodeMap.put(nodeRef.toString(), key);
			addToPathMapping(key, contentTypeConfig);
		}
	}
	
	/**
	 * remove configuration
	 * 
	 * @param key
	 */
	@Override
    protected void removeConfiguration(String key) {
        if (!StringUtils.isEmpty(key)) {
        	ContentTypeConfigTO contentTypeConfig = _contentTypeMap.get(key);
        	if (contentTypeConfig != null) {
        		_contentTypeMap.remove(key);
        		_contentTypeNodeMap.remove(contentTypeConfig.getNodeRef());
        		removeFromPathMapping(key, contentTypeConfig);
        	}
        }
    }

	/**
	 * remove configuration from path mapping
	 * 
	 * @param key
	 * @param contentTypeConfig
	 */
	protected void removeFromPathMapping(String key, ContentTypeConfigTO configToRemove) {
    	String [] values = key.split(",");
    	String site = values[0];
    	SiteContentTypePathsTO paths = this._pathMapping.get(site);
    	if (paths != null) {
			for (String pathInclude : configToRemove.getPathIncludes()) {
	    		for (ContentTypePathTO pathTO : paths.getConfigs()) {
	    			if (pathTO.getPathInclude().equalsIgnoreCase(pathInclude)) {
    	    			pathTO.removeAllowedContentTypes(key);
	    			}
	    		}
			}
            paths.setLastUpdated(new Date());
    	}
	}

	/**
	 * add configuration to path mapping 
	 * 
	 * @param key
	 * @param contentTypeConfig
	 */
    protected void addToPathMapping(String key, ContentTypeConfigTO configToAdd) {
    	if (LOGGER.isDebugEnabled()) {
    		LOGGER.debug("Adding a path configuration to mapping with key: " + key);
    	}
    	String [] values = key.split(",");
    	String site = values[0];
    	SiteContentTypePathsTO paths = this._pathMapping.get(site);
    	if (paths != null) {
    		boolean added = false;
    		// find a matching path configuration and add
			for (String pathInclude : configToAdd.getPathIncludes()) {
	    		for (ContentTypePathTO pathTO : paths.getConfigs()) {
	    			if (pathTO.getPathInclude().equalsIgnoreCase(pathInclude)) {
	    	        	if (LOGGER.isDebugEnabled()) {
	    	        		LOGGER.debug("Adding " + key + " to " + pathInclude);
	    	        	}
	    	    		pathTO.addToAllowedContentTypes(key);
	    				added = true;
	    			}
	    		}
	    		// if no same pathInclude found, create a new one
	    		if (!added) {
    	        	if (LOGGER.isDebugEnabled()) {
    	        		LOGGER.debug("Creating a new include for " + key + " with " + pathInclude);
    	        	}
					ContentTypePathTO pathTO = createNewPathConfig(pathInclude, key, configToAdd);
					paths.getConfigs().add(pathTO);
	    		}
			}
			paths.setLastUpdated(new Date());
    	} else {
        	if (LOGGER.isDebugEnabled()) {
        		LOGGER.debug("No configuration exists. adding a new record.");
        	}
    		// add new content type path mapping
    		SiteContentTypePathsTO newPaths = new SiteContentTypePathsTO();
    		List<String> pathIncludes = configToAdd.getPathIncludes();
    		List<ContentTypePathTO> configs = new FastList<ContentTypePathTO>();
			for (String pathInclude : pathIncludes) {
				ContentTypePathTO pathTO = createNewPathConfig(pathInclude, key, configToAdd);
				configs.add(pathTO);
			}
    		newPaths.setConfigs(configs);
    		newPaths.setLastUpdated(new Date());
    		this._pathMapping.put(site, newPaths);
    	}
	}

    /**
     * create a new path configuration
     * 
     * @param pathInclude
     * @param key
     * @param config
     * @return
     */
	private ContentTypePathTO createNewPathConfig(String pathInclude, String key, ContentTypeConfigTO config) {
		ContentTypePathTO pathTO = new ContentTypePathTO();
		pathTO.setPathInclude(pathInclude);
		pathTO.addToAllowedContentTypes(key);
		return pathTO;
	}

	/**
	 * load configuration from the configuration noderef
	 * @param configRef
	 */
	@SuppressWarnings("unchecked")
	protected ContentTypeConfigTO loadConfigurationFile(NodeRef configRef) {
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		Document document = persistenceManagerService.loadXml(configRef);
		if (document != null) {
			Element root = document.getRootElement();
			String name = root.valueOf("@name");
			ContentTypeConfigTO contentTypeConfig = new ContentTypeConfigTO();
			contentTypeConfig.setName(name);
			contentTypeConfig.setNodeRef(configRef.toString());
			contentTypeConfig.setLabel(root.valueOf("label"));
			String imageThumbnail=root.valueOf("image-thumbnail");
			if(imageThumbnail != null)
				contentTypeConfig.setImageThumbnail(imageThumbnail);
			contentTypeConfig.setForm(root.valueOf("form"));
			boolean previewable = ContentFormatUtils.getBooleanValue(root.valueOf("previewable"));
			contentTypeConfig.setFormPath(root.valueOf("form-path"));
			contentTypeConfig.setPreviewable(previewable);
			contentTypeConfig.setModelInstancePath(root.valueOf("model-instance-path"));
			boolean contentAsFolder = ContentFormatUtils.getBooleanValue(root.valueOf("content-as-folder"));
			contentTypeConfig.setContentAsFolder(contentAsFolder);
			boolean useRoundedFolder = ContentFormatUtils.getBooleanValue(root.valueOf("use-rounded-folder"));
			contentTypeConfig.setUseRoundedFolder(useRoundedFolder);
			List<String> pathIncludes = getPaths(root, "paths/includes/pattern");
			if (pathIncludes.size() == 0) {
				// if no configuration, include every path
				pathIncludes.add(".*");
			}
			contentTypeConfig.setPathIncludes(pathIncludes);
			List<String> pathExcludes = getPaths(root, "paths/excludes/pattern");
			contentTypeConfig.setPathExcludes(pathExcludes);
			//(contentTypeConfig, root.selectNodes("allowed-roles/role"));
			loadRoles(contentTypeConfig, root.selectNodes("allowed-roles/role"));
			loadDeleteDependencies(contentTypeConfig, root.selectNodes("delete-dependencies/delete-dependency"));
			loadCopyDependencyPatterns(contentTypeConfig, root.selectNodes("copy-dependencies/copy-dependency"));
			contentTypeConfig.setNoThumbnail(ContentFormatUtils.getBooleanValue(root.valueOf("noThumbnail")));
			SearchConfigTO searchConfig = loadSearchConfig(root.selectSingleNode("search"));
			contentTypeConfig.setSearchConfig(searchConfig);
			contentTypeConfig.setLastUpdated(new Date());
			contentTypeConfig.setType(getContentTypeTypeByName(name));
			return contentTypeConfig;
		} else {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("No content type configuration document found at " + configRef);
			}
			return null;
		}
	}

	/**
	 * Checks name for naming convention.
	 * @param name Name to be check
	 * @return <ul>
	 * <li><b>component</b> if the name matches component naming convention</li>
	 * <li><b>page</b> if the name matches page naming convention</li>
	 * <li><b>unknown</b> if name don't match any known convention</li>
	 * </ul>
	 */
	private String getContentTypeTypeByName(String name) {
		if (Pattern.matches("/component/.*?", name)) {
			return "component";
		} else if (Pattern.matches("/page/.*?", name))
			return "page";
		else {
			return "unknown";
		}
	}

	/**
	 * get paths 
	 * 
	 * @param root
	 * @param path
	 * @return
	 */
	private List<String> getPaths(Element root, String path) {
		List<String> paths = null;
		List<Node> nodes = root.selectNodes(path);
		if (nodes != null && nodes.size() > 0) {
			paths = new FastList<String>(nodes.size());
			for (Node node : nodes) {
				String role = node.getText();
				if (!StringUtils.isEmpty(role)) {
					paths.add(role);
				}
			}
		} else {
			paths = new FastList<String>();
		}
		return paths;
	}

	/**
	 * load a list of allowed roles
	 * @param config
	 * @param nodes
	 */
	protected void loadRoles(ContentTypeConfigTO config, List<Node> nodes) {
		Set<String> roles = null;
		if (nodes != null && nodes.size() > 0) {
			roles = new FastSet<String>(nodes.size());
			for (Node node : nodes) {
				String role = node.getText();
				if (!StringUtils.isEmpty(role)) {
					roles.add(role);
				}
			}
		} else {
			roles = new FastSet<String>();
		}
		config.setAllowedRoles(roles);
	}

	
	/**
	 * load delete dependencies mapping
	 * 
	 * @param contentTypeConfig
	 * @param nodes
	 */
	protected void loadDeleteDependencies(ContentTypeConfigTO contentTypeConfig, List<Node> nodes) {
		List<DeleteDependencyConfigTO> deleteConfigs = new FastList<DeleteDependencyConfigTO>();
		if (nodes != null) {
			for (Node node : nodes) {
				Node patternNode = node.selectSingleNode("pattern");
				Node removeFolderNode = node.selectSingleNode("remove-empty-folder");
				if(patternNode!=null){
					String pattern = patternNode.getText();
					String removeEmptyFolder = removeFolderNode.getText();
					boolean isRemoveEmptyFolder=false;
					if(removeEmptyFolder!=null){
						isRemoveEmptyFolder = Boolean.valueOf(removeEmptyFolder);
					}
					if(StringUtils.isNotEmpty(pattern)){
						DeleteDependencyConfigTO deleteConfigTO = new DeleteDependencyConfigTO(pattern, isRemoveEmptyFolder);
						deleteConfigs.add(deleteConfigTO);
					}
				}
			}
			contentTypeConfig.setDeleteDependencies(deleteConfigs);
		}
	}

	/**
	 * 
	 * @param config
	 * @param copyDependencyNodes
	 * @return
	 */
	protected void loadCopyDependencyPatterns(ContentTypeConfigTO config, List<Node> copyDependencyNodes) {
		List<CopyDependencyConfigTO> copyConfig = new FastList<CopyDependencyConfigTO>();
		if (copyDependencyNodes != null) {
			for (Node copyDependency : copyDependencyNodes) {
				Node patternNode = copyDependency.selectSingleNode("pattern");
				Node targetNode = copyDependency.selectSingleNode("target");
				if(patternNode!=null && targetNode!=null){
					String pattern = patternNode.getText();
					String target = targetNode.getText();
					if(StringUtils.isNotEmpty(pattern) && StringUtils.isNotEmpty(target)){
						CopyDependencyConfigTO copyDependencyConfigTO  = new CopyDependencyConfigTO(pattern,target);
						copyConfig.add(copyDependencyConfigTO);
					}
				}
			}
		}
		config.setCopyDepedencyPattern(copyConfig);
		
	}

	/**
	 * create a list of QNames from the given set of nodes 
	 *
	 * @param nodes
	 */
	protected List<QName> createListOfQNames(List<Node> nodes) {
		if (nodes != null) {
			List<QName> components = new FastList<QName>(nodes.size());
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			for (Node node : nodes) {
				QName property = persistenceManagerService.createQName(node.valueOf("@name"));
				if (property != null) {
					components.add(property);
				}
			}
			return components;
		} 
		return null;
	}
	
	/**
	 * load search configuration
	 * 
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public SearchConfigTO loadSearchConfig(Node node) {
		SearchConfigTO searchConfig = new SearchConfigTO();
		if (node != null) {
			// get the maximum number of results to search for 
			String maxCount = node.valueOf("max-count");
			if (!StringUtils.isEmpty(maxCount)) {
				int max = ContentFormatUtils.getIntValue(node.valueOf("max-count"));
				searchConfig.setMaxCount((max <= 0) ? 100 : max);
			} else {
				searchConfig.setMaxCount(100);
			}
			// get wcm searchPath 
			String wcmSearchPath = node.valueOf("wcm-search-path");
			searchConfig.setWcmSearchPath(wcmSearchPath);
			// set base search filters
			List<Node> propNodes = node.selectNodes("base-searchable-properties/property");
			if (propNodes != null && propNodes.size() > 0) {
				List<SearchColumnTO> columns = new FastList<SearchColumnTO>(propNodes.size());
				for (Node propNode : propNodes) {
					String key = propNode.valueOf("@name");
					if (!StringUtils.isEmpty(key)) {
						SearchColumnTO column = new SearchColumnTO();
						column.setName(key);
						column.setTitle(propNode.valueOf("@title"));
						String useWildCard = propNode.valueOf("@use-wild-card");
						column.setUseWildCard(ContentFormatUtils.getBooleanValue(useWildCard));
						column.setSearchable(true);
						columns.add(column);
					}
				}
				searchConfig.setBaseSearchableColumns(columns);
			}
			// set searchable content-types
			List<Node> ctypeNodes = node.selectNodes("searchable-content-types/searchable-content-type");
			if (ctypeNodes != null && ctypeNodes.size() > 0) {
				List<String> ctypes = new FastList<String>(ctypeNodes.size());
				for (Node ctypeNode : ctypeNodes) {
					String s = ctypeNode.getText();
					if (!StringUtils.isEmpty(s)) {
						ctypes.add(ctypeNode.getText());
					}
				}
				searchConfig.setSearchableContentTypes(ctypes);
			}
			// set extractable metadata qnames
			Map<QName, String> extractableMetadata = getConfigMapWithStringValue(node.selectNodes("extractable-properties/property"));
			searchConfig.setExtractableMetadata(extractableMetadata);
			loadSearchColumnConfig(searchConfig, node.selectNodes("search-result/column"));
		}
		return searchConfig;
	}
	
	/**
	 * load search column configuration from the given nodes
	 * 
	 * @param nodes
	 * @return search column configuration
	 */
	protected void loadSearchColumnConfig(SearchConfigTO config, List<Node> nodes) {
		Map<String, QName> columns = new FastMap<String, QName>();
		if (nodes != null && nodes.size() > 0) {
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			for (Node node : nodes) {
				String name = node.valueOf("@name");
				String typeStr = node.getText();
				QName type = persistenceManagerService.createQName(typeStr);
				columns.put(name, type);
			}
		}
		config.setSearchColumnMap(columns);
	}

	/**
	 * get configuration map that has string values
	 * @param nodes
	 *
	 * @return
	 */
	protected Map<QName, String> getConfigMapWithStringValue(List<Node> nodes) {
		if (nodes != null) {
			Map<QName, String> configMap = new FastMap<QName, String>();
			PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
			for (Node node : nodes) {
				String typeStr = node.valueOf("@name");
				QName name = persistenceManagerService.createQName(typeStr);
				String value = node.getText();
				if (name != null) {
					configMap.put(name, value);
				}
			}
			return configMap;
		} else {
			return null;
		}
	}

    public Map<String, SiteContentTypePathsTO> getPathMapping() {
        return _pathMapping;
    }
    public void setPathMapping(Map<String, SiteContentTypePathsTO> pathMapping) {
        this._pathMapping = pathMapping;
    }

}
