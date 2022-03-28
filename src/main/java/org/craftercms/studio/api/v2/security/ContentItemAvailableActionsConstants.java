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

package org.craftercms.studio.api.v2.security;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.util.List;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_COPY;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_CREATE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_WRITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_FOLDER_CREATE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_ITEM_UNLOCK;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_PUBLISH;

public final class ContentItemAvailableActionsConstants {

    private static final Logger logger = LoggerFactory.getLogger(ContentItemAvailableActionsConstants.class);

    // Constants number required by annotations
    // Editorial
    public static final long CONTENT_READ =
            0b0000000000000000000000000000000000000000000000000000000000000001L;
    public static final long CONTENT_COPY =
            0b0000000000000000000000000000000000000000000000000000000000000010L;
    public static final long CONTENT_READ_VERSION_HISTORY =
            0b0000000000000000000000000000000000000000000000000000000000000100L;
    public static final long CONTENT_GET_DEPENDENCIES =
            0b0000000000000000000000000000000000000000000000000000000000001000L;
    public static final long PUBLISH_REQUEST =
            0b0000000000000000000000000000000000000000000000000000000000010000L;
    public static final long CONTENT_CREATE =
            0b0000000000000000000000000000000000000000000000000000000000100000L;
    public static final long CONTENT_PASTE =
            0b0000000000000000000000000000000000000000000000000000000001000000L;
    public static final long CONTENT_EDIT =
            0b0000000000000000000000000000000000000000000000000000000010000000L;
    public static final long CONTENT_RENAME =
            0b0000000000000000000000000000000000000000000000000000000100000000L;
    public static final long CONTENT_CUT =
            0b0000000000000000000000000000000000000000000000000000001000000000L;
    public static final long CONTENT_UPLOAD =
            0b0000000000000000000000000000000000000000000000000000010000000000L;
    public static final long CONTENT_DUPLICATE =
            0b0000000000000000000000000000000000000000000000000000100000000000L;
    public static final long CONTENT_CHANGE_TYPE =
            0b0000000000000000000000000000000000000000000000000001000000000000L;
    public static final long CONTENT_REVERT =
            0b0000000000000000000000000000000000000000000000000010000000000000L;
    public static final long CONTENT_EDIT_CONTROLLER =
            0b0000000000000000000000000000000000000000000000000100000000000000L;
    public static final long CONTENT_EDIT_TEMPLATE =
            0b0000000000000000000000000000000000000000000000001000000000000000L;
    public static final long FOLDER_CREATE =
            0b0000000000000000000000000000000000000000000000010000000000000000L;
    public static final long CONTENT_DELETE =
            0b0000000000000000000000000000000000000000000000100000000000000000L;
    public static final long CONTENT_DELETE_CONTROLLER =
            0b0000000000000000000000000000000000000000000001000000000000000000L;
    public static final long CONTENT_DELETE_TEMPLATE =
            0b0000000000000000000000000000000000000000000010000000000000000000L;
    public static final long PUBLISH =
            0b0000000000000000000000000000000000000000000100000000000000000000L;
    public static final long PUBLISH_APPROVE =
            0b0000000000000000000000000000000000000000001000000000000000000000L;
    public static final long PUBLISH_SCHEDULE =
            0b0000000000000000000000000000000000000000010000000000000000000000L;
    public static final long PUBLISH_REJECT =
            0b0000000000000000000000000000000000000000100000000000000000000000L;
    public static final long ITEM_UNLOCK =
            0b0000000000000000000000000000000000000001000000000000000000000000L;

