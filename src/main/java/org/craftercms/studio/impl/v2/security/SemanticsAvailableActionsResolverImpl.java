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
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.api.v2.security.SemanticsAvailableActionsResolver;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.model.rest.content.DetailedItem;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.TOP_LEVEL_FOLDERS;
import static org.craftercms.studio.api.v2.dal.ItemState.USER_LOCKED;
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
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.ITEM_UNLOCK;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_APPROVE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_REJECT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_SCHEDULE;
import static org.craftercms.studio.api.v2.security.ContentItemPossibleActionsConstants.getPossibleActionsForItemState;
import static org.craftercms.studio.api.v2.security.ContentItemPossibleActionsConstants.getPossibleActionsForObject;

public class SemanticsAvailableActionsResolverImpl implements SemanticsAvailableActionsResolver {

    private org.craftercms.studio.api.v1.service.security.SecurityService securityServiceV1;
    private SecurityService securityService;
    private ContentServiceInternal contentServiceInternal;
    private ServicesConfig servicesConfig;
    private WorkflowServiceInternal workflowServiceInternal;
    private UserServiceInternal userServiceInternal;
    private StudioBlobStoreResolver studioBlobStoreResolver;
    private ContentTypeServiceInternal contentTypeServiceInternal;

    @Override
    public long calculateContentItemAvailableActions(String username, String siteId, Item item)
            throws ServiceLayerException, UserNotFoundException {
        long userPermissionsBitmap = securityService.getAvailableActions(username, siteId, item.getPath());
        long systemTypeBitmap = getPossibleActionsForObject(item.getSystemType());
        long workflowStateBitmap = getPossibleActionsForItemState(item.getState(),
                StringUtils.equals(username, item.getLockOwner()));

        long result = (userPermissionsBitmap & systemTypeBitmap) & workflowStateBitmap;
        return applySpecialUseCaseFilters(username, siteId, item.getPath(), item.getMimeType(),
                item.getSystemType(), item.getContentTypeId(), item.getModifier(), item.getState(), result);
    }

    @Override
    public long calculateContentItemAvailableActions(String username, String siteId, DetailedItem detailedItem)
            throws ServiceLayerException, UserNotFoundException {
        long userPermissionsBitmap = securityService.getAvailableActions(username, siteId, detailedItem.getPath());
        long systemTypeBitmap = getPossibleActionsForObject(detailedItem.getSystemType());
        long workflowStateBitmap = getPossibleActionsForItemState(detailedItem.getState(),
                StringUtils.equals(username, detailedItem.getLockOwner()));

        long result = (userPermissionsBitmap & systemTypeBitmap) & workflowStateBitmap;
        return applySpecialUseCaseFilters(username, siteId, detailedItem.getPath(), detailedItem.getMimeType(),
                detailedItem.getSystemType(), detailedItem.getContentTypeId(), detailedItem.getSandbox().getModifier(),
                detailedItem.getState(),
                result);
    }

