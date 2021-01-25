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

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.util.List;

import static org.craftercms.studio.permissions.StudioPermissions.ACTION_ADD_REMOTE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_AUDIT_LOG;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CANCEL_FAILED_PULL;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CANCEL_PUBLISH;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CHANGE_CONTENT_TYPE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CLONE_CONTENT_CMIS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_COMMIT_RESOLUTION;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CREATE_CLUSTER;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CREATE_CONTENT;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CREATE_FOLDER;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CREATE_GROUPS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CREATE_SITE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_CREATE_USERS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_DELETE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_DELETE_CLUSTER;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_DELETE_CONTENT;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_DELETE_GROUPS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_DELETE_USERS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_EDIT_SITE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_ENCRYPTION_TOOL;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_GET_CHILDREN;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_GET_PUBLISHING_QUEUE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_LIST_CMIS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_LIST_REMOTES;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_PUBLISH;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_PULL_FROM_REMOTE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_PUSH_TO_REMOTE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_READ;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_READ_CLUSTER;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_READ_GROUPS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_READ_LOGS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_READ_USERS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_REBUILD_DATABASE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_REMOVE_REMOTE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_RESOLVE_CONFLICT;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_S3_READ;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_S3_WRITE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_SEARCH_CMIS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_SITE_DIFF_CONFLICTED_FILE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_SITE_STATUS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_UPDATE_CLUSTER;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_UPDATE_GROUPS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_UPDATE_USERS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_UPLOAD_CONTENT_CMIS;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_WEBDAV_READ;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_WEBDAV_WRITE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_WRITE;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_WRITE_CONFIGURATION;
import static org.craftercms.studio.permissions.StudioPermissions.ACTION_WRITE_GLOBAL_CONFIGURATION;

public final class AvailableActions {

    private static final Logger logger = LoggerFactory.getLogger(AvailableActions.class);

    private AvailableActions() {
    }

    // Constants number required by annotations
    // Editorial
    public static final long CONTENT_CREATE =
            0b0000000000000000000000000000000000000000000000000000000000000001L;
    public static final long CONTENT_UPDATE =
            0b0000000000000000000000000000000000000000000000000000000000000010L;
    public static final long CONTENT_DELETE =
            0b0000000000000000000000000000000000000000000000000000000000000100L;
    public static final long CONTENT_CUT =
            0b0000000000000000000000000000000000000000000000000000000000001000L;
    public static final long CONTENT_COPY =
            0b0000000000000000000000000000000000000000000000000000000000010000L;
    public static final long CONTENT_PASTE =
            0b0000000000000000000000000000000000000000000000000000000000100000L;
    public static final long CONTENT_MOVE_RENAME =
            0b0000000000000000000000000000000000000000000000000000000001000000L;
    public static final long CONTENT_DUPLICATE =
            0b0000000000000000000000000000000000000000000000000000000010000000L;
    public static final long CONTENT_TRANSLATE =
            0b0000000000000000000000000000000000000000000000000000000100000000L;
    public static final long CONTENT_RESERVED_1 =
            0b0000000000000000000000000000000000000000000000000000001000000000L;
    public static final long CONTENT_RESERVED_2 =
            0b0000000000000000000000000000000000000000000000000000010000000000L;
    public static final long CONTENT_RESERVED_3 =
            0b0000000000000000000000000000000000000000000000000000100000000000L;

    // Publishing
    public static final long REQUEST_PUBLISH =
            0b0000000000000000000000000000000000000000000000000001000000000000L;
    public static final long APPROVE_PUBLISH =
            0b0000000000000000000000000000000000000000000000000010000000000000L;
    public static final long REJECT_PUBLISH =
            0b0000000000000000000000000000000000000000000000000100000000000000L;
    public static final long CANCEL_PUBLISH =
            0b0000000000000000000000000000000000000000000000001000000000000000L;
    public static final long BULK_PUBLISH =
            0b0000000000000000000000000000000000000000000000010000000000000000L;
    public static final long READ_PUBLISHING_QUEUE =
            0b0000000000000000000000000000000000000000000000100000000000000000L;
    public static final long READ_PUBLISHING_STATUS =
            0b0000000000000000000000000000000000000000000001000000000000000000L;
    public static final long START_PUBLISHING =
            0b0000000000000000000000000000000000000000000010000000000000000000L;
    public static final long STOP_PUBLISHING =
            0b0000000000000000000000000000000000000000000100000000000000000000L;
    public static final long PUBLISH_BY_COMMIT_ID =
            0b0000000000000000000000000000000000000000001000000000000000000000L;
    public static final long PUBLISHING_RESERVED_1 =
            0b0000000000000000000000000000000000000000010000000000000000000000L;
    public static final long PUBLISHING_RESERVED_2 =
            0b0000000000000000000000000000000000000000100000000000000000000000L;

