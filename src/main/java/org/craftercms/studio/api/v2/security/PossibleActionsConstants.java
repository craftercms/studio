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
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.ADD_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.APPROVE_PUBLISH;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.BULK_PUBLISH;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CANCEL_PUBLISH;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_COPY;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_CREATE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_CUT;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_DELETE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_DUPLICATE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_MOVE_RENAME;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_PASTE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_READ;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_TRANSLATE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_UPDATE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.CONTENT_VERSION_HISTORY;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.PUBLISH_BY_COMMIT_ID;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.PULL_FROM_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.PUSH_TO_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.READ_PUBLISHING_QUEUE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.READ_PUBLISHING_STATUS;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.READ_SITE_LOG;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.REJECT_PUBLISH;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.REMOVE_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.REQUEST_PUBLISH;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.RESOLVE_CONFLICTS;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.SET_WORKFLOW_STATE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.START_PUBLISHING;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.STOP_PUBLISHING;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.SYSTEM_CREATE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.SYSTEM_DELETE;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.SYSTEM_READ;
import static org.craftercms.studio.api.v2.security.AvailableActionsConstants.SYSTEM_UPDATE;

public final class PossibleActionsConstants {

    public static final long PAGE =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_TRANSLATE +
                    CONTENT_VERSION_HISTORY + REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH +
                    BULK_PUBLISH + SET_WORKFLOW_STATE;
    public static final long ASSET =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_TRANSLATE +
                    CONTENT_VERSION_HISTORY + REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH +
                    BULK_PUBLISH + SET_WORKFLOW_STATE;
    public static final long COMPONENT =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_TRANSLATE +
                    CONTENT_VERSION_HISTORY + REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH +
                    BULK_PUBLISH + SET_WORKFLOW_STATE;;
    public static final long DOCUMENT =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_TRANSLATE +
                    CONTENT_VERSION_HISTORY + REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH +
                    BULK_PUBLISH + SET_WORKFLOW_STATE;
    public static final long RENDERING_TEMPLATE =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_VERSION_HISTORY +
                    REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH + BULK_PUBLISH +
                    SET_WORKFLOW_STATE;
    public static final long TAXONOMY =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_TRANSLATE +
                    CONTENT_VERSION_HISTORY + REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH +
                    BULK_PUBLISH + SET_WORKFLOW_STATE;
    public static final long CONTENT_TYPE =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE +
            CONTENT_VERSION_HISTORY;
    public static final long CONFIGURATION = CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE +
            CONTENT_VERSION_HISTORY;
    public static final long FOLDER =
            CONTENT_READ + CONTENT_CREATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY + CONTENT_PASTE +
                    CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + BULK_PUBLISH + SET_WORKFLOW_STATE;
    public static final long USER =
            SYSTEM_CREATE + SYSTEM_READ + SYSTEM_UPDATE + SYSTEM_DELETE;
    public static final long GROUP =
            SYSTEM_CREATE + SYSTEM_READ + SYSTEM_UPDATE + SYSTEM_DELETE;
    public static final long FORM_DEFINITION =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_VERSION_HISTORY;
    public static final long SITE =
            BULK_PUBLISH + READ_PUBLISHING_QUEUE + READ_PUBLISHING_STATUS + START_PUBLISHING + STOP_PUBLISHING +
                    PUBLISH_BY_COMMIT_ID + READ_SITE_LOG + ADD_REMOTE_REPOSITORY + REMOVE_REMOTE_REPOSITORY +
                    PULL_FROM_REMOTE_REPOSITORY + PUSH_TO_REMOTE_REPOSITORY + RESOLVE_CONFLICTS + SYSTEM_CREATE +
                    SYSTEM_READ + SYSTEM_UPDATE + SYSTEM_DELETE;
    public static final long REMOTE_REPOSITORY =
            ADD_REMOTE_REPOSITORY + REMOVE_REMOTE_REPOSITORY + PULL_FROM_REMOTE_REPOSITORY + PUSH_TO_REMOTE_REPOSITORY;
    public static final long CONFIG_FOLDER =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_DUPLICATE;
    public static final long SCRIPT =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_TRANSLATE +
                    CONTENT_VERSION_HISTORY + REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH +
                    BULK_PUBLISH + SET_WORKFLOW_STATE;
    public static long LEVEL_DESCRIPTOR =
            CONTENT_READ + CONTENT_CREATE + CONTENT_UPDATE + CONTENT_DELETE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE + CONTENT_TRANSLATE +
                    CONTENT_VERSION_HISTORY + REQUEST_PUBLISH + APPROVE_PUBLISH + REJECT_PUBLISH + CANCEL_PUBLISH +
                    BULK_PUBLISH + SET_WORKFLOW_STATE;
    public static final long UNKNOWN = 0L;

    public static long getPosibleActionsForObject(String type) {
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

    private PossibleActionsConstants() { }
}