    private long applySpecialUseCaseFilters(String username, String siteId, String itemPath, String itemMimeType,
                                            String itemSystemType, String itemContentTypeId, String itemModifier,
                                            long itemState,
                                            long availableActions)
            throws ServiceLayerException, UserNotFoundException {
        long result = availableActions;

        // The item is locked and the user is not the owner of the lock
        if ((itemState & USER_LOCKED.value) > 0 && (result & ITEM_UNLOCK) == 0) {
            // If the user is system_admin or site_admin, add the unlock action back
            if (securityServiceV1.isSiteAdmin(username, siteId)) {
                result |= ITEM_UNLOCK;
            }
        }

        if (RegexUtils.matchesAny(itemPath, TOP_LEVEL_FOLDERS)) {
            result &= ~CONTENT_DELETE;
            result &= ~CONTENT_CUT;
            result &= ~CONTENT_RENAME;
            result &= ~CONTENT_DUPLICATE;
            result &= ~CONTENT_COPY;
        }

        List<String> protectedFolderPatterns = servicesConfig.getProtectedFolderPatterns(siteId);
        if (CollectionUtils.isNotEmpty(protectedFolderPatterns) &&
                ContentUtils.matchesPatterns(itemPath, protectedFolderPatterns)) {
            result &= ~CONTENT_DELETE;
            result &= ~CONTENT_CUT;
            result &= ~CONTENT_RENAME;
        }

        if (studioBlobStoreResolver.isBlob(siteId, itemPath)) {
            result &= ~CONTENT_READ_VERSION_HISTORY;
            result &= ~CONTENT_REVERT;
        }

        if ((result & CONTENT_EDIT) > 0 && (!contentServiceInternal.isEditable(itemPath, itemMimeType))) {
            result &= ~CONTENT_EDIT;
        }

        if ((result & CONTENT_UPLOAD) > 0 &&
                (!StringUtils.equals(itemSystemType, CONTENT_TYPE_FOLDER) ||
                        !StudioUtils.matchesPatterns(itemPath, servicesConfig.getAssetPatterns(siteId)))) {
            result &= ~CONTENT_UPLOAD;
        }

        if (servicesConfig.isRequirePeerReview(siteId)) {
            if (StringUtils.equals(username, itemModifier)) {
                result &= ~PUBLISH_SCHEDULE;
                result &= ~PUBLISH;
            }

            if (isInWorkflow(itemState)) {
                WorkflowItem workflow = workflowServiceInternal.getWorkflowEntry(siteId, itemPath);
                User user = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (user.getId() == workflow.getId()) {
                    result &= ~PUBLISH_APPROVE;
                    result &= ~PUBLISH_SCHEDULE;
                    result &= ~PUBLISH_REJECT;
                }
            }
        }

        // controller and template
        if (isNotEmpty(itemContentTypeId)) {
            String controllerPath = contentTypeServiceInternal.getContentTypeControllerPath(itemContentTypeId);
            result = checkActionForDependency(siteId, username, controllerPath, result,
                    CONTENT_EDIT_CONTROLLER, CONTENT_EDIT, CONTENT_DELETE_CONTROLLER, CONTENT_DELETE);
            String templatePath = contentTypeServiceInternal.getContentTypeTemplatePath(siteId, itemContentTypeId);
            result = checkActionForDependency(siteId, username, templatePath, result,
                    CONTENT_EDIT_TEMPLATE, CONTENT_EDIT, CONTENT_DELETE_TEMPLATE, CONTENT_DELETE);
        }

        return result;
    }

    private long checkActionForDependency(String siteId, String username, String dependencyPath,
                                          long actions, long itemEditMask, long depEditMask,
                                          long itemDeleteMask, long depDeleteMask)
            throws UserNotFoundException, ServiceLayerException {
        if (isNotEmpty(dependencyPath)) {
            long depAvailableActions = securityService.getAvailableActions(username, siteId, dependencyPath);
            actions = updateForDependency(actions, depAvailableActions, itemEditMask, depEditMask);
            actions = updateForDependency(actions, depAvailableActions, itemDeleteMask, depDeleteMask);
        } else {
            actions &= ~itemEditMask;
            actions &= ~itemDeleteMask;
        }
        return actions;
    }

    private long updateForDependency(long itemActions, long dependencyActions, long itemActionMask,
                                     long dependencyActionMask) {
        // Check if the available actions for the dependency contain the required bit
        if ((dependencyActions & dependencyActionMask) > 0) {
            // If so, turn on the bit for the item too
            itemActions |= itemActionMask;
        } else {
            // Otherwise, turn off the bit for the item
            itemActions &= ~itemActionMask;
        }
        return itemActions;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setStudioBlobStoreResolver(StudioBlobStoreResolver studioBlobStoreResolver) {
        this.studioBlobStoreResolver = studioBlobStoreResolver;
    }

    public void setContentTypeServiceInternal(ContentTypeServiceInternal contentTypeServiceInternal) {
        this.contentTypeServiceInternal = contentTypeServiceInternal;
    }

    public void setSecurityServiceV1(org.craftercms.studio.api.v1.service.security.SecurityService securityServiceV1) {
        this.securityServiceV1 = securityServiceV1;
    }

}
