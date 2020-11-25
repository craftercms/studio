/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

public enum AvailableActions {

    // Editorial
    CONTENT_CREATE("Content: Create", 0),
    CONTENT_UPDATE("Content: Update", 1),
    CONTENT_DELETE("Content: Delete", 2),
    CONTENT_CUT("Content: Cut", 3),
    CONTENT_COPY("Content: Copy", 4),
    CONTENT_PASTE("Content: Paste", 5),
    CONTENT_MOVE_RENAME("Content: Move/Rename", 6),
    CONTENT_DUPLICATE("Content: Duplicate", 7),
    CONTENT_TRANSLATE("Content: Translate", 8),
    CONTENT_RESERVED_1("Content: Reserved 1", 9),
    CONTENT_RESERVED_2("Content: Reserved 2", 10),
    CONTENT_RESERVED_3("Content: Reserved 3", 11),

    // Publishing
    REQUEST_PUBLISH("Request Publish", 12),
    APPROVE_PUBLISH("Approve Publish", 13),
    REJECT_PUBLISH("Reject Publish", 14),
    CANCEL_PUBLISH("Cancel Publish", 15),
    BULK_PUBLISH("Bulk Publish", 16),
    READ_PUBLISHING_QUEUE("Read Publishing Queue", 17),
    READ_PUBLISHING_STATUS("Read Publishing Status", 18),
    START_PUBLISHING("Start Publishing", 19),
    STOP_PUBLISHING("Stop Publishing", 20),
    PUBLISH_BY_COMMIT_ID("Publish by Commit ID", 21),
    PUBLISHING_RESERVED_1("Publishing: Reserved 1", 22),
    PUBLISHING_RESERVED_2("Publishing: Reserved 2", 23),

    // Site Admin
    SET_WORKFLOW_STATE("Set Workflow State", 24),
    READ_AUDIT_LOG("Read Audit Log", 25),
    READ_SITE_LOG("Read Site Log", 26),
    SITE_ADMIN_RESERVED_1("Site Admin: Reserved 1", 27),
    SITE_ADMIN_RESERVED_2("Site Admin: Reserved 2", 28),
    SITE_ADMIN_RESERVED_3("Site Admin: Reserved 3", 29),
    SITE_ADMIN_RESERVED_4("Site Admin: Reserved 4", 30),
    SITE_ADMIN_RESERVED_5("Site Admin: Reserved 5", 31),

    // Site Git Operations
    ADD_REMOTE_REPOSITORY("Add Remote Repository", 32),
    REMOVE_REMOTE_REPOSITORY("Remove Remote Repository", 33),
    PULL_FROM_REMOTE_REPOSITORY("Pull From Remote Repository", 34),
    PUSH_TO_REMOTE_REPOSITORY("Push To Remote Repository", 35),
    RESOLVE_CONFLICTS("Resolve Conflicts", 36),
    GIT_RESERVED_1("Git: Reserved 1", 37),
    GIT_RESERVED_2("Git: Reserved 2", 38),
    GIT_RESERVED_3("Git: Reserved 3", 39),

    // System Admin
    SYSTEM_CREATE("System Create", 40),
    SYSTEM_READ("System Read", 41),
    SYSTEM_UPDATE("System Update", 42),
    SYSTEM_DELETE("System Delete", 43),
    READ_STUDIO_LOG_SETTINGS("Read Studio Log Settings", 44),
    UPDATE_STUDIO_LOG_SETTINGS("Update Studio Log Settings", 45),
    ADMIN_RESERVED_1("Admin: Reserved 1", 46),
    ADMIN_RESERVED_2("Admin: Reserved 2", 47),

    // Reserved
    RESERVED_1("Reserved 1", 48),
    RESERVED_2("Reserved 2", 49),
    RESERVED_3("Reserved 3", 50),
    RESERVED_4("Reserved 4", 51),
    RESERVED_5("Reserved 5", 52),
    RESERVED_6("Reserved 6", 53),
    RESERVED_7("Reserved 7", 54),
    RESERVED_8("Reserved 8", 55),
    RESERVED_9("Reserved 9", 56),
    RESERVED_10("Reserved 10", 57),
    RESERVED_11("Reserved 11", 58),
    RESERVED_12("Reserved 12", 59),
    RESERVED_13("Reserved 13", 60),
    RESERVED_14("Reserved 14", 61),
    RESERVED_15("Reserved 15", 62);

    public final long value;
    public final String label;

    AvailableActions(String label, long exponent) {
        this.value = Math.round(Math.pow(2, exponent));
        this.label = label;
    }

