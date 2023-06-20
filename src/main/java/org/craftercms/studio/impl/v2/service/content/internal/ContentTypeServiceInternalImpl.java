/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.content.internal;

import com.google.common.cache.Cache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.CopyDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.contentType.ContentTypeUsage;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.lang3.RegExUtils.replaceAll;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_CREATE;

public class ContentTypeServiceInternalImpl implements ContentTypeServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(ContentTypeServiceInternalImpl.class);

    private static final String QUICK_CREATE = "quickCreate";
    private static final String QUICK_CREATE_PATH = "quickCreatePath";

    protected final SecurityService securityService;
    protected final ConfigurationService configurationService;
    protected final ItemDAO itemDao;
    protected ContentService contentService;
    protected final SiteService siteService;
    protected final StudioConfiguration studioConfiguration;
    protected final ContentRepository contentRepository;

    protected final String contentTypeBasePathPattern;
    protected final String contentTypeDefinitionFilename;
    protected final String templateXPath;
    protected final String controllerPattern;
    protected final String controllerFormat;
    protected final String previewImageXPath;
    protected final String defaultPreviewImagePath;

    protected final Cache<String, ContentTypeConfigTO> cache;

    @ConstructorProperties({"securityService", "configurationService", "itemDao",
            "siteService", "studioConfiguration", "contentRepository", "contentTypeBasePathPattern", "contentTypeDefinitionFilename",
            "templateXPath", "controllerPattern", "controllerFormat", "previewImageXPath", "defaultPreviewImagePath", "cache"})
    public ContentTypeServiceInternalImpl(SecurityService securityService,
                                          ConfigurationService configurationService, ItemDAO itemDao,
                                          SiteService siteService, StudioConfiguration studioConfiguration,
                                          ContentRepository contentRepository, String contentTypeBasePathPattern,
                                          String contentTypeDefinitionFilename, String templateXPath,
                                          String controllerPattern, String controllerFormat,
                                          String previewImageXPath, String defaultPreviewImagePath, Cache<String, ContentTypeConfigTO> cache) {
        this.securityService = securityService;
        this.configurationService = configurationService;
        this.itemDao = itemDao;
        this.siteService = siteService;
        this.studioConfiguration = studioConfiguration;
        this.contentRepository = contentRepository;
        this.contentTypeBasePathPattern = contentTypeBasePathPattern;
        this.contentTypeDefinitionFilename = contentTypeDefinitionFilename;
        this.templateXPath = templateXPath;
        this.controllerPattern = controllerPattern;
        this.controllerFormat = controllerFormat;
        this.previewImageXPath = previewImageXPath;
        this.defaultPreviewImagePath = defaultPreviewImagePath;
        this.cache = cache;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public ContentTypeConfigTO loadContentTypeConfiguration(String siteId, String contentTypeId) {
        String siteConfigPath = getConfigPath().replaceAll(PATTERN_SITE, siteId)
                .replaceAll(PATTERN_CONTENT_TYPE, contentTypeId)
                .replaceAll(FILE_SEPARATOR + FILE_SEPARATOR, FILE_SEPARATOR);
        String configFileFullPath = siteConfigPath + FILE_SEPARATOR + getConfigFileName();

        // TODO: SJ: Add general lock service lock around this key to avoid having more than one thread do this work
        var cacheKey = configurationService.getCacheKey(siteId, null, configFileFullPath,
                null, "object");
        ContentTypeConfigTO contentTypeConfig = cache.getIfPresent(cacheKey);
        if (contentTypeConfig == null) {
            try {
                logger.debug("Cache miss for key '{}'", cacheKey);

                if (contentService.contentExists(siteId, configFileFullPath)) {
                    Document document = configurationService.getConfigurationAsDocument(siteId, null,
                            configFileFullPath, null);
                    Element root = document.getRootElement();
                    String name = root.valueOf("@name");
                    contentTypeConfig = new ContentTypeConfigTO();
                    contentTypeConfig.setName(name);
                    contentTypeConfig.setLabel(root.valueOf("label"));
                    String imageThumbnail=root.valueOf("image-thumbnail");
                    if (imageThumbnail != null) {
                        contentTypeConfig.setImageThumbnail(imageThumbnail);
                    }
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
                    loadRoles(contentTypeConfig, root.selectNodes("allowed-roles/role"));
                    loadDeleteDependencies(contentTypeConfig, root.selectNodes("delete-dependencies/delete-dependency"));
                    loadCopyDependencyPatterns(contentTypeConfig, root.selectNodes("copy-dependencies/copy-dependency"));
                    contentTypeConfig.setLastUpdated(DateUtils.getCurrentTime());
                    contentTypeConfig.setType(getContentTypeTypeByName(name));
                    boolean quickCreate = ContentFormatUtils.getBooleanValue(root.valueOf(QUICK_CREATE));
                    contentTypeConfig.setQuickCreate(quickCreate);
                    contentTypeConfig.setQuickCreatePath(root.valueOf(QUICK_CREATE_PATH));

                    cache.put(cacheKey, contentTypeConfig);
                }
            } catch (ServiceLayerException e) {
                logger.error("No content type configuration document found in site '{}' at '{}'",
                        siteId, configFileFullPath, e);
            }
        }
        return contentTypeConfig;
    }

    @Override
    public List<ContentTypeConfigTO> getContentTypes(String siteId, String path) throws ServiceLayerException {
        if (isEmpty(path)) {
            return getAllContentTypes(siteId);
        }

        return getAllowedContentTypesForPath(siteId, path);
    }

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) throws SiteNotFoundException {
        return getAllContentTypes(siteId).stream()
                .filter(ContentTypeConfigTO::isQuickCreate)
                .filter(contentType ->
                    securityService.getUserPermissions(siteId, contentType.getQuickCreatePath(),
                                    securityService.getCurrentUser(), null)
                            .contains(PERMISSION_CONTENT_CREATE))
                .map(contentType -> {
                    QuickCreateItem item = new QuickCreateItem();
                    item.setSiteId(siteId);
                    item.setContentTypeId(contentType.getForm());
                    item.setLabel(contentType.getLabel());
                    item.setPath(contentType.getQuickCreatePath());
                    return item;
                })
                .collect(toList());
    }

    @Override
    public ContentTypeUsage getContentTypeUsage(String siteId, String contentType) throws ServiceLayerException {

        var usages = new ContentTypeUsage();

        String template = getContentTypeTemplatePath(siteId, contentType);
        if (isNotEmpty(template)) {
            usages.setTemplates(singletonList(template));
        }

        String scriptPath = getContentTypeControllerPath(contentType);

        List<Item> items = itemDao.getContentTypeUsages(siteId, contentType, scriptPath);

        usages.setContent(items.stream()
                .filter(i -> equalsAnyIgnoreCase(i.getSystemType(), CONTENT_TYPE_PAGE, CONTENT_TYPE_COMPONENT))
                .map(Item::getPath)
                .collect(toList()));

        usages.setScripts(items.stream()
                .filter(i -> equalsIgnoreCase(i.getSystemType(), (CONTENT_TYPE_SCRIPT)))
                .map(Item::getPath)
                .collect(toList()));

        return usages;
    }

    @Override
    public ImmutablePair<String, Resource> getContentTypePreviewImage(String siteId,
                                                                      @ValidateSecurePathParam String contentTypeId) throws ServiceLayerException {

        String filename = getContentTypePreviewImageFilename(siteId, contentTypeId);
        boolean hasPreviewImage = isNotEmpty(filename) && !filename.equals("undefined"); // form-definition could have undefined value for imageThumbnail
        if (hasPreviewImage) {
            String previewImagePath = UrlUtils.concat(getContentTypePath(contentTypeId), filename);
            return (new ImmutablePair(previewImagePath, contentService.getContentAsResource(siteId, previewImagePath)));
        }

        return (new ImmutablePair(defaultPreviewImagePath, new ClassPathResource(defaultPreviewImagePath)));
    }

    @Override
    public void deleteContentType(String siteId, String contentType, boolean deleteDependencies)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException {
        ContentTypeUsage usage = getContentTypeUsage(siteId, contentType);

        var files = new LinkedList<String>();

        if (CollectionUtils.isNotEmpty(usage.getContent())) {
            if (!deleteDependencies) {
                throw new ServiceLayerException("The content-type " + contentType + " in site " + siteId +
                        " can't be deleted because there is content using it");
            }

            files.addAll(usage.getContent());
        }

        files.addAll(usage.getTemplates());
        files.addAll(usage.getScripts());
        files.add(getContentTypePath(contentType));

        if (!contentService.deleteContent(siteId, files, "Delete content-type " + contentType)) {
            throw new ServiceLayerException("Error deleting content-type " + contentType + " in site "+ siteId);
        }

    }

    @Override
    public String getContentTypeControllerPath(String contentTypeId) {
        return replaceAll(contentTypeId, controllerPattern, controllerFormat);
    }

    @Override
    public String getContentTypeTemplatePath(String siteId, String contentTypeId) throws ServiceLayerException {
        Document definition = getFormDefinitionDocument(siteId, contentTypeId);

        Node templateNode = definition.selectSingleNode(templateXPath);

        if (templateNode != null && isNotEmpty(templateNode.getText())) {
            return templateNode.getText();
        }

        return null;
    }

    /**
     * Get all content types for a site
     * @param siteId site identifier
     * @return list of {@link ContentTypeConfigTO} for the site
     * @throws SiteNotFoundException
     */
    protected List<ContentTypeConfigTO> getAllContentTypes(String siteId) throws SiteNotFoundException {
        String contentTypesRootPath = getSiteContentTypesConfigPath().replaceAll(PATTERN_SITE, siteId);

        RepositoryItem[] folders = contentRepository.getContentChildren(siteId, contentTypesRootPath);
        List<ContentTypeConfigTO> contentTypes = new ArrayList<>();

        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                String configPath =
                        folders[i].path + FILE_SEPARATOR + folders[i].name + FILE_SEPARATOR + getConfigFileName();
                if (contentService.contentExists(siteId, configPath)) {
                    ContentTypeConfigTO config =
                            reloadConfiguration(siteId, configPath.replace(contentTypesRootPath, "")
                                    .replace(FILE_SEPARATOR + getConfigFileName(), ""));
                    if (config != null) {
                        contentTypes.add(config);
                    }
                }

                reloadContentTypeConfigForChildren(siteId, folders[i], contentTypes);
            }
        }
        return contentTypes;
    }

    /**
     * Recursive method to reload content type config for children
     * @param siteId site identifier
     * @param node {@link RepositoryItem} node object
     * @param contentTypes list of {@link ContentTypeConfigTO} to load
     * @throws SiteNotFoundException
     */
    protected void reloadContentTypeConfigForChildren(String siteId, RepositoryItem node,
                                                      List<ContentTypeConfigTO> contentTypes) throws SiteNotFoundException {
        String contentTypesRootPath = getSiteContentTypesConfigPath().replaceAll(PATTERN_SITE, siteId);
        String fullPath = node.path + FILE_SEPARATOR + node.name;
        logger.debug("Get Content Type Config from site '{}' for children path '{}'", siteId, fullPath);
        RepositoryItem[] folders = contentRepository.getContentChildren(siteId, fullPath);
        if (folders != null) {
            for (int i = 0; i < folders.length; i++) {
                if (folders[i].isFolder) {
                    String configPath =
                            folders[i].path + FILE_SEPARATOR + folders[i].name + FILE_SEPARATOR + getConfigFileName();
                    if (contentService.contentExists(siteId, configPath)) {
                        ContentTypeConfigTO config = reloadConfiguration(siteId, configPath
                                        .replace(contentTypesRootPath, "")
                                        .replace(FILE_SEPARATOR + getConfigFileName(), ""));
                        if (config != null) {
                            contentTypes.add(config);
                        }
                    }
                    // traverse the children file-folder structure
                    reloadContentTypeConfigForChildren(siteId, folders[i], contentTypes);
                }
            }
        }
    }

    /**
     * Reload content type configuration
     * @param siteId site identifier
     * @param contentTypeId content type Id
     * @return {@link ContentTypeConfigTO} object
     */
    protected ContentTypeConfigTO reloadConfiguration(String siteId, String contentTypeId) {
        return loadContentTypeConfiguration(siteId, contentTypeId);
    }

    /**
     * Get allowed content types for a path
     * @param siteId site identifier
     * @param relativePath relative path to get allowed content types
     * @return list of {@link ContentTypeConfigTO} objects
     * @throws SiteNotFoundException
     */
    protected List<ContentTypeConfigTO> getAllowedContentTypesForPath(String siteId, String relativePath) throws SiteNotFoundException {
        String user = securityService.getCurrentUser();
        Set<String> userRoles = securityService.getUserRoles(siteId, user);
        List<ContentTypeConfigTO> allContentTypes = getAllContentTypes(siteId);

        if (CollectionUtils.isNotEmpty(allContentTypes)) {
            List<ContentTypeConfigTO> contentTypes = new ArrayList<>();
            for (ContentTypeConfigTO contentTypeConfig : allContentTypes) {
                // check if the path matches one of includes paths
                if (CollectionUtils.isNotEmpty(contentTypeConfig.getPathIncludes())) {
                    for (String pathIncludes : contentTypeConfig.getPathIncludes()) {
                        if (relativePath.matches(pathIncludes)) {
                            logger.trace("In site '{}' path '{}' matches '{}'", siteId, relativePath, pathIncludes);
                            boolean isMatch = true;
                            if (contentTypeConfig.getPathExcludes() != null) {
                                for (String excludePath : contentTypeConfig.getPathExcludes()) {
                                    if (relativePath.matches(excludePath)) {
                                        logger.trace("In site '{}' path '{}' matches an exclude path '{}'",
                                                siteId, relativePath, excludePath);
                                        isMatch = false;
                                        break;
                                    }
                                }
                            }
                            if (isMatch) {
                                // if a match is found, populate the content type information
                                addContentTypes(userRoles, contentTypeConfig, contentTypes);
                            }
                        }
                    }
                } else if (CollectionUtils.isEmpty(contentTypeConfig.getPathExcludes())) {
                    addContentTypes(userRoles, contentTypeConfig, contentTypes);
                }
            }
            return contentTypes;
        } else {
            logger.error("No content type path configuration is found for site '{}'", siteId);
            return null;
        }
    }

    /**
     * Add a content type to a list if allowed
     * @param userRoles list of user roles
     * @param config {@link ContentTypeConfigTO} object to check
     * @param contentTypes list of {@link ContentTypeConfigTO} to add
     */
    protected void addContentTypes(Set<String> userRoles, ContentTypeConfigTO config,
                                   List<ContentTypeConfigTO> contentTypes) {
        boolean isAllowed = this.isUserAllowed(userRoles, config);
        if (isAllowed) {
            contentTypes.add(config);
        }
    }

    /**
     * Check if user is allowed to access given content type
     * @param userRoles list of user roles
     * @param item {@link ContentTypeConfigTO} object to check
     * @return true if allowed, false otherwise
     */
    protected boolean isUserAllowed(Set<String> userRoles, ContentTypeConfigTO item) {
        if (item == null) {
            logger.debug("No content type config provided for null item to limit user access, " +
                    "defaulting to permit the user");
            return true;
        }

        Set<String> allowedRoles = item.getAllowedRoles();
        logger.trace("Item '{}' allows roles '{}', checking against user roles '{}'",
                item.getName(), allowedRoles, userRoles);

        if (allowedRoles == null || allowedRoles.size() == 0) {
            logger.trace("User with roles '{}' is allowed access to '{}'", userRoles, item.getName());
            return true;
        }

        boolean notAllowed = Collections.disjoint(userRoles, allowedRoles);
        if (notAllowed) {
            logger.debug("Item '{}' is not allowed for user with roles '{}'",
                    item.getName(), userRoles);
            return false;
        }

        logger.trace("User with roles '{}' is allowed access to '{}'", userRoles, item.getName());
        return true;
    }

    /**
     * Get location where content types configuration files are stored for a site
     * @return content type path in String
     */
    protected String getConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_PATH);
    }

    /**
     * Get file name where configuration of content type is stored
     * @return file name in String
     */
    protected String getConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_FILE_NAME);
    }

    /**
     * Get location where content types are stored for a site
     * @return path in string
     */
    public String getSiteContentTypesConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH);
    }

    /**
     * get paths
     *
     * @param root element
     * @param path path to get
     * @return list of paths
     */
    private List<String> getPaths(Element root, String path) {
        List<String> paths;
        List<Node> nodes = root.selectNodes(path);
        if (nodes != null && nodes.size() > 0) {
            paths = new ArrayList<>(nodes.size());
            for (Node node : nodes) {
                String role = node.getText();
                if (!isEmpty(role)) {
                    paths.add(role);
                }
            }
        } else {
            paths = new ArrayList<>();
        }
        return paths;
    }

    /**
     * Load a list of allowed roles
     * @param config {@link ContentTypeConfigTO} configuration object
     * @param nodes list of {@link Node} to load role
     */
    protected void loadRoles(ContentTypeConfigTO config, List<Node> nodes) {
        Set<String> roles = null;
        if (nodes != null && nodes.size() > 0) {
            roles = new HashSet<>(nodes.size());
            for (Node node : nodes) {
                String role = node.getText();
                if (!isEmpty(role)) {
                    roles.add(role);
                }
            }
        } else {
            roles = new HashSet<>();
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
        List<DeleteDependencyConfigTO> deleteConfigs = new ArrayList<>();
        if (nodes != null) {
            for (Node node : nodes) {
                Node patternNode = node.selectSingleNode("pattern");
                Node removeFolderNode = node.selectSingleNode("remove-empty-folder");
                if (patternNode!=null) {
                    String pattern = patternNode.getText();
                    String removeEmptyFolder = removeFolderNode.getText();
                    boolean isRemoveEmptyFolder=false;
                    if (removeEmptyFolder != null) {
                        isRemoveEmptyFolder = Boolean.parseBoolean(removeEmptyFolder);
                    }
                    if (isNotEmpty(pattern)) {
                        DeleteDependencyConfigTO deleteConfigTO =
                                new DeleteDependencyConfigTO(pattern, isRemoveEmptyFolder);
                        deleteConfigs.add(deleteConfigTO);
                    }
                }
            }
            contentTypeConfig.setDeleteDependencies(deleteConfigs);
        }
    }

    /**
     * Load copy dependency patterns
     * @param config {@link ContentTypeConfigTO} object
     * @param copyDependencyNodes list of copy dependency nodes
     */
    protected void loadCopyDependencyPatterns(ContentTypeConfigTO config, List<Node> copyDependencyNodes) {
        List<CopyDependencyConfigTO> copyConfig = new ArrayList<>();
        if (copyDependencyNodes != null) {
            for (Node copyDependency : copyDependencyNodes) {
                Node patternNode = copyDependency.selectSingleNode("pattern");
                Node targetNode = copyDependency.selectSingleNode("target");
                if (patternNode!=null && targetNode!=null) {
                    String pattern = patternNode.getText();
                    String target = targetNode.getText();
                    if (isNotEmpty(pattern) && isNotEmpty(target)) {
                        CopyDependencyConfigTO copyDependencyConfigTO  = new CopyDependencyConfigTO(pattern,target);
                        copyConfig.add(copyDependencyConfigTO);
                    }
                }
            }
        }
        config.setCopyDepedencyPattern(copyConfig);
    }

    /**
     * Checks name for naming convention.
     * @param name Name to be checked
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

    protected String getContentTypePath(String contentType) {
        return normalize(contentTypeBasePathPattern.replaceFirst("\\{content-type}", contentType));
    }

    /**
     * Get preview image filename extract from form-definition.xml
     * @param siteId
     * @param contentTypeId
     * @return preview image filename
     * @throws ServiceLayerException
     */
    protected String getContentTypePreviewImageFilename(String siteId, String contentTypeId) throws ServiceLayerException {
        Document definition = getFormDefinitionDocument(siteId, contentTypeId);

        Node previewImageNode = definition.selectSingleNode(previewImageXPath);

        if (previewImageNode != null && isNotEmpty(previewImageNode.getText())) {
            return previewImageNode.getText();
        }

        return null;
    }

    /**
     * Get form-definition.xml as Document of a content type
     * @param siteId
     * @param contentTypeId
     * @return Document of form-definition.xml
     * @throws ServiceLayerException
     */
    protected Document getFormDefinitionDocument(String siteId, String contentTypeId) throws ServiceLayerException {
        siteService.checkSiteExists(siteId);

        String definitionPath = getContentTypePath(contentTypeId) + File.separator + contentTypeDefinitionFilename;
        Document definition = configurationService.getConfigurationAsDocument(siteId, null, definitionPath, null);

        if (definition == null) {
            throw new ContentNotFoundException(definitionPath, siteId, "Content-Type not found");
        }

        return definition;
    }

}