    // Site Admin
    public static final long SET_WORKFLOW_STATE =
            0b0000000000000000000000000000000000000001000000000000000000000000L;
    public static final long READ_AUDIT_LOG =
            0b0000000000000000000000000000000000000010000000000000000000000000L;
    public static final long READ_SITE_LOG =
            0b0000000000000000000000000000000000000100000000000000000000000000L;
    public static final long SITE_ADMIN_RESERVED_1 =
            0b0000000000000000000000000000000000001000000000000000000000000000L;
    public static final long SITE_ADMIN_RESERVED_2 =
            0b0000000000000000000000000000000000010000000000000000000000000000L;
    public static final long SITE_ADMIN_RESERVED_3 =
            0b0000000000000000000000000000000000100000000000000000000000000000L;
    public static final long SITE_ADMIN_RESERVED_4 =
            0b0000000000000000000000000000000001000000000000000000000000000000L;
    public static final long SITE_ADMIN_RESERVED_5 =
            0b0000000000000000000000000000000010000000000000000000000000000000L;

    // Site Git Operations
    public static final long ADD_REMOTE_REPOSITORY =
            0b0000000000000000000000000000000100000000000000000000000000000000L;
    public static final long REMOVE_REMOTE_REPOSITORY =
            0b0000000000000000000000000000001000000000000000000000000000000000L;
    public static final long PULL_FROM_REMOTE_REPOSITORY =
            0b0000000000000000000000000000010000000000000000000000000000000000L;
    public static final long PUSH_TO_REMOTE_REPOSITORY =
            0b0000000000000000000000000000100000000000000000000000000000000000L;
    public static final long RESOLVE_CONFLICTS =
            0b0000000000000000000000000001000000000000000000000000000000000000L;
    public static final long GIT_RESERVED_1 =
            0b0000000000000000000000000010000000000000000000000000000000000000L;
    public static final long GIT_RESERVED_2 =
            0b0000000000000000000000000100000000000000000000000000000000000000L;
    public static final long GIT_RESERVED_3 =
            0b0000000000000000000000001000000000000000000000000000000000000000L;

    // System Admin
    public static final long SYSTEM_CREATE =
            0b0000000000000000000000010000000000000000000000000000000000000000L;
    public static final long SYSTEM_READ =
            0b0000000000000000000000100000000000000000000000000000000000000000L;
    public static final long SYSTEM_UPDATE =
            0b0000000000000000000001000000000000000000000000000000000000000000L;
    public static final long SYSTEM_DELETE =
            0b0000000000000000000010000000000000000000000000000000000000000000L;
    public static final long READ_STUDIO_LOG_SETTINGS =
            0b0000000000000000000100000000000000000000000000000000000000000000L;
    public static final long UPDATE_STUDIO_LOG_SETTINGS =
            0b0000000000000000001000000000000000000000000000000000000000000000L;
    public static final long ADMIN_RESERVED_1 =
            0b0000000000000000010000000000000000000000000000000000000000000000L;
    public static final long ADMIN_RESERVED_2 =
            0b0000000000000000100000000000000000000000000000000000000000000000L;

    // Reserved
    public static final long RESERVED_1 =
            0b0000000000000001000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_2 =
            0b0000000000000010000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_3 =
            0b0000000000000100000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_4 =
            0b0000000000001000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_5 =
            0b0000000000010000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_6 =
            0b0000000000100000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_7 =
            0b0000000001000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_8 =
            0b0000000010000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_9 =
            0b0000000100000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_10 =
            0b0000001000000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_11 =
            0b0000010000000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_12 =
            0b0000100000000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_13 =
            0b0001000000000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_14 =
            0b0010000000000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_15 =
            0b0100000000000000000000000000000000000000000000000000000000000000L;
    public static final long RESERVED_16 =
            0b1000000000000000000000000000000000000000000000000000000000000000L;

