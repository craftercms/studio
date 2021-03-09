/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.security;

import org.apache.commons.lang3.StringUtils;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_ASSET;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_COMPONENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIGURATION;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONFIG_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONTENT_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_DOCUMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FORM_DEFINITION;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_GROUP;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_LEVEL_DESCRIPTOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_PAGE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_RENDERING_TEMPLATE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_SCRIPT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_TAXONOMY;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_UNKNOWN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_USER;
import static org.craftercms.studio.api.v2.dal.ItemState.isDeleted;
import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflow;
import static org.craftercms.studio.api.v2.dal.ItemState.isLive;
import static org.craftercms.studio.api.v2.dal.ItemState.isModified;
import static org.craftercms.studio.api.v2.dal.ItemState.isNew;
import static org.craftercms.studio.api.v2.dal.ItemState.isPublishing;
import static org.craftercms.studio.api.v2.dal.ItemState.isScheduled;
import static org.craftercms.studio.api.v2.dal.ItemState.isStaged;
import static org.craftercms.studio.api.v2.dal.ItemState.isSystemProcessing;
import static org.craftercms.studio.api.v2.dal.ItemState.isTranslationInProgress;
import static org.craftercms.studio.api.v2.dal.ItemState.isTranslationPending;
import static org.craftercms.studio.api.v2.dal.ItemState.isTranslationUpToDate;
import static org.craftercms.studio.api.v2.dal.ItemState.isUserLocked;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_CHANGE_TYPE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_COPY;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_CREATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_CUT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DELETE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DELETE_CONTROLLER;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DELETE_TEMPLATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_DUPLICATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_EDIT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_EDIT_CONTROLLER;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_EDIT_TEMPLATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_GET_DEPENDENCIES;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_PASTE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_READ;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_READ_VERSION_HISTORY;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_RENAME;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_REVERT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_UPLOAD;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.FOLDER_CREATE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_APPROVE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_REQUEST;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_SCHEDULE;

public final class ContentItemPossibleActionsConstants {

    public static final long PAGE = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long ASSET = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_EDIT + CONTENT_RENAME + CONTENT_CUT +
            CONTENT_DUPLICATE + CONTENT_REVERT + CONTENT_DELETE  + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long COMPONENT = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long DOCUMENT = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_EDIT + CONTENT_RENAME + CONTENT_CUT +
            CONTENT_DUPLICATE + CONTENT_REVERT + CONTENT_DELETE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long RENDERING_TEMPLATE = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_EDIT + CONTENT_RENAME + CONTENT_CUT +
            CONTENT_DUPLICATE + CONTENT_REVERT + CONTENT_DELETE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long TAXONOMY = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_EDIT + CONTENT_RENAME + CONTENT_CUT +
            CONTENT_DUPLICATE + CONTENT_REVERT + CONTENT_DELETE  + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long CONTENT_TYPE = 0L;

    public static final long CONFIGURATION = 0L;

    public static final long FOLDER = CONTENT_READ + CONTENT_COPY + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + FOLDER_CREATE + CONTENT_DELETE + PUBLISH_APPROVE +
            PUBLISH_SCHEDULE;

    public static final long USER = 0L;

    public static final long GROUP = 0L;

    public static final long FORM_DEFINITION = 0L;

    public static final long SITE = 0L;

    public static final long REMOTE_REPOSITORY = 0L;

    public static final long CONFIG_FOLDER = 0L;

    public static final long SCRIPT = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long LEVEL_DESCRIPTOR = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_EDIT + CONTENT_RENAME + CONTENT_CUT +
            CONTENT_DUPLICATE + CONTENT_REVERT + CONTENT_DELETE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long UNKNOWN = 0L;

    // Semantics Matrix for available actions
    public static final long ITEM_STATE_NEW = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long ITEM_STATE_MODIFIED = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long ITEM_STATE_DELETED = 0L;

    public static final long ITEM_STATE_USER_LOCKED = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long ITEM_STATE_SYSTEM_PROCESSING = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + CONTENT_CREATE + CONTENT_PASTE + CONTENT_UPLOAD + CONTENT_DUPLICATE +
            FOLDER_CREATE;

    public static final long ITEM_STATE_IN_WORKFLOW = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long ITEM_STATE_SCHEDULED = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long ITEM_STATE_PUBLISHING = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE;

