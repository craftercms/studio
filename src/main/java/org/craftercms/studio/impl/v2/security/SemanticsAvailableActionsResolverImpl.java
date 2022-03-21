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

package org.craftercms.studio.impl.v2.security;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.api.v2.security.SemanticsAvailableActionsResolver;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.List;

import static org.craftercms.studio.api.v1.constant.DmConstants.XML_PATTERN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.HOME_PAGE_PATH;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_DISPLAY_TEMPLATE;
import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflow;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_COPY;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_CUT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DELETE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DELETE_CONTROLLER;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DELETE_TEMPLATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DUPLICATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_EDIT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_EDIT_CONTROLLER;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_EDIT_TEMPLATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_READ_VERSION_HISTORY;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_RENAME;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_REVERT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_UPLOAD;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_APPROVE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_REJECT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_SCHEDULE;
import static org.craftercms.studio.api.v2.security.ContentItemPossibleActionsConstants.getPossibleActionsForItemState;
import static org.craftercms.studio.api.v2.security.ContentItemPossibleActionsConstants.getPossibleActionsForObject;

public class SemanticsAvailableActionsResolverImpl implements SemanticsAvailableActionsResolver {

    private static final Logger logger = LoggerFactory.getLogger(SemanticsAvailableActionsResolverImpl.class);

    private static final String CONTROLLER_PATH_FORMAT = "/scripts/%s/%s.groovy/";
    private static final String PAGE = "page";
    private static final String PAGES = "pages";

    private SecurityService securityService;
    private ContentServiceInternal contentServiceInternal;
    private ServicesConfig servicesConfig;
    private WorkflowServiceInternal workflowServiceInternal;
    private UserServiceInternal userServiceInternal;
    private StudioBlobStoreResolver studioBlobStoreResolver;
    private StudioConfiguration studioConfiguration;
    private ContentService contentService;

    @Override
    public long calculateContentItemAvailableActions(String username, String siteId, Item item)
            throws ServiceLayerException, UserNotFoundException {
        long userPermissionsBitmap = securityService.getAvailableActions(username, siteId, item.getPath());
        long systemTypeBitmap = getPossibleActionsForObject(item.getSystemType());
        long workflowStateBitmap = getPossibleActionsForItemState(item.getState(),
                StringUtils.equals(username, item.getLockOwner()));

        long result = (userPermissionsBitmap & systemTypeBitmap) & workflowStateBitmap;
        long toReturn = applySpecialUseCaseFilters(username, siteId, item.getPath(), item.getMimeType(),
                item.getSystemType(), item.getContentTypeId(), item.getModifier(), item.getState(), result);
        return toReturn;
    }

    @Override
    public long calculateContentItemAvailableActions(String username, String siteId, DetailedItem detailedItem)
            throws ServiceLayerException, UserNotFoundException {
        long userPermissionsBitmap = securityService.getAvailableActions(username, siteId, detailedItem.getPath());
        long systemTypeBitmap = getPossibleActionsForObject(detailedItem.getSystemType());
        long workflowStateBitmap = getPossibleActionsForItemState(detailedItem.getState(),
                StringUtils.equals(username, detailedItem.getLockOwner()));

        long result = (userPermissionsBitmap & systemTypeBitmap) & workflowStateBitmap;
        long toReturn = applySpecialUseCaseFilters(username, siteId, detailedItem.getPath(), detailedItem.getMimeType(),
                detailedItem.getSystemType(), detailedItem.getContentTypeId(), detailedItem.getSandbox().getModifier(),
                detailedItem.getState(),
                result);
        return toReturn;
    }