    // Map permissions to available actions
    // add_remote
    public static final long ADD_REMOTE = ADD_REMOTE_REPOSITORY;
    // audit_log
    public static final long AUDIT_LOG = READ_AUDIT_LOG;
    // cancel_failed_pull
    public static final long CANCEL_FAILED_PULL = RESOLVE_CONFLICTS;
    // cancel_publish
    public static final long CANCEL_PUBLISH_PERMISSION = CANCEL_PUBLISH;
    // Change Content Type
    public static final long CHANGE_CONTENT_TYPE = CONTENT_UPDATE;
    // clone_content_cmis
    public static final long CLONE_CONTENT_CMIS = CONTENT_CREATE + CONTENT_UPDATE;
    // commit_resolution
    public static final long COMMIT_RESOLUTION = RESOLVE_CONFLICTS;
    // Create Content
    public static final long CREATE_CONTENT = CONTENT_CREATE;
    // Create Folder
    public static final long CREATE_FOLDER = CONTENT_CREATE;
    // create_cluster
    public static final long CREATE_CLUSTER = SYSTEM_CREATE;
    // create_groups
    public static final long CREATE_GROUPS = SYSTEM_CREATE;
    // create_users
    public static final long CREATE_USERS = SYSTEM_CREATE;
    // create-site
    public static final long CREATE_SITE = SYSTEM_CREATE;
    // Delete
    public static final long DELETE = CONTENT_DELETE;
    // delete_cluster
    public static final long DELETE_CLUSTER = SYSTEM_DELETE;
    // delete_content
    public static final long DELETE_CONTENT = CONTENT_DELETE;
    // delete_groups
    public static final long DELETE_GROUPS = SYSTEM_DELETE;
    // delete_users
    public static final long DELETE_USERS = SYSTEM_DELETE;
    // edit_site
    public static final long EDIT_SITE = SYSTEM_UPDATE;
    // encryption_tool
    public static final long ENCRYPTION_TOOL = CONTENT_UPDATE + SYSTEM_UPDATE;
    // get_children
    public static final long GET_CHILDREN = -1L;
    // get_publishing_queue
    public static final long GET_PUBLISHING_QUEUE = READ_PUBLISHING_QUEUE;
    // list_cmis
    public static final long LIST_CMIS = -1L;
    // list_remotes
    public static final long LIST_REMOTES = ADD_REMOTE_REPOSITORY;
    // Publish
    public static final long PUBLISH = APPROVE_PUBLISH + REJECT_PUBLISH;
    // pull_from_remote
    public static final long PULL_FROM_REMOTE = PULL_FROM_REMOTE_REPOSITORY;
    // push_to_remote
    public static final long PUSH_TO_REMOTE = PUSH_TO_REMOTE_REPOSITORY;
    // Read
    public static final long READ = -1L;
    // read_cluster
    public static final long READ_CLUSTER = SYSTEM_READ;
    // read_groups
    public static final long READ_GROUPS = SYSTEM_READ;
    // read_logs
    public static final long READ_LOGS = READ_SITE_LOG;
    // read_users
    public static final long READ_USERS = SYSTEM_READ;
    // rebuild_database
    public static final long REBUILD_DATABASE = SYSTEM_UPDATE;
    // remove_remote
    public static final long REMOVE_REMOTE = REMOVE_REMOTE_REPOSITORY;
    // resolve_conflict
    public static final long RESOLVE_CONFLICT = RESOLVE_CONFLICTS;
    // S3 Read
    public static final long S3_READ = -1L;
    // S3 Write
    public static final long S3_WRITE = CONTENT_CREATE + CONTENT_UPDATE;
    // search_cmis
    public static final long SEARCH_CMIS = -1L;
    // site_diff_conflicted_file
    public static final long SITE_DIFF_CONFLICTED_FILE = RESOLVE_CONFLICTS;
    // site_status
    public static final long SITE_STATUS = SYSTEM_READ;
    // update_cluster
    public static final long UPDATE_CLUSTER = SYSTEM_UPDATE;
    // update_groups
    public static final long UPDATE_GROUPS = SYSTEM_UPDATE;
    // update_users
    public static final long UPDATE_USERS = SYSTEM_UPDATE;
    // upload_content_cmis
    public static final long UPLOAD_CONTENT_CMIS = CONTENT_CREATE + CONTENT_UPDATE;
    // webdav_read
    public static final long WEBDAV_READ = -1L;
    // webdav_write
    public static final long WEBDAV_WRITE = CONTENT_CREATE + CONTENT_UPDATE;
    // Write
    public static final long WRITE =
            CONTENT_CREATE + CONTENT_UPDATE + CONTENT_CUT + CONTENT_COPY +
                    CONTENT_PASTE + CONTENT_MOVE_RENAME + CONTENT_DUPLICATE;
    // write_configuration
    public static final long WRITE_CONFIGURATION = CONTENT_CREATE + CONTENT_UPDATE;
    // write_global_configuration
    public static final long WRITE_GLOBAL_CONFIGURATION = SYSTEM_CREATE + SYSTEM_UPDATE;

    public static final long ALL_PERMISSIONS = -1L;