    public static final long ITEM_STATE_STAGED = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE + PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE;

    public static final long ITEM_STATE_LIVE = CONTENT_READ + CONTENT_COPY + CONTENT_READ_VERSION_HISTORY +
            CONTENT_GET_DEPENDENCIES + CONTENT_CREATE + CONTENT_PASTE + CONTENT_EDIT +
            CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE + CONTENT_REVERT +
            CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE + FOLDER_CREATE + CONTENT_DELETE +
            CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE;

    public static final long ITEM_STATE_TRANSLATION_UP_TO_DATE = 0L;

    public static final long ITEM_STATE_TRANSLATION_PENDING = 0L;

    public static final long ITEM_STATE_TRANSLATION_IN_PROGRESS = 0L;

    public static long getPossibleActionsForItemState(long itemState) {
        long result = 0L;
        if (isNew(itemState)) result = result | ITEM_STATE_NEW;
        if (isModified(itemState)) result = result | ITEM_STATE_MODIFIED;
        if (isDeleted(itemState)) result = result | ITEM_STATE_DELETED;
        if (isUserLocked(itemState)) result = result | ITEM_STATE_USER_LOCKED;
        if (isSystemProcessing(itemState)) result = result | ITEM_STATE_SYSTEM_PROCESSING;
        if (isInWorkflow(itemState)) result = result | ITEM_STATE_IN_WORKFLOW;
        if (isPublishing(itemState)) result = result | ITEM_STATE_PUBLISHING;
        if (isScheduled(itemState)) result = result | ITEM_STATE_SCHEDULED;
        if (isStaged(itemState)) result = result | ITEM_STATE_STAGED;
        if (isLive(itemState)) result = result | ITEM_STATE_LIVE;
        if (isTranslationUpToDate(itemState)) result = result | ITEM_STATE_TRANSLATION_UP_TO_DATE;
        if (isTranslationPending(itemState)) result = result | ITEM_STATE_TRANSLATION_PENDING;
        if (isTranslationInProgress(itemState)) result = result | ITEM_STATE_TRANSLATION_IN_PROGRESS;
        return result;
    }

    public static long getPossibleActionsForObject(String type) {
        if (StringUtils.isEmpty(type)) return 0L;
        long toRet;
        switch (type) {
            case CONTENT_TYPE_PAGE:
                toRet = PAGE;
                break;
            case CONTENT_TYPE_ASSET:
                toRet = ASSET;
                break;
            case CONTENT_TYPE_COMPONENT:
                toRet = COMPONENT;
                break;
            case CONTENT_TYPE_DOCUMENT:
                toRet = DOCUMENT;
                break;
            case CONTENT_TYPE_RENDERING_TEMPLATE:
                toRet = RENDERING_TEMPLATE;
                break;
            case CONTENT_TYPE_TAXONOMY:
                toRet = TAXONOMY;
                break;
            case CONTENT_TYPE_CONTENT_TYPE:
                toRet = CONTENT_TYPE;
                break;
            case CONTENT_TYPE_CONFIGURATION:
                toRet = CONFIGURATION;
                break;
            case CONTENT_TYPE_FOLDER:
                toRet = FOLDER;
                break;
            case CONTENT_TYPE_USER:
                toRet = USER;
                break;
            case CONTENT_TYPE_GROUP:
                toRet = GROUP;
                break;
            case CONTENT_TYPE_FORM_DEFINITION:
                toRet = FORM_DEFINITION;
                break;
            case CONTENT_TYPE_SITE:
                toRet = SITE;
                break;
            case CONTENT_TYPE_REMOTE_REPOSITORY:
                toRet = REMOTE_REPOSITORY;
                break;
            case CONTENT_TYPE_CONFIG_FOLDER:
                toRet = CONFIG_FOLDER;
                break;
            case CONTENT_TYPE_SCRIPT:
                toRet = SCRIPT;
                break;
            case CONTENT_TYPE_LEVEL_DESCRIPTOR:
                toRet = LEVEL_DESCRIPTOR;
                break;
            case CONTENT_TYPE_UNKNOWN:
            default:
                toRet = 0L;
                break;
        }
        return toRet;
    }

    private ContentItemPossibleActionsConstants() { }
}