    private static final Logger logger = LoggerFactory.getLogger(AvailableActions.class);

    // Map permissions to available actions
    // add_remote
    public static final long ADD_REMOTE = ADD_REMOTE_REPOSITORY.value;
    // audit_log
    public static final long AUDIT_LOG = READ_AUDIT_LOG.value;
    public static final long AUDIT_LOG_CONST = Math.round(Math.pow(2, 25));
    // cancel_failed_pull
    public static final long CANCEL_FAILED_PULL = RESOLVE_CONFLICTS.value;
    // cancel_publish
    public static final long CANCEL_PUBLISH_PERMISSION = CANCEL_PUBLISH.value;
    // Change Content Type
    public static final long CHANGE_CONTENT_TYPE = CONTENT_UPDATE.value;
    // clone_content_cmis
    public static final long CLONE_CONTENT_CMIS = CONTENT_CREATE.value + CONTENT_UPDATE.value;
    // commit_resolution
    public static final long COMMIT_RESOLUTION = RESOLVE_CONFLICTS.value;
    // Create Content
    public static final long CREATE_CONTENT = CONTENT_CREATE.value;
    // Create Folder
    public static final long CREATE_FOLDER = CONTENT_CREATE.value;
    // create_cluster
    public static final long CREATE_CLUSTER = SYSTEM_CREATE.value;
    // create_groups
    public static final long CREATE_GROUPS = SYSTEM_CREATE.value;
    // create_users
    public static final long CREATE_USERS = SYSTEM_CREATE.value;
    // create-site
    public static final long CREATE_SITE = SYSTEM_CREATE.value;
    // Delete
    public static final long DELETE = CONTENT_DELETE.value;
    // delete_cluster
    public static final long DELETE_CLUSTER = SYSTEM_DELETE.value;
    // delete_content
    public static final long DELETE_CONTENT = CONTENT_DELETE.value;
    // delete_groups
    public static final long DELETE_GROUPS = SYSTEM_DELETE.value;
    // delete_users
    public static final long DELETE_USERS = SYSTEM_DELETE.value;
    // edit_site
    public static final long EDIT_SITE = SYSTEM_UPDATE.value;
    // encryption_tool
    public static final long ENCRYPTION_TOOL = CONTENT_UPDATE.value + SYSTEM_UPDATE.value;
    // get_children
    public static final long GET_CHILDREN = 0L;
    // get_publishing_queue
    public static final long GET_PUBLISHING_QUEUE = READ_PUBLISHING_QUEUE.value;
    // list_cmis
    public static final long LIST_CMIS = 0L;
    // list_remotes
    public static final long LIST_REMOTES = ADD_REMOTE_REPOSITORY.value;
    // Publish
    public static final long PUBLISH = APPROVE_PUBLISH.value + REJECT_PUBLISH.value;
    // pull_from_remote
    public static final long PULL_FROM_REMOTE = PULL_FROM_REMOTE_REPOSITORY.value;
    // push_to_remote
    public static final long PUSH_TO_REMOTE = PUSH_TO_REMOTE_REPOSITORY.value;
    // Read
    public static final long READ = 0L;
    // read_cluster
    public static final long READ_CLUSTER = SYSTEM_READ.value;
    // read_groups
    public static final long READ_GROUPS = SYSTEM_READ.value;
    // read_logs
    public static final long READ_LOGS = READ_SITE_LOG.value;
    // read_users
    public static final long READ_USERS = SYSTEM_READ.value;
    // rebuild_database
    public static final long REBUILD_DATABASE = SYSTEM_UPDATE.value;
    // remove_remote
    public static final long REMOVE_REMOTE = REMOVE_REMOTE_REPOSITORY.value;
    // resolve_conflict
    public static final long RESOLVE_CONFLICT = RESOLVE_CONFLICTS.value;
    // S3 Read
    public static final long S3_READ = 0L;
    // S3 Write
    public static final long S3_WRITE = CONTENT_CREATE.value + CONTENT_UPDATE.value;
    // search_cmis
    public static final long SEARCH_CMIS = 0L;
    // site_diff_conflicted_file
    public static final long SITE_DIFF_CONFLICTED_FILE = RESOLVE_CONFLICTS.value;
    // site_status
    public static final long SITE_STATUS = SYSTEM_READ.value;
    // update_cluster
    public static final long UPDATE_CLUSTER = SYSTEM_UPDATE.value;
    // update_groups
    public static final long UPDATE_GROUPS = SYSTEM_UPDATE.value;
    // update_users
    public static final long UPDATE_USERS = SYSTEM_UPDATE.value;
    // upload_content_cmis
    public static final long UPLOAD_CONTENT_CMIS = CONTENT_CREATE.value + CONTENT_UPDATE.value;
    // webdav_read
    public static final long WEBDAV_READ = 0L;
    // webdav_write
    public static final long WEBDAV_WRITE = CONTENT_CREATE.value + CONTENT_UPDATE.value;
    // Write
    public static final long WRITE =
            CONTENT_CREATE.value + CONTENT_UPDATE.value + CONTENT_CUT.value + CONTENT_COPY.value +
                    CONTENT_PASTE.value + CONTENT_MOVE_RENAME.value + CONTENT_DUPLICATE.value;
    // write_configuration
    public static final long WRITE_CONFIGURATION = CONTENT_CREATE.value + CONTENT_UPDATE.value;
    // write_global_configuration
    public static final long WRITE_GLOBAL_CONFIGURATION = SYSTEM_CREATE.value + SYSTEM_UPDATE.value;

