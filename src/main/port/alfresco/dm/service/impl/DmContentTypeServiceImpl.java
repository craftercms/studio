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

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.lock.LockType;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;


import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmXmlConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentTypeService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmPreviewService;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.api.*;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.InvalidTypeException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.service.impl.ConfigurableServiceBase;
import org.craftercms.cstudio.alfresco.to.*;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class DmContentTypeServiceImpl extends ConfigurableServiceBase implements DmContentTypeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmContentTypeServiceImpl.class);

    @Override
    public void register() {
        getServicesManager().registerService(DmContentTypeService.class, this);
    }

    public List<String> getExcludedPaths(String site) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        TemplateConfigTO config = servicesConfig.getTemplateConfig(site);
        if (config != null) {
            return config.getExcludedPathsOnConvert();
        } else {
            return new FastList<String>(0);
        }
    }

    /**
     * @return the multiValuedPaths
     */
    public List<String> getMultiValuedPaths(String site) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        TemplateConfigTO config = servicesConfig.getTemplateConfig(site);
        if (config != null) {
            return config.getMultiValuedPathsOnConvert();
        } else {
            return new FastList<String>(0);
        }
    }

    @Override
    public InputStream mergeLastestTemplate(String site, String path, String contentType, String version, InputStream content) {
        ModelService modelService = getService(ModelService.class);
        String templateVersion = modelService.getTemplateVersion(site, contentType);

        if(templateVersion == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while mering content with the latest template, versiong is not turned on for the model-prototype. Returning the original content.");
            }

            return content;
        }
        else if(!templateVersion.equals(version)) {

            // get the original content and the latest template and merge them.

            try {
                Document target = ContentUtils.convertStreamToXml(content);
                Document template = modelService.getModelTemplate(site, contentType, false, false);
                mergeTemplate(template, target, templateVersion);
                return ContentUtils.convertDocumentToStream(target, CStudioConstants.CONTENT_ENCODING);
            } catch (DocumentException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while mering content with the latest template. Returning the original content.", e);
                }
                return content;
            }
            catch (AccessDeniedException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while mering content with the latest template. Returning the original content.", e);
                }
                return content;
            } catch (ContentNotFoundException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while mering content with the latest template. Returning the original content.", e);
                }
                return content;
            } catch (InvalidTypeException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while mering content with the latest template. Returning the original content.", e);
                }
                return content;
            }
        }else{
            return content;
        }
    }

    /**
     * merge new elemetns from the template document to target document
     *
     * @param template
     * @param target
     */
    @SuppressWarnings("unchecked")
    protected void mergeTemplate(Document template, Document target, String templateVersion) {
        if (template != null && target != null) {
            Element root = template.getRootElement();
            Element targetRoot = target.getRootElement();
            List<Element> elements = root.elements();
            if (elements != null && elements.size() > 0) {
                for (Element element : elements) {
                    String path = element.getName();
                    List<Node> nodes = targetRoot.selectNodes(path);
                    if (nodes != null && nodes.size() > 0) {
                        // for each existing element, walk down the structure and copy it over as needed
                        for (Node node : nodes) {
                            mergeChildElement(element, node);
                        }
                    } else {
                        Element copiedElement = element.createCopy();
                        targetRoot.add(copiedElement);
                    }
                }
            }
            // add the latest version label so it can be extracted when the content is saved
            Node versionNode = targetRoot.selectSingleNode(DmXmlConstants.ELM_TEMPLATE_VERSION);
            Element versionElement = (versionNode != null) ? (Element) versionNode : targetRoot.addElement(DmXmlConstants.ELM_TEMPLATE_VERSION);
            versionElement.setText(templateVersion);
        }
    }

    /**
     * merge the given template child element to the node
     *
     * @param templateElement
     * @param targetNode
     */
    @SuppressWarnings("unchecked")
    protected void mergeChildElement(Element templateElement, Node targetNode) {
        List<Element> elements = templateElement.elements();
        if (elements != null && elements.size() > 0) {
            for (Element element : elements) {
                String path = element.getName();
                List<Node> nodes = targetNode.selectNodes(path);
                if (nodes != null && nodes.size() > 0) {
                    for (Node childNode : nodes) {
                        mergeChildElement(element, childNode);
                    }
                } else {
                    Element copiedElement = element.createCopy();
                    Element targetElement = (Element) targetNode;
                    targetElement.add(copiedElement);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.craftercms.cstudio.alfresco.wcm.service.api.WcmContentTypeService#changeContentType(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void changeContentType(String site, String sub, String path, String contentType) throws ServiceException {
        ContentTypeConfigTO contentTypeConfigTO = getContentType(site, contentType);
        if (contentTypeConfigTO.getFormPath().equalsIgnoreCase(DmConstants.CONTENT_TYPE_CONFIG_FORM_PATH_SIMPLE)){
            // Simple form engine is not using templates - skip copying template and merging content
            return;
        }
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String fullPath = dmContentService.getContentFullPath(site, path);
        // get new template and the current data and merge data
        NodeRef node = persistenceManagerService.getNodeRef(fullPath);
        if (node != null) {
        	persistenceManagerService.lock(node, LockType.WRITE_LOCK);
            Document original = dmContentService.getContentXml(site, sub, path);
            ModelService modelService = getService(ModelService.class);
            Document template = modelService.getModelTemplate(site, contentType, false, false);
            String templateVersion = modelService.getTemplateVersion(site, contentType);
            copyContent(site, original, template, contentType, templateVersion);
            //cleanAspects(node);
            // write the content
            // TODO fix this part as write content is hanging.
            writeContent(site, path, contentType, node, template);
        } else {
            throw new ContentNotFoundException(path + " is not a valid content path.");
        }
    }

    @Override
    public List<ContentTypeConfigTO> getAllContentTypes(String site) {
        List<FileInfo> folders = getService(PersistenceManagerService.class).listFolders(
        		_configPath.replaceAll(CStudioConstants.PATTERN_SITE, site));
		List<ContentTypeConfigTO> contentTypes = new FastList<ContentTypeConfigTO>();
        if (folders != null) {
            for (FileInfo rootFolderInfo : folders) {
                // traverse the children file-folder structure
                getContentTypeConfigForChildren(site, rootFolderInfo.getNodeRef(), contentTypes);
            }
        }
        return contentTypes;
    }

	/**
     * Traverse file folder -- recursive!, searching for config.xml
     *
     * @param site
     * @param node
     */
    protected void getContentTypeConfigForChildren(String site, NodeRef node, List<ContentTypeConfigTO> contentTypes) {
        List<FileInfo> folders = getService(PersistenceManagerService.class).listFolders(node);
        if (folders != null) {
            for (FileInfo folderInfo : folders) {
                NodeRef configNode = getService(PersistenceManagerService.class).getChildByName(folderInfo.getNodeRef(),
                		ContentModel.ASSOC_CONTAINS, _configFileName);
                if (configNode != null){
                	ContentTypeConfigTO config = this.getContentTypesConfig().loadConfiguration(
                			site, configNode);
                	if (config != null) {
                		contentTypes.add(config);
                	}
                }
                // traverse the children file-folder structure
                getContentTypeConfigForChildren(site, folderInfo.getNodeRef(), contentTypes);
            }
        }
    }

    /**
     * copy values from the child elements to the target document
     *
     * TODO can optimize this code to just work on one recursive loop
     *
     * @param site
     * @param original
     * @param target
     * @param contentType
     * @param templateVersion
     */
    protected void copyContent(String site, Document original, Document target, String contentType, String templateVersion) {
        if (original != null && target != null) {
            Element root = original.getRootElement();
            Element targetRoot = target.getRootElement();
            List<Element> elements = root.elements();
            if (elements != null && elements.size() > 0) {
                for (Element element : elements) {
                    if (!StringUtils.equalsIgnoreCase(element.getName(), DmXmlConstants.ELM_DISPLAY_TEMPLATE)) {
                        Node targetNode = targetRoot.selectSingleNode(element.getName());
                        copyContent(site, "", element, targetNode);
                    }
                }
            }
            Node contentTypeNode = targetRoot.selectSingleNode("//" + DmXmlConstants.ELM_CONTENT_TYPE);
            ((Element) contentTypeNode).setText(contentType);
            // add the latest version label so it can be extracted when the content is saved
            Node versionNode = targetRoot.selectSingleNode(DmXmlConstants.ELM_TEMPLATE_VERSION);
            Element versionElement = (versionNode != null) ? (Element) versionNode : targetRoot.addElement(DmXmlConstants.ELM_TEMPLATE_VERSION);
            versionElement.setText(templateVersion);
        }
    }

    /**
     * copy the given element to the target node
     *
     * @param site
     * @param parentPath
     * @param element
     * @param targetNode
     */
    protected void copyContent(String site, String parentPath, Element element, Node targetNode) {
        if (targetNode != null) {
            String name = element.getName();
            String currentPath = parentPath + name;
            boolean noCopyOnConvert = DmUtils.getBooleanValue(targetNode.valueOf("@no-copy"), false);
            if (!this.getExcludedPaths(site).contains(currentPath) &&  !noCopyOnConvert) {
                Element targetElement = (Element) targetNode;
                boolean multiValued = DmUtils.getBooleanValue(targetNode.valueOf("@copy-children"), false);
                List<Element> childElements = element.elements();
                if (childElements != null && childElements.size() > 0) {
                    for (Element childElement : childElements) {
                        Node targetChildNode = null;
                        if (this.getMultiValuedPaths(site).contains(currentPath) || multiValued) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("[CHANGE-TEMPLATE] " + currentPath + " is multi-valued.");
                            }
                            targetChildNode = (Node) targetElement.addElement(childElement.getName());
                        } else {
                            String uniquePath = childElement.getUniquePath(element);
                            targetChildNode = targetElement.selectSingleNode(uniquePath);
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("[CHANGE-TEMPLATE] copying child node of "
                                    + currentPath + " : " + childElement.getUniquePath(element));
                        }
                        copyContent(site, currentPath + "/", childElement, targetChildNode);
                    }
                }
                targetElement.setText(element.getText());
                targetElement.setAttributes(element.attributes());
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[CHANGE-TEMPLATE] " + currentPath + " is not being copied since it is in exlcudedPaths or no copy on convert node.");
                }
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[CHANGE-TEMPLATE] " + element.getUniquePath() + " does not exist in target location.");
            }
        }

    }

    /**
     * write the updated content
     *
     * @param site
     * @param path
     * @param contentType
     * @param node
     * @param template
     * @throws ServiceException
     */
    protected void writeContent(String site, String path, String contentType, NodeRef node, Document template) throws ServiceException {
        FileInfo fileInfo = getService(PersistenceManagerService.class).getFileInfo(node);
        String fileName = fileInfo.getName();
        Map<String, String> params = new FastMap<String, String>();
        params.put(DmConstants.KEY_SITE, site);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FILE_NAME, fileName);
        params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        params.put(DmConstants.KEY_CREATE_FOLDERS, "false");
        params.put(DmConstants.KEY_EDIT, "true");
        InputStream inputStream = ContentUtils.convertDocumentToStream(template, CStudioConstants.CONTENT_ENCODING);
        DmPreviewService dmPreviewService = getService(DmPreviewService.class);
        dmPreviewService.writeContent(site, path, fileName, contentType, inputStream);

    }

    @Override
    protected void loadConfiguration(String key) {
    	// not used
    }

    /**
     * get a list of string form the given nodes
     *
     * @param nodes
     * @return string values
     */
    protected List<String> getStringList(List<Node> nodes) {
        if (nodes != null && nodes.size() > 0) {
            List<String> includes = new ArrayList<String>(nodes.size());
            for (Node node : nodes) {
                String value = node.getText();
                if (!StringUtils.isEmpty(value)) {
                    includes.add(value);
                }
            }
            return includes;
        }
        return null;
    }

    @Override
    protected TimeStamped getConfiguration(String key) {
    	// not used
    	return null;
    }

    @Override
    protected void removeConfiguration(String key) {
    	// not used
    }

    @Override
    protected NodeRef getConfigRef(String key) {
        String siteConfigPath = _configPath.replaceFirst(CStudioConstants.PATTERN_SITE, key);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return persistenceManagerService.getNodeRef(siteConfigPath + "/" + _configFileName);
    }

    @Override
    public List<ContentTypeConfigTO> getAllSearchableContentTypes(String site, String user) throws ServiceException {
        checkForUpdate(site);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        SearchConfigTO searchConfig = servicesConfig.getDefaultSearchConfig(site);
        if (searchConfig != null) {
            List<ContentTypeConfigTO> contentTypes = new ArrayList<ContentTypeConfigTO>();
            List<String> searchableContentTypes = searchConfig.getSearchableContentTypes();
            for (String stype : searchableContentTypes) {
                if (!StringUtils.isEmpty(stype)) {
                    ContentTypeConfigTO item = servicesConfig.getContentTypeConfig(site, stype);
                    if (item != null) {
                        contentTypes.add(item);
                    }
                }
            }
            return contentTypes;
        } else {
            LOGGER.error("No default Search config for site: " + site);
            return null;
        }
    }

    @Override
    public List<ContentTypeConfigTO> getAllowedContentTypes(String site, String relativepath) throws ServiceException {
    	this.getAllContentTypes(site);
        String user = getService(PersistenceManagerService.class).getCurrentUserName();
        PermissionService permissionService = getService(PermissionService.class);
        Set<String> userRoles = permissionService.getUserRoles(site, user);
        SiteContentTypePathsTO pathsConfig = this.getContentTypesConfig().getPathMapping(site);
        if (pathsConfig != null && pathsConfig.getConfigs() != null) {
            List<ContentTypeConfigTO> contentTypes = new FastList<ContentTypeConfigTO>();
            Set<String> contentKeys = new FastSet<String>();
            for (ContentTypePathTO pathConfig : pathsConfig.getConfigs()) {
                // check if the path matches one of includes paths
                if (relativepath.matches(pathConfig.getPathInclude())) {
                	if (LOGGER.isDebugEnabled()) {
                		LOGGER.debug(relativepath + " matches " + pathConfig.getPathInclude());
                	}
                	Set<String> allowedContentTypes = pathConfig.getAllowedContentTypes();
                	if (allowedContentTypes != null) {
                		for (String key : allowedContentTypes) {
                			if (!contentKeys.contains(key)) {
                            	if (LOGGER.isDebugEnabled()) {
                            		LOGGER.debug("Checking an allowed content type: " + key);
                            	}
	                			ContentTypeConfigTO typeConfig = 
	                				this.getContentTypesConfig().getContentTypeConfig(key);
	                			if (typeConfig != null) {
	                                boolean isMatch = true;
		                            if (typeConfig.getPathExcludes() != null) {
		                                for (String excludePath : typeConfig.getPathExcludes()) {
		                                    if (relativepath.matches(excludePath)) {
		                                    	if (LOGGER.isDebugEnabled()) {
		                                    		LOGGER.debug(relativepath + " matches an exclude path: " + excludePath);
		                                    	}
		                                        isMatch = false;
		                                        break;
		                                    }
		                                }
		                            }
		                            if (isMatch) {
		                                // if a match is found, populate the content type information
                                    	if (LOGGER.isDebugEnabled()) {
                                    		LOGGER.debug("adding " + key + " to content types.");
                                    	}
		                                addContentTypes(site, userRoles, typeConfig, contentTypes);
		                            }
	                			} else {
	                				if (LOGGER.isWarnEnabled()) {
	                					LOGGER.warn("no configuration found for " + key);
	                				}
	                			}
	                			contentKeys.add(key);
                			} else {
                            	if (LOGGER.isDebugEnabled()) {
                            		LOGGER.debug(key + " is already added. skipping the content type.");
                            	}
                			}
                		}
                	}
                }
            }
            return contentTypes;
        } else {
            LOGGER.error("No content type path configuration is found for site: " + site);
            return null;
        }
    }

    /**
     * add DM/WCM content types to the given content types list
     *
     * @param site
     * @param userRoles
     * @param allowedContentTypes content types to add
     * @param contentTypes
     */
    protected void addContentTypes(String site, Set<String> userRoles, ContentTypeConfigTO config, List<ContentTypeConfigTO> contentTypes) {
        boolean isAllowed = this.isUserAllowed(userRoles, config);
        if (isAllowed) {
            contentTypes.add(config);
        }
    }

    @Override
    public ContentTypeConfigTO getContentTypeByRelativePath(String site, String sub, String relativePath) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, relativePath);
        try {
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node != null) {
                String type = getContentType(node);
                if (!StringUtils.isEmpty(type)) {
                    ServicesConfig servicesConfig = getService(ServicesConfig.class);
                    return servicesConfig.getContentTypeConfig(site, type);
                } else {
                    throw new ServiceException("No content type specified for " + relativePath + " in site: " + site);
                }
            } else {
                throw new ContentNotFoundException(relativePath + " is not found in site: " + site + " sub: " + sub);
            }
        } catch (Exception e) { // PORT AVMBadArgumentException e) {
            throw new ContentNotFoundException(relativePath + " is not valid in site: " + site + " sub: " + sub);
        }
    }

    /**
     * get the content type value of the given node
     *
     * @param node
     * @return content type
     */
    protected String getContentType(NodeRef node) {
        Map<QName, Serializable> properties = getService(PersistenceManagerService.class).getProperties(node);
        Serializable typeValue = properties.get(CStudioContentModel.PROP_CONTENT_TYPE);
        if (typeValue != null) {
            return (String) typeValue;
        } else {
            return null;
        }
    }

    @Override
    public boolean isUserAllowed(Set<String> userRoles, ContentTypeConfigTO item) {
        if (item != null) {
            String name = item.getName();
            Set<String> allowedRoles = item.getAllowedRoles();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking allowed roles on " + name + ". user roles: "
                        + userRoles + ", allowed roles: " + allowedRoles);
            }
            if (allowedRoles == null || allowedRoles.size() == 0) {
                return true;
            } else {
                boolean notAllowed = Collections.disjoint(userRoles, allowedRoles);
                if (notAllowed) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(name + " is not allowed for the user.");
                    }
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("no content type config provided. returning true for user access to content type checking.");
            }
            return true;
        }
    }

    @Override
    public ContentTypeConfigTO getContentType(String site, String type) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        return servicesConfig.getContentTypeConfig(site, type);
    }

    protected ContentTypesConfig getContentTypesConfig() {
    	return getServicesManager().getService(ContentTypesConfig.class);
    }
    
}