    // Reserved
    public static final long CONTENT_RESERVED_39 =
            0b0000000000000000000000000000000000000010000000000000000000000000L;
    public static final long CONTENT_RESERVED_38 =
            0b0000000000000000000000000000000000000100000000000000000000000000L;
    public static final long CONTENT_RESERVED_37 =
            0b0000000000000000000000000000000000001000000000000000000000000000L;
    public static final long CONTENT_RESERVED_36 =
            0b0000000000000000000000000000000000010000000000000000000000000000L;
    public static final long CONTENT_RESERVED_35 =
            0b0000000000000000000000000000000000100000000000000000000000000000L;
    public static final long CONTENT_RESERVED_34 =
            0b0000000000000000000000000000000001000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_33 =
            0b0000000000000000000000000000000010000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_32 =
            0b0000000000000000000000000000000100000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_31 =
            0b0000000000000000000000000000001000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_30 =
            0b0000000000000000000000000000010000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_29 =
            0b0000000000000000000000000000100000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_28 =
            0b0000000000000000000000000001000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_27 =
            0b0000000000000000000000000010000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_26 =
            0b0000000000000000000000000100000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_25 =
            0b0000000000000000000000001000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_24 =
            0b0000000000000000000000010000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_23 =
            0b0000000000000000000000100000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_22 =
            0b0000000000000000000001000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_21 =
            0b0000000000000000000010000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_20 =
            0b0000000000000000000100000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_19 =
            0b0000000000000000001000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_18 =
            0b0000000000000000010000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_17 =
            0b0000000000000000100000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_16 =
            0b0000000000000001000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_15 =
            0b0000000000000010000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_14 =
            0b0000000000000100000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_13 =
            0b0000000000001000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_12 =
            0b0000000000010000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_11 =
            0b0000000000100000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_10 =
            0b0000000001000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_9 =
            0b0000000010000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_8 =
            0b0000000100000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_7 =
            0b0000001000000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_6 =
            0b0000010000000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_5 =
            0b0000100000000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_4 =
            0b0001000000000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_3 =
            0b0010000000000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_2 =
            0b0100000000000000000000000000000000000000000000000000000000000000L;
    public static final long CONTENT_RESERVED_1 =
            0b1000000000000000000000000000000000000000000000000000000000000000L;

    // Map permissions to available actions
    // content_read
    public static final long BITMAP_CONTENT_READ =
            CONTENT_READ + CONTENT_READ_VERSION_HISTORY + CONTENT_GET_DEPENDENCIES + PUBLISH_REQUEST;
    // content_create
    public static final long BITMAP_CONTENT_CREATE =
            CONTENT_CREATE + CONTENT_PASTE;
    // content_write
    public static final long BITMAP_CONTENT_WRITE =
            CONTENT_EDIT + CONTENT_RENAME + CONTENT_CUT + CONTENT_UPLOAD + CONTENT_DUPLICATE + CONTENT_CHANGE_TYPE +
                    CONTENT_REVERT + CONTENT_EDIT_CONTROLLER + CONTENT_EDIT_TEMPLATE;
    // folder_create
    public static final long BITMAP_FOLDER_CREATE =
            FOLDER_CREATE;
    // content_delete
    public static final long BITMAP_CONTENT_DELETE =
            CONTENT_DELETE + CONTENT_DELETE_CONTROLLER + CONTENT_DELETE_TEMPLATE;
    // publish
    public static final long BITMAP_PUBLISH =
            PUBLISH + PUBLISH_APPROVE + PUBLISH_SCHEDULE + PUBLISH_REJECT;
    // item_unlock
    public static final long BITMAP_ITEM_UNLOCK =
            ITEM_UNLOCK;

    public static final long BITMAP_UNDEFINED = 0L;

    private ContentItemAvailableActionsConstants() {
    }

    public static long mapPermissionToContentItemAvailableActions(String permission) {
        long result = 0L;
        switch (permission.toLowerCase()) {
            case PERMISSION_CONTENT_READ:
                result = BITMAP_CONTENT_READ;
                break;
            case PERMISSION_CONTENT_COPY:
                result = CONTENT_COPY;
                break;
            case PERMISSION_CONTENT_CREATE:
                result = BITMAP_CONTENT_CREATE;
                break;
            case PERMISSION_CONTENT_WRITE:
                result = BITMAP_CONTENT_WRITE;
                break;
            case PERMISSION_FOLDER_CREATE:
                result = BITMAP_FOLDER_CREATE;
                break;
            case PERMISSION_CONTENT_DELETE:
                result = BITMAP_CONTENT_DELETE;
                break;
            case PERMISSION_PUBLISH:
                result = BITMAP_PUBLISH;
                break;
            case PERMISSION_ITEM_UNLOCK:
                result = BITMAP_ITEM_UNLOCK;
                break;
            default:
                logger.debug("Permission " + permission + " not declared with content item available actions");
                result = BITMAP_UNDEFINED;
                break;
        }
        return result;
    }

    public static long mapPermissionsToContentItemAvailableActions(List<String> permissions) {
        return permissions.stream()
                .mapToLong(ContentItemAvailableActionsConstants::mapPermissionToContentItemAvailableActions)
                .reduce(0L, (a, b) -> a | b);
    }

}
