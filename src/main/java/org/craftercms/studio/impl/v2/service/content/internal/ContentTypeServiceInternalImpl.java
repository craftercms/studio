/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.model.contentType.ContentTypeUsage;
import org.dom4j.Document;
import org.dom4j.Node;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.lang3.RegExUtils.replaceAll;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_CREATE;

public class ContentTypeServiceInternalImpl implements ContentTypeServiceInternal {

    protected final ContentTypeService contentTypeService;
    protected final SecurityService securityService;
    protected final ConfigurationService configurationService;
    protected final ItemDAO itemDao;
    protected ContentService contentService;

    protected final String contentTypeBasePathPattern;
    protected final String contentTypesRootPath;
    protected final String contentTypeDefinitionFilename;
    protected final String contentTypeConfigFilename;
    protected final String templateXPath;
    protected final String controllerPattern;
    protected final String controllerFormat;
    protected final String previewImageXPath;
    protected final String defaultPreviewImagePath;
    private final GitRepositoryHelper gitRepositoryHelper;

    @ConstructorProperties({"contentTypeService", "securityService", "configurationService", "itemDao",
            "contentTypeBasePathPattern", "contentTypeDefinitionFilename", "contentTypeConfigFilename",
            "contentTypesRootPath",
            "templateXPath", "controllerPattern", "controllerFormat", "previewImageXPath", "defaultPreviewImagePath",
            "gitRepositoryHelper"})
    public ContentTypeServiceInternalImpl(ContentTypeService contentTypeService, SecurityService securityService,
                                          ConfigurationService configurationService, ItemDAO itemDao, String contentTypeBasePathPattern,
                                          String contentTypeDefinitionFilename, String contentTypeConfigFilename,
                                          String contentTypesRootPath, String templateXPath,
                                          String controllerPattern, String controllerFormat,
                                          String previewImageXPath, String defaultPreviewImagePath,
                                          GitRepositoryHelper gitRepositoryHelper) {
        this.contentTypeService = contentTypeService;
        this.securityService = securityService;
        this.configurationService = configurationService;
        this.itemDao = itemDao;
        this.contentTypeBasePathPattern = contentTypeBasePathPattern;
        this.contentTypeDefinitionFilename = contentTypeDefinitionFilename;
        this.contentTypeConfigFilename = contentTypeConfigFilename;
        this.contentTypesRootPath = contentTypesRootPath;
        this.templateXPath = templateXPath;
        this.controllerPattern = controllerPattern;
        this.controllerFormat = controllerFormat;
        this.previewImageXPath = previewImageXPath;
        this.defaultPreviewImagePath = defaultPreviewImagePath;
        this.gitRepositoryHelper = gitRepositoryHelper;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) {
        return contentTypeService.getAllContentTypes(siteId, true).stream()
                .filter(ContentTypeConfigTO::isQuickCreate)
                .filter(contentType ->
                    securityService.getUserPermissions(siteId, contentType.getQuickCreatePath(), securityService.getCurrentUser())
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
        if(isNotEmpty(template)) {
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

        if(templateNode != null && isNotEmpty(templateNode.getText())) {
            return templateNode.getText();
        }

        return null;
    }

    @Override
    @RequireSiteExists
    public Collection<String> getAllModelDefinitions(@SiteId String site) throws ServiceLayerException {
        List<String> modelDefinitions = new LinkedList<>();

        Path repoRootPath = gitRepositoryHelper.buildRepoPath(SANDBOX, site);
        Path contentTypesRepoPath = repoRootPath.resolve(gitRepositoryHelper.getGitPath(contentTypesRootPath));
        try {
            walkFileTree(contentTypesRepoPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return visitContentTypeFile(file, modelDefinitions);
                }
            });
        } catch (IOException e) {
            throw new ServiceLayerException(format("Failed to retrieve content types for site '%s'", site), e);
        }

        return modelDefinitions;
    }

    @NotNull
    private FileVisitResult visitContentTypeFile(final Path file, final List<String> contentTypes) throws IOException {
        if (!file.getFileName().toString().equals(contentTypeDefinitionFilename)) {
            return FileVisitResult.CONTINUE;
        }
        contentTypes.add(Files.readString(file));
        return FileVisitResult.SKIP_SIBLINGS;
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
    @RequireSiteExists
    protected Document getFormDefinitionDocument(@SiteId String siteId, String contentTypeId) throws ServiceLayerException {
        String definitionPath = getContentTypePath(contentTypeId) + File.separator + contentTypeDefinitionFilename;
        Document definition = configurationService.getConfigurationAsDocument(siteId, null, definitionPath, null);

        if (definition == null) {
            throw new ContentNotFoundException(definitionPath, siteId, "Content-Type not found");
        }

        return definition;
    }

}