    private long applySpecialUseCaseFilters(String username, String siteId, String itemPath, String itemMimeType,
                                            String itemSystemType, String itemContentTypeId, String itemModifier,
                                            long itemState,
                                            long availableActions)
            throws ServiceLayerException, UserNotFoundException {
        long result = availableActions;

        if (StringUtils.equals(itemPath, HOME_PAGE_PATH)) {
            result = result & ~CONTENT_DELETE;
            result = result & ~CONTENT_CUT;
            result = result & ~CONTENT_RENAME;
            result = result & ~CONTENT_DUPLICATE;
            result = result & ~CONTENT_COPY;
        }

        List<String> protectedFolderPatterns = servicesConfig.getProtectedFolderPatterns(siteId);
        if (CollectionUtils.isNotEmpty(protectedFolderPatterns) &&
                ContentUtils.matchesPatterns(itemPath, protectedFolderPatterns)) {
            result = result & ~CONTENT_DELETE;
            result = result & ~CONTENT_CUT;
            result = result & ~CONTENT_RENAME;
        }

        if (studioBlobStoreResolver.isBlob(siteId, itemPath)) {
            result = result & ~CONTENT_READ_VERSION_HISTORY;
            result = result & ~CONTENT_REVERT;
        }

        if ((result & CONTENT_EDIT) > 0 && (!contentServiceInternal.isEditable(itemPath, itemMimeType))) {
            result = result & ~CONTENT_EDIT;
        }

        if ((result & CONTENT_UPLOAD) > 0 &&
                (!StringUtils.equals(itemSystemType, CONTENT_TYPE_FOLDER) ||
                        !StudioUtils.matchesPatterns(itemPath, servicesConfig.getAssetPatterns(siteId)))) {
            result = result & ~CONTENT_UPLOAD;
        }

        if (servicesConfig.isRequirePeerReview(siteId)) {
            if (StringUtils.equals(username, itemModifier)) {
                result = result & ~PUBLISH_SCHEDULE;
                result = result & ~PUBLISH;
            }

            if (isInWorkflow(itemState)) {
                WorkflowItem workflow = workflowServiceInternal.getWorkflowEntry(siteId, itemPath);
                User user = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (user.getId() == workflow.getId()) {
                    result = result & ~PUBLISH_APPROVE;
                    result = result & ~PUBLISH_SCHEDULE;
                    result = result & ~PUBLISH_REJECT;
                }
            }

            // controller and template
            String controllerPath = calculateControllerPath(itemContentTypeId);
            if (StringUtils.isNotEmpty(controllerPath)) {
                long controllerAvailableActions = securityService.getAvailableActions(username, siteId,
                        controllerPath);
                result = applyFilterForDependency(result, controllerAvailableActions, CONTENT_EDIT_CONTROLLER,
                        CONTENT_EDIT);
                result = applyFilterForDependency(result, controllerAvailableActions, CONTENT_DELETE_CONTROLLER,
                        CONTENT_DELETE);
            } else {
                result = result & ~CONTENT_EDIT_CONTROLLER;
                result = result & ~CONTENT_DELETE_CONTROLLER;
            }
            String templatePath = getTemplatePath(siteId, itemPath);
            if (StringUtils.isNotEmpty(templatePath)) {
                long templateAvailableActions = securityService.getAvailableActions(username, siteId,
                        templatePath);
                result = applyFilterForDependency(result, templateAvailableActions, CONTENT_EDIT_TEMPLATE,
                        CONTENT_EDIT);
                result = applyFilterForDependency(result, templateAvailableActions, CONTENT_DELETE_TEMPLATE,
                        CONTENT_DELETE);
            } else {
                result = result & ~CONTENT_EDIT_TEMPLATE;
                result = result & ~CONTENT_DELETE_TEMPLATE;
            }
        }

        return result;
    }

    private String calculateControllerPath(String contentTypeId) {
        if (StringUtils.isNotEmpty(contentTypeId)) {
            String[] tokens = StringUtils.split(contentTypeId, FILE_SEPARATOR);
            String type = tokens[tokens.length - 2];
            return String.format(CONTROLLER_PATH_FORMAT,
                    PAGE.equals(type) ? PAGES : type,
                    tokens[tokens.length - 1]);
        } else {
            return null;
        }
    }

    private String getTemplatePath(String siteId, String itemPath) {
        String templatePath = null;
        if (contentService.contentExists(siteId, itemPath) && StringUtils.endsWith(itemPath, XML_PATTERN)) {
            try {
                Document document = contentService.getContentAsDocument(siteId, itemPath);
                if (document != null) {
                    Element root = document.getRootElement();
                    templatePath = root.valueOf(DOCUMENT_ELM_DISPLAY_TEMPLATE);
                }
            } catch (DocumentException e) {
                logger.info("Could not get rendering template for content at path " + itemPath + " site " + siteId);
                templatePath = null;
            }
        }
        return templatePath;
    }

    private long applyFilterForDependency(long itemAvailableActions, long dependencyAvailableActions,
                                          long itemActionMask, long dependencyActionMask) {
        if ((dependencyAvailableActions & dependencyActionMask) > 0) {
            itemAvailableActions = itemAvailableActions & itemActionMask;
        } else {
            itemAvailableActions = itemAvailableActions & ~itemActionMask;
        }
        return itemAvailableActions;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public WorkflowServiceInternal getWorkflowServiceInternal() {
        return workflowServiceInternal;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public StudioBlobStoreResolver getStudioBlobStoreResolver() {
        return studioBlobStoreResolver;
    }

    public void setStudioBlobStoreResolver(StudioBlobStoreResolver studioBlobStoreResolver) {
        this.studioBlobStoreResolver = studioBlobStoreResolver;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