    public static long mapPermissionToAvailableActions(String permission) {
        long result = 0L;
        switch (permission.toLowerCase()) {
            case ACTION_ADD_REMOTE:
                result = ADD_REMOTE;
                break;
            case ACTION_AUDIT_LOG:
                result = AUDIT_LOG;
                break;
            case ACTION_CANCEL_FAILED_PULL:
                result = CANCEL_FAILED_PULL;
                break;
            case ACTION_CANCEL_PUBLISH:
                result = CANCEL_PUBLISH_PERMISSION;
                break;
            case ACTION_CHANGE_CONTENT_TYPE:
                result = CHANGE_CONTENT_TYPE;
                break;
            case ACTION_CLONE_CONTENT_CMIS:
                result = CLONE_CONTENT_CMIS;
                break;
            case ACTION_COMMIT_RESOLUTION:
                result = COMMIT_RESOLUTION;
                break;
            case ACTION_CREATE_CONTENT:
                result = CREATE_CONTENT;
                break;
            case ACTION_CREATE_FOLDER:
                result = CREATE_FOLDER;
                break;
            case ACTION_CREATE_CLUSTER:
                result = CREATE_CLUSTER;
                break;
            case ACTION_CREATE_GROUPS:
                result = CREATE_GROUPS;
                break;
            case ACTION_CREATE_USERS:
                result = CREATE_USERS;
                break;
            case ACTION_CREATE_SITE:
                result = CREATE_SITE;
                break;
            case ACTION_DELETE:
                result = DELETE;
                break;
            case ACTION_DELETE_CLUSTER:
                result = DELETE_CLUSTER;
                break;
            case ACTION_DELETE_CONTENT:
                result = DELETE_CONTENT;
                break;
            case ACTION_DELETE_GROUPS:
                result = DELETE_GROUPS;
                break;
            case ACTION_DELETE_USERS:
                result = DELETE_USERS;
                break;
            case ACTION_EDIT_SITE:
                result = EDIT_SITE;
                break;
            case ACTION_ENCRYPTION_TOOL:
                result = ENCRYPTION_TOOL;
                break;
            case ACTION_GET_CHILDREN:
                result = GET_CHILDREN;
                break;
            case ACTION_GET_PUBLISHING_QUEUE:
                result = GET_PUBLISHING_QUEUE;
                break;
            case ACTION_LIST_CMIS:
                result = LIST_CMIS;
                break;
            case ACTION_LIST_REMOTES:
                result = LIST_REMOTES;
                break;
            case ACTION_PUBLISH:
                result = PUBLISH;
                break;
            case ACTION_PULL_FROM_REMOTE:
                result = PULL_FROM_REMOTE;
                break;
            case ACTION_PUSH_TO_REMOTE:
                result = PUSH_TO_REMOTE;
                break;
            case ACTION_READ:
                result = READ;
                break;
            case ACTION_READ_CLUSTER:
                result = READ_CLUSTER;
                break;
            case ACTION_READ_GROUPS:
                result = READ_GROUPS;
                break;
            case ACTION_READ_LOGS:
                result = READ_LOGS;
                break;
            case ACTION_READ_USERS:
                result = READ_USERS;
                break;
            case ACTION_REBUILD_DATABASE:
                result = REBUILD_DATABASE;
                break;
            case ACTION_REMOVE_REMOTE:
                result = REMOVE_REMOTE;
                break;
            case ACTION_RESOLVE_CONFLICT:
                result = RESOLVE_CONFLICT;
                break;
            case ACTION_S3_READ:
                result = S3_READ;
                break;
            case ACTION_S3_WRITE:
                result = S3_WRITE;
                break;
            case ACTION_SEARCH_CMIS:
                result = SEARCH_CMIS;
                break;
            case ACTION_SITE_DIFF_CONFLICTED_FILE:
                result = SITE_DIFF_CONFLICTED_FILE;
                break;
            case ACTION_SITE_STATUS:
                result = SITE_STATUS;
                break;
            case ACTION_UPDATE_CLUSTER:
                result = UPDATE_CLUSTER;
                break;
            case ACTION_UPDATE_GROUPS:
                result = UPDATE_GROUPS;
                break;
            case ACTION_UPDATE_USERS:
                result = UPDATE_USERS;
                break;
            case ACTION_UPLOAD_CONTENT_CMIS:
                result = UPLOAD_CONTENT_CMIS;
                break;
            case ACTION_WEBDAV_READ:
                result = WEBDAV_READ;
                break;
            case ACTION_WEBDAV_WRITE:
                result = WEBDAV_WRITE;
                break;
            case ACTION_WRITE:
                result = WRITE;
                break;
            case ACTION_WRITE_CONFIGURATION:
                result = WRITE_CONFIGURATION;
                break;
            case ACTION_WRITE_GLOBAL_CONFIGURATION:
                result = WRITE_GLOBAL_CONFIGURATION;
                break;
            default:
                logger.warn("Permission " + permission + " not declared with available actions");
                break;
        }
        return result;
    }

    public static long mapPermissionsToAvailableActions(List<String> permissions) {
        return permissions.stream().mapToLong(AvailableActions::mapPermissionToAvailableActions).sum();
    }

}
