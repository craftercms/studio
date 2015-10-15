/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.ConfigurableServiceBase;
import org.craftercms.studio.api.v1.service.configuration.ContentTypesConfig;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.Serializable;
import java.util.*;

/**
 * @author Dejan Brkic
 */
public class ContentTypeServiceImpl implements ContentTypeService {

    private static final Logger logger = LoggerFactory.getLogger(ContentTypeServiceImpl.class);

    @Override
    public ContentTypeConfigTO getContentTypeForContent(String site, String path) throws ServiceException {
        ContentItemTO itemTO = contentService.getContentItem(site, path);
        if (itemTO != null) {
            String type = itemTO.getContentType();
            if (!StringUtils.isEmpty(type)) {
                return servicesConfig.getContentTypeConfig(site, type);
            } else {
                throw new ServiceException("No content type specified for " + path + " in site: " + site);
            }
        } else {
            throw new ContentNotFoundException(path + " is not found in site: " + site);
        }
    }

    @Override
    public boolean isUserAllowed(Set<String> userRoles, ContentTypeConfigTO item) {
        if (item != null) {
            String name = item.getName();
            Set<String> allowedRoles = item.getAllowedRoles();
            logger.debug("Checking allowed roles on " + name + ". user roles: "
                    + userRoles + ", allowed roles: " + allowedRoles);

            if (allowedRoles == null || allowedRoles.size() == 0) {
                return true;
            } else {
                boolean notAllowed = Collections.disjoint(userRoles, allowedRoles);
                if (notAllowed) {
                    logger.debug(name + " is not allowed for the user.");
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            logger.debug("no content type config provided. returning true for user access to content type checking.");

            return true;
        }
    }

    @Override
    public ContentTypeConfigTO getContentTypeByRelativePath(String site, String relativePath) throws ServiceException {
        ContentItemTO item = contentService.getContentItem(site, relativePath);
        if (item != null) {
            String type = item.getContentType();
            if (!StringUtils.isEmpty(type)) {
                return servicesConfig.getContentTypeConfig(site, type);
            } else {
                throw new ServiceException("No content type specified for " + relativePath + " in site: " + site);
            }
        } else {
            throw new ContentNotFoundException(relativePath + " is not found in site: " + site);
        }
    }

    @Override
    public ContentTypeConfigTO getContentType(String site, String type) {
        return servicesConfig.getContentTypeConfig(site, type);
    }

    @Override
    public List<ContentTypeConfigTO> getAllContentTypes(String site, boolean searchable) {
        return getAllContentTypes(site);
    }

    protected List<ContentTypeConfigTO> getAllContentTypes(String site) {
        SiteContentTypePathsTO pathsConfig = contentTypesConfig.getPathMapping(site);
        if (pathsConfig != null && pathsConfig.getConfigs() != null) {
            List<ContentTypeConfigTO> contentTypes = new ArrayList<ContentTypeConfigTO>();
            for (ContentTypePathTO pathConfig : pathsConfig.getConfigs()) {
                Set<String> allowedContentTypes = pathConfig.getAllowedContentTypes();
                if (CollectionUtils.isNotEmpty(allowedContentTypes)) {
                    for (String key : allowedContentTypes) {
                        logger.debug("Checking an allowed content type: " + key);
                        if (StringUtils.isNotEmpty(key)) {
                            String[] tokens = key.split(",");
                            if (tokens.length == 2) {
                                ContentTypeConfigTO typeConfig = contentTypesConfig.getContentTypeConfig(tokens[0], tokens[1]);
                                if (typeConfig != null) {
                                    // if a match is found, populate the content type information
                                    logger.debug("adding " + key + " to content types.");
                                    contentTypes.add(typeConfig);
                                }
                            }
                        }
                    }
                }
            }
            return contentTypes;
        } else {
            logger.error("No content type path configuration is found for site: " + site);
            return null;
        }
    }



    @Override
    public List<ContentTypeConfigTO> getAllowedContentTypesForPath(String site, String relativePath) throws ServiceException {
        String user = securityService.getCurrentUser();
        Set<String> userRoles = securityService.getUserRoles(site, user);
        SiteContentTypePathsTO pathsConfig = contentTypesConfig.getPathMapping(site);
        if (pathsConfig != null && pathsConfig.getConfigs() != null) {
            List<ContentTypeConfigTO> contentTypes = new ArrayList<ContentTypeConfigTO>();
            Set<String> contentKeys = new HashSet<String>();
            for (ContentTypePathTO pathConfig : pathsConfig.getConfigs()) {
                // check if the path matches one of includes paths
                if (relativePath.matches(pathConfig.getPathInclude())) {
                    logger.debug(relativePath + " matches " + pathConfig.getPathInclude());
                    Set<String> allowedContentTypes = pathConfig.getAllowedContentTypes();
                    if (CollectionUtils.isNotEmpty(allowedContentTypes)) {
                        for (String key : allowedContentTypes) {
                            if (!contentKeys.contains(key)) {
                                logger.debug("Checking an allowed content type: " + key);
                                if (StringUtils.isNotEmpty(key)) {
                                    String[] tokens = key.split(",");
                                    if (tokens.length == 2) {
                                        ContentTypeConfigTO typeConfig = contentTypesConfig.getContentTypeConfig(tokens[0], tokens[1]);
                                        if (typeConfig != null) {
                                            boolean isMatch = true;
                                            if (typeConfig.getPathExcludes() != null) {
                                                for (String excludePath : typeConfig.getPathExcludes()) {
                                                    if (relativePath.matches(excludePath)) {
                                                        logger.debug(relativePath + " matches an exclude path: " + excludePath);
                                                        isMatch = false;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (isMatch) {
                                                // if a match is found, populate the content type information
                                                logger.debug("adding " + key + " to content types.");
                                                addContentTypes(site, userRoles, typeConfig, contentTypes);
                                            }
                                        } else {
                                            logger.warn("no configuration found for " + key);
                                        }
                                        contentKeys.add(key);
                                    }
                                }
                            } else {
                                logger.debug(key + " is already added. skipping the content type.");
                            }
                        }
                    }
                }
            }
            return contentTypes;
        } else {
            logger.error("No content type path configuration is found for site: " + site);
            return null;
        }
    }

    protected void addContentTypes(String site, Set<String> userRoles, ContentTypeConfigTO config, List<ContentTypeConfigTO> contentTypes) {
        boolean isAllowed = this.isUserAllowed(userRoles, config);
        if (isAllowed) {
            contentTypes.add(config);
        }
    }

    public boolean changeContentType(String site, String path, String contentType) throws ServiceException {
        ContentTypeConfigTO contentTypeConfigTO = getContentType(site, contentType);
        if (contentTypeConfigTO.getFormPath().equalsIgnoreCase(DmConstants.CONTENT_TYPE_CONFIG_FORM_PATH_SIMPLE)){
            // Simple form engine is not using templates - skip copying template and merging content
            return true;
        }
        String fullPath = contentService.expandRelativeSitePath(site, path);
        // get new template and the current data and merge data
        ContentItemTO item = contentService.getContentItem(site, path, 0);
        if (item != null) {
            contentService.lockContent(site, path);
            Document original = null;
            try {
                original = contentService.getContentAsDocument(fullPath);
            } catch (DocumentException e) {
                logger.error("Error while getting document for " + fullPath, e);
                return false;
            }
            throw new RuntimeException("Is it getting here?");
            /*
            ModelService modelService = getService(ModelService.class);
            Document template = modelService.getModelTemplate(site, contentType, false, false);
            String templateVersion = modelService.getTemplateVersion(site, contentType);
            copyContent(site, original, template, contentType, templateVersion);
            //cleanAspects(node);
            // write the content
            // TODO fix this part as write content is hanging.
            writeContent(site, path, contentType, node, template);
            return true;*/
        } else {
            throw new ContentNotFoundException(path + " is not a valid content path.");
        }
    }

    @Override
    public void reloadConfiguration(String site) {
        String contentTypesRootPath = configPath.replaceAll(CStudioConstants.PATTERN_SITE, site);
        RepositoryItem[] folders = contentRepository.getContentChildren(contentTypesRootPath);
        List<ContentTypeConfigTO> contentTypes = new ArrayList<>();

        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                reloadContentTypeConfigForChildren(site, folders[i], contentTypes);
            }
        }
    }

    protected void reloadContentTypeConfigForChildren(String site, RepositoryItem node, List<ContentTypeConfigTO> contentTypes) {
        String contentTypesRootPath = configPath.replaceAll(CStudioConstants.PATTERN_SITE, site);
        String fullPath = node.path + "/" + node.name;
        logger.debug("Get Content Type Config fot Children path = {0}", fullPath );
        RepositoryItem[] folders = contentRepository.getContentChildren(fullPath);
        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                if (folders[i].isFolder) {
                    String configPath = folders[i].path + "/" + folders[i].name + "/" + configFileName;
                    if (contentService.contentExists(configPath)) {
                        ContentTypeConfigTO config = contentTypesConfig.reloadConfiguration(site, configPath.replace(contentTypesRootPath, "").replace("" + configFileName, ""));
                        if (config != null) {
                            contentTypes.add(config);
                        }
                    }
                    // traverse the children file-folder structure

                    reloadContentTypeConfigForChildren(site, folders[i], contentTypes);
                }
            }
        }
    }


    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public ContentTypesConfig getContentTypesConfig() { return contentTypesConfig; }
    public void setContentTypesConfig(ContentTypesConfig contentTypesConfig) { this.contentTypesConfig = contentTypesConfig; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public String getConfigPath() { return configPath; }
    public void setConfigPath(String configPath) { this.configPath = configPath; }

    public String getConfigFileName() { return configFileName; }
    public void setConfigFileName(String configFileName) { this.configFileName = configFileName; }

    protected ContentService contentService;
    protected ServicesConfig servicesConfig;
    protected ContentTypesConfig contentTypesConfig;
    protected SecurityService securityService;
    protected ContentRepository contentRepository;
    protected String configPath;
    protected String configFileName;
}
