/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ContentTypesConfig;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME;

/**
 * @author Dejan Brkic
 */
public class ContentTypeServiceImpl implements ContentTypeService {

    private static final Logger logger = LoggerFactory.getLogger(ContentTypeServiceImpl.class);

    protected ContentService contentService;
    protected ServicesConfig servicesConfig;
    protected ContentTypesConfig contentTypesConfig;
    protected SecurityService securityService;
    protected ContentRepository contentRepository;
    protected StudioConfiguration studioConfiguration;

    @Override
    @ValidateParams
    public ContentTypeConfigTO getContentTypeForContent(@ValidateStringParam(name = "site") String site,
                                                        @ValidateSecurePathParam(name = "path") String path)
            throws ServiceLayerException {
        ContentItemTO itemTO = contentService.getContentItem(site, path, 0);
        if (itemTO != null) {
            String type = itemTO.getContentType();
            if (!StringUtils.isEmpty(type)) {
                return servicesConfig.getContentTypeConfig(site, type);
            } else {
                throw new ServiceLayerException("No content type specified for " + path + " in site: " + site);
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
    @ValidateParams
    public ContentTypeConfigTO getContentTypeByRelativePath(@ValidateStringParam(name = "site") String site,
                                                            @ValidateSecurePathParam(name = "relativePath")
                                                                    String relativePath) throws ServiceLayerException {
        ContentItemTO item = contentService.getContentItem(site, relativePath, 0);
        if (item != null) {
            String type = item.getContentType();
            if (!StringUtils.isEmpty(type)) {
                return servicesConfig.getContentTypeConfig(site, type);
            } else {
                throw new ServiceLayerException("No content type specified for " + relativePath + " in site: " + site);
            }
        } else {
            throw new ContentNotFoundException(relativePath + " is not found in site: " + site);
        }
    }

    @Override
    @ValidateParams
    public ContentTypeConfigTO getContentType(@ValidateStringParam(name = "site") String site,
                                              @ValidateStringParam(name = "type") String type) {
        return servicesConfig.getContentTypeConfig(site, type);
    }

    @Override
    @ValidateParams
    public List<ContentTypeConfigTO> getAllContentTypes(@ValidateStringParam(name = "site") String site,
                                                        boolean searchable) {
        return getAllContentTypes(site);
    }

    @Override
    @ValidateParams
    public List<ContentTypeConfigTO> getAllowedContentTypesForPath(@ValidateStringParam(name = "site") String site,
                                                                   @ValidateSecurePathParam(name = "relativePath")
                                                                           String relativePath) {
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

    protected void addContentTypes(String site, Set<String> userRoles, ContentTypeConfigTO config,
                                   List<ContentTypeConfigTO> contentTypes) {
        boolean isAllowed = this.isUserAllowed(userRoles, config);
        if (isAllowed) {
            contentTypes.add(config);
        }
    }

    @Override
    @ValidateParams
    public boolean changeContentType(@ValidateStringParam(name = "site") String site,
                                     @ValidateSecurePathParam(name = "path") String path,
                                     @ValidateStringParam(name = "contentType") String contentType)
            throws ServiceLayerException, UserNotFoundException {
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
                String configPath =
                        folders[i].path + FILE_SEPARATOR + folders[i].name + FILE_SEPARATOR + getConfigFileName();
                if (contentService.contentExists(site, configPath)) {
                    ContentTypeConfigTO config = contentTypesConfig
                            .reloadConfiguration(site,
                                    configPath.replace(contentTypesRootPath, "")
                                            .replace(FILE_SEPARATOR + getConfigFileName(), ""));
                    if (config != null) {
                        contentTypes.add(config);
                    }
                }

                reloadContentTypeConfigForChildren(site, folders[i], contentTypes);
            }
        }
        return contentTypes;
    }

    protected void reloadContentTypeConfigForChildren(String site, RepositoryItem node,
                                                      List<ContentTypeConfigTO> contentTypes) {
        String contentTypesRootPath = getConfigPath().replaceAll(StudioConstants.PATTERN_SITE, site);
        String fullPath = node.path + FILE_SEPARATOR + node.name;
        logger.debug("Get Content Type Config fot Children path = {0}", fullPath );
        RepositoryItem[] folders = contentRepository.getContentChildren(site, fullPath);
        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                if (folders[i].isFolder) {
                    String configPath =
                            folders[i].path + FILE_SEPARATOR + folders[i].name + FILE_SEPARATOR + getConfigFileName();
                    if (contentService.contentExists(site, configPath)) {
                        ContentTypeConfigTO config = contentTypesConfig
                                .reloadConfiguration(site, configPath
                                        .replace(contentTypesRootPath, "")
                                        .replace(FILE_SEPARATOR + getConfigFileName(), ""));
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

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public ContentTypesConfig getContentTypesConfig() {
        return contentTypesConfig;
    }

    public void setContentTypesConfig(ContentTypesConfig contentTypesConfig) {
        this.contentTypesConfig = contentTypesConfig;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
