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

package org.craftercms.studio.permissions;

public interface StudioPermissions {

    /** Publish Service */
    String ACTION_GET_PUBLISHING_QUEUE = "get_publishing_queue";
    String ACTION_CANCEL_PUBLISH = "cancel_publish";

    /** Encryption service */
    String ACTION_ENCRYPTION_TOOL = "encryption_tool";

    /** Content Service */
    String ACTION_DELETE_CONTENT = "delete_content";

    /** Sites Service */
    String ACTION_PUBLISH_STATUS = "publish_status";
    String ACTION_PUBLISH_CLEAR_LOCK = "publish_clear_lock";
}