    public static final long EVERYTHING_ALLOWED = -1L;

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


    // Constants number required by annotations
    public static final long CONTENT_CREATE_CONST_LONG = 1L;
    public static final long CONTENT_UPDATE_CONST_LONG = 2L;
    public static final long CONTENT_DELETE_CONST_LONG = 4L;
    public static final long CONTENT_CUT_CONST_LONG= 8L;
    public static final long CONTENT_COPY_CONST_LONG = 16L;
    public static final long CONTENT_PASTE_CONST_LONG= 32L;
    public static final long CONTENT_MOVE_RENAME_CONST_LONG = 64L;
    public static final long CONTENT_DUPLICATE_CONST_LONG = 128L;
    public static final long CONTENT_TRANSLATE_CONST_LONG = 256L;
    public static final long CONTENT_RESERVED_1_CONST_LONG = 512L;
    public static final long CONTENT_RESERVED_2_CONST_LONG = 1024L;
    public static final long CONTENT_RESERVED_3_CONST_LONG = 2048L;
    public static final long REQUEST_PUBLISH_CONST_LONG = 4096L;
    public static final long APPROVE_PUBLISH_CONST_LONG = 8192L;
    public static final long REJECT_PUBLISH_CONST_LONG = 16384L;
    public static final long CANCEL_PUBLISH_CONST_LONG = 32768L;
    public static final long BULK_PUBLISH_CONST_LONG = 65536L;
    public static final long READ_PUBLISHING_QUEUE_CONST_LONG = 131072L;
    public static final long READ_PUBLISHING_STATUS_CONST_LONG = 262144L;
    public static final long START_PUBLISHING_CONST_LONG = 524288L;
    public static final long STOP_PUBLISHING_CONST_LONG = 1048576L;
    public static final long PUBLISH_BY_COMMIT_ID_CONST_LONG = 2097152L;
    public static final long PUBLISHING_RESERVED_1_CONST_LONG = 4194304L;
    public static final long PUBLISHING_RESERVED_2_CONST_LONG = 8388608L;
    public static final long SET_WORKFLOW_STATE_CONST_LONG = 16777216L;
    public static final long READ_AUDIT_LOG_CONST_LONG = 33554432L;
    public static final long READ_SITE_LOG_CONST_LONG = 67108864L;
    public static final long SITE_ADMIN_RESERVED_1_CONST_LONG = 134217728L;
    public static final long SITE_ADMIN_RESERVED_2_CONST_LONG = 268435456L;
    public static final long SITE_ADMIN_RESERVED_3_CONST_LONG = 536870912L;
    public static final long SITE_ADMIN_RESERVED_4_CONST_LONG = 1073741824L;
    public static final long SITE_ADMIN_RESERVED_5_CONST_LONG = 2147483648L;
    public static final long ADD_REMOTE_REPOSITORY_CONST_LONG = 4294967296L;
    public static final long REMOVE_REMOTE_REPOSITORY_CONST_LONG = 8589934592L;
    public static final long PULL_FROM_REMOTE_REPOSITORY_CONST_LONG = 17179869184L;
    public static final long PUSH_TO_REMOTE_REPOSITORY_CONST_LONG = 34359738368L;
    public static final long RESOLVE_CONFLICTS_CONST_LONG = 68719476736L;
    public static final long GIT_RESERVED_1_CONST_LONG = 137438953472L;
    public static final long GIT_RESERVED_2_CONST_LONG = 274877906944L;
    public static final long GIT_RESERVED_3_CONST_LONG = 549755813888L;
    public static final long SYSTEM_CREATE_CONST_LONG = 1099511627776L;
    public static final long SYSTEM_READ_CONST_LONG = 2199023255552L;
    public static final long SYSTEM_UPDATE_CONST_LONG = 4398046511104L;
    public static final long SYSTEM_DELETE_CONST_LONG = 8796093022208L;
    public static final long READ_STUDIO_LOG_SETTINGS_CONST_LONG = 17592186044416L;
    public static final long UPDATE_STUDIO_LOG_SETTINGS_CONST_LONG = 35184372088832L;
    public static final long ADMIN_RESERVED_1_CONST_LONG = 70368744177664L;
    public static final long ADMIN_RESERVED_2_CONST_LONG = 140737488355328L;
    public static final long RESERVED_1_CONST_LONG = 281474976710656L;
    public static final long RESERVED_2_CONST_LONG = 562949953421312L;
    public static final long RESERVED_3_CONST_LONG = 1125899906842620L;
    public static final long RESERVED_4_CONST_LONG = 2251799813685250L;
    public static final long RESERVED_5_CONST_LONG = 4503599627370500L;
    public static final long RESERVED_6_CONST_LONG = 9007199254740990L;
    public static final long RESERVED_7_CONST_LONG = 18014398509482000L;
    public static final long RESERVED_8_CONST_LONG = 36028797018964000L;
    public static final long RESERVED_9_CONST_LONG = 72057594037927900L;
    public static final long RESERVED_10_CONST_LONG = 144115188075856000L;
    public static final long RESERVED_11_CONST_LONG = 288230376151712000L;
    public static final long RESERVED_12_CONST_LONG = 576460752303424000L;
    public static final long RESERVED_13_CONST_LONG = 1152921504606850000L;
    public static final long RESERVED_14_CONST_LONG = 2305843009213690000L;
    public static final long RESERVED_15_CONST_LONG = 4611686018427390000L;

