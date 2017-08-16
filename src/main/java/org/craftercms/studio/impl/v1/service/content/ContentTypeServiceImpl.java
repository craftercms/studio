/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ContentTypesConfig;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.util.*;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME;

/**
 * @author Dejan Brkic
 */
public class ContentTypeServiceImpl implements ContentTypeService {

    private static final Logger logger = LoggerFactory.getLogger(ContentTypeServiceImpl.class);

    @Override
    public ContentTypeConfigTO getContentTypeForContent(String site, String path) throws ServiceException {
        ContentItemTO itemTO = contentService.getContentItem(site, path, 0);
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
        ContentItemTO item = contentService.getContentItem(site, relativePath, 0);
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

    @Override
    public List<ContentTypeConfigTO> getAllowedContentTypesForPath(String site, String relativePath) throws ServiceException {
        String user = securityService.getCurrentUser();
        Set<String> userRoles = securityService.getUserRoles(site, user);
        List<ContentTypeConfigTO> allContentTypes = getAllContentTypes(site);

        if (CollectionUtils.isNotEmpty(allContentTypes)) {
            List<ContentTypeConfigTO> contentTypes = new ArrayList<ContentTypeConfigTO>();
            for (ContentTypeConfigTO contentTypeConfig : allContentTypes) {
                // check if the path matches one of includes paths
                if (CollectionUtils.isNotEmpty(contentTypeConfig.getPathIncludes())){
                    for (String pathIncludes : contentTypeConfig.getPathIncludes()) {
                        if (relativePath.matches(pathIncludes)) {
                            logger.debug(relativePath + " matches " + pathIncludes);
                            boolean isMatch = true;
                            if (contentTypeConfig.getPathExcludes() != null) {
                                for (String excludePath : contentTypeConfig.getPathExcludes()) {
                                    if (relativePath.matches(excludePath)) {
                                        logger.debug(relativePath + " matches an exclude path: " + excludePath);
                                        isMatch = false;
                                        break;
                                    }
                                }
                            }
                            if (isMatch) {
                                // if a match is found, populate the content type information
                                addContentTypes(site, userRoles, contentTypeConfig, contentTypes);
                            }
                        }
                    }
                } else if (CollectionUtils.isEmpty(contentTypeConfig.getPathExcludes())) {
                    addContentTypes(site, userRoles, contentTypeConfig, contentTypes);
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
        // get new template and the current data and merge data
        ContentItemTO item = contentService.getContentItem(site, path, 0);
        if (item != null) {
            contentService.lockContent(site, path);
            Document original = null;
            try {
                original = contentService.getContentAsDocument(site, path);
            } catch (DocumentException e) {
                logger.error("Error while getting document for site: " + site + " path: " + path, e);
                return false;
            }
            throw new RuntimeException("Is it getting here?");
        } else {
            throw new ContentNotFoundException(path + " is not a valid content path.");
        }
    }

    protected List<ContentTypeConfigTO> getAllContentTypes(String site) {
        String contentTypesRootPath = getConfigPath().replaceAll(StudioConstants.PATTERN_SITE, site);

        RepositoryItem[] folders = contentRepository.getContentChildren(site, contentTypesRootPath);
        List<ContentTypeConfigTO> contentTypes = new ArrayList<>();

        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                String configPath = folders[i].path + "/" + folders[i].name + "/" + getConfigFileName();
                if (contentService.contentExists(site, configPath)) {
                    ContentTypeConfigTO config = contentTypesConfig.reloadConfiguration(site, configPath.replace(contentTypesRootPath, "").replace("/" + getConfigFileName(), ""));
                    if (config != null) {
                        contentTypes.add(config);
                    }
                }

                reloadContentTypeConfigForChildren(site, folders[i], contentTypes);
            }
        }
        return contentTypes;
    }

    @Override
    public void reloadConfiguration(String site) {
        String contentTypesRootPath = getConfigPath().replaceAll(StudioConstants.PATTERN_SITE, site);
        RepositoryItem[] folders = contentRepository.getContentChildren(site, contentTypesRootPath);
        List<ContentTypeConfigTO> contentTypes = new ArrayList<>();

        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                String configPath = folders[i].path + "/" + folders[i].name + "/" + getConfigFileName();
                if (contentService.contentExists(site, configPath)) {
                    ContentTypeConfigTO config = contentTypesConfig.reloadConfiguration(site, configPath.replace(contentTypesRootPath, "").replace("/" + getConfigFileName(), ""));
                    if (config != null) {
                        contentTypes.add(config);
                    }
                }

                reloadContentTypeConfigForChildren(site, folders[i], contentTypes);
            }
        }
    }

    protected void reloadContentTypeConfigForChildren(String site, RepositoryItem node, List<ContentTypeConfigTO> contentTypes) {
        String contentTypesRootPath = getConfigPath().replaceAll(StudioConstants.PATTERN_SITE, site);
        String fullPath = node.path + "/" + node.name;
        logger.debug("Get Content Type Config fot Children path = {0}", fullPath );
        RepositoryItem[] folders = contentRepository.getContentChildren(site, fullPath);
        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                if (folders[i].isFolder) {
                    String configPath = folders[i].path + "/" + folders[i].name + "/" + getConfigFileName();
                    if (contentService.contentExists(site, configPath)) {
                        ContentTypeConfigTO config = contentTypesConfig.reloadConfiguration(site, configPath.replace(contentTypesRootPath, "").replace("/" + getConfigFileName(), ""));
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

    public String getConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH);
    }

    public String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME);
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

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected ContentService contentService;
    protected ServicesConfig servicesConfig;
    protected ContentTypesConfig contentTypesConfig;
    protected SecurityService securityService;
    protected ContentRepository contentRepository;
    protected StudioConfiguration studioConfiguration;
}