    // Map permissions to available actions
    // add_remote
    public static final long ADD_REMOTE_CONST_LONG = ADD_REMOTE_REPOSITORY_CONST_LONG;
    // audit_log
    public static final long AUDIT_LOG_CONST_LONG = READ_AUDIT_LOG_CONST_LONG;
    // cancel_failed_pull
    public static final long CANCEL_FAILED_PULL_CONST_LONG = RESOLVE_CONFLICTS_CONST_LONG;
    // cancel_publish
    public static final long CANCEL_PUBLISH_PERMISSION_CONST_LONG = CANCEL_PUBLISH_CONST_LONG;
    // Change Content Type
    public static final long CHANGE_CONTENT_TYPE_CONST_LONG = CONTENT_UPDATE_CONST_LONG;
    // clone_content_cmis
    public static final long CLONE_CONTENT_CMIS_CONST_LONG = CONTENT_CREATE_CONST_LONG + CONTENT_UPDATE_CONST_LONG;
    // commit_resolution
    public static final long COMMIT_RESOLUTION_CONST_LONG = RESOLVE_CONFLICTS_CONST_LONG;
    // Create Content
    public static final long CREATE_CONTENT_CONST_LONG = CONTENT_CREATE_CONST_LONG;
    // Create Folder
    public static final long CREATE_FOLDER_CONST_LONG = CONTENT_CREATE_CONST_LONG;
    // create_cluster
    public static final long CREATE_CLUSTER_CONST_LONG = SYSTEM_CREATE_CONST_LONG;
    // create_groups
    public static final long CREATE_GROUPS_CONST_LONG = SYSTEM_CREATE_CONST_LONG;
    // create_users
    public static final long CREATE_USERS_CONST_LONG = SYSTEM_CREATE_CONST_LONG;
    // create-site
    public static final long CREATE_SITE_CONST_LONG = SYSTEM_CREATE_CONST_LONG;
    // Delete
    public static final long DELETE_CONST_LONG = CONTENT_DELETE_CONST_LONG;
    // delete_cluster
    public static final long DELETE_CLUSTER_CONST_LONG = SYSTEM_DELETE_CONST_LONG;
    // delete_content
    public static final long DELETE_CONTENT_CONST_LONG = CONTENT_DELETE_CONST_LONG;
    // delete_groups
    public static final long DELETE_GROUPS_CONST_LONG = SYSTEM_DELETE_CONST_LONG;
    // delete_users
    public static final long DELETE_USERS_CONST_LONG = SYSTEM_DELETE_CONST_LONG;
    // edit_site
    public static final long EDIT_SITE_CONST_LONG = SYSTEM_UPDATE_CONST_LONG;
    // encryption_tool
    public static final long ENCRYPTION_TOOL_CONST_LONG = CONTENT_UPDATE_CONST_LONG + SYSTEM_UPDATE_CONST_LONG;
    // get_children
    public static final long GET_CHILDREN_CONST_LONG = 0L;
    // get_publishing_queue
    public static final long GET_PUBLISHING_QUEUE_CONST_LONG = READ_PUBLISHING_QUEUE_CONST_LONG;
    // list_cmis
    public static final long LIST_CMIS_CONST_LONG = 0L;
    // list_remotes
    public static final long LIST_REMOTES_CONST_LONG = ADD_REMOTE_REPOSITORY_CONST_LONG;
    // Publish
    public static final long PUBLISH_CONST_LONG = APPROVE_PUBLISH_CONST_LONG + REJECT_PUBLISH_CONST_LONG;
    // pull_from_remote
    public static final long PULL_FROM_REMOTE_CONST_LONG = PULL_FROM_REMOTE_REPOSITORY_CONST_LONG;
    // push_to_remote
    public static final long PUSH_TO_REMOTE_CONST_LONG = PUSH_TO_REMOTE_REPOSITORY_CONST_LONG;
    // Read
    public static final long READ_CONST_LONG = 0L;
    // read_cluster
    public static final long READ_CLUSTER_CONST_LONG = SYSTEM_READ_CONST_LONG;
    // read_groups
    public static final long READ_GROUPS_CONST_LONG = SYSTEM_READ_CONST_LONG;
    // read_logs
    public static final long READ_LOGS_CONST_LONG = READ_SITE_LOG_CONST_LONG;
    // read_users
    public static final long READ_USERS_CONST_LONG = SYSTEM_READ_CONST_LONG;
    // rebuild_database
    public static final long REBUILD_DATABASE_CONST_LONG = SYSTEM_UPDATE_CONST_LONG;
    // remove_remote
    public static final long REMOVE_REMOTE_CONST_LONG = REMOVE_REMOTE_REPOSITORY_CONST_LONG;
    // resolve_conflict
    public static final long RESOLVE_CONFLICT_CONST_LONG = RESOLVE_CONFLICTS_CONST_LONG;
    // S3 Read
    public static final long S3_READ_CONST_LONG = 0L;
    // S3 Write
    public static final long S3_WRITE_CONST_LONG = CONTENT_CREATE_CONST_LONG + CONTENT_UPDATE_CONST_LONG;
    // search_cmis
    public static final long SEARCH_CMIS_CONST_LONG = 0L;
    // site_diff_conflicted_file
    public static final long SITE_DIFF_CONFLICTED_FILE_CONST_LONG = RESOLVE_CONFLICTS_CONST_LONG;
    // site_status
    public static final long SITE_STATUS_CONST_LONG = SYSTEM_READ_CONST_LONG;
    // update_cluster
    public static final long UPDATE_CLUSTER_CONST_LONG = SYSTEM_UPDATE_CONST_LONG;
    // update_groups
    public static final long UPDATE_GROUPS_CONST_LONG = SYSTEM_UPDATE_CONST_LONG;
    // update_users
    public static final long UPDATE_USERS_CONST_LONG = SYSTEM_UPDATE_CONST_LONG;
    // upload_content_cmis
    public static final long UPLOAD_CONTENT_CMIS_CONST_LONG = CONTENT_CREATE_CONST_LONG + CONTENT_UPDATE_CONST_LONG;
    // webdav_read
    public static final long WEBDAV_READ_CONST_LONG = 0L;
    // webdav_write
    public static final long WEBDAV_WRITE_CONST_LONG = CONTENT_CREATE_CONST_LONG + CONTENT_UPDATE_CONST_LONG;
    // Write
    public static final long WRITE_CONST_LONG =
            CONTENT_CREATE_CONST_LONG + CONTENT_UPDATE_CONST_LONG + CONTENT_CUT_CONST_LONG + CONTENT_COPY_CONST_LONG +
                    CONTENT_PASTE_CONST_LONG + CONTENT_MOVE_RENAME_CONST_LONG + CONTENT_DUPLICATE_CONST_LONG;
    // write_configuration
    public static final long WRITE_CONFIGURATION_CONST_LONG = CONTENT_CREATE_CONST_LONG + CONTENT_UPDATE_CONST_LONG;
    // write_global_configuration
    public static final long WRITE_GLOBAL_CONFIGURATION_CONST_LONG = SYSTEM_CREATE_CONST_LONG + SYSTEM_UPDATE_CONST_LONG;
}
