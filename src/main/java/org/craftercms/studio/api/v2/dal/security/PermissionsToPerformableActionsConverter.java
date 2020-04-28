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

package org.craftercms.studio.api.v2.dal.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class PermissionsToPerformableActionsConverter {

    private static Map<String, Long> conversionMap;

    public static String ADD_REMOTE = "add_remote";
    public static String AUDIT_LOG = "audit_log";
    public static String CANCEL_FAILED_PULL = "cancel_failed_pull";
    public static String CANCEL_PUBLISH = "cancel_publish";
    public static String CHANGE_CONTENT_TYPE = "Change Content Type";
    public static String CLONE_CONTENT_CMIS = "clone_content_cmis";
    public static String COMMIT_RESOLUTION = "commit_resolution";
    public static String CREATE_CONTENT = "Create Content";
    public static String CREATE_FOLDER = "Create Folder";
    public static String CREATE_CLUSTER = "create_cluster";
    public static String CREATE_GROUPS = "create_groups";
    public static String CREATE_USERS = "create_users";
    public static String CREATE_SITE = "create-site";
    public static String DELETE = "Delete";
    public static String DELETE_CLUSTER = "delete_cluster";
    public static String DELETE_CONTENT = "delete_content";
    public static String DELETE_GROUPS = "delete_groups";
    public static String DELETE_USERS = "delete_users";
    public static String ENCRYPTION_TOOL = "encryption_tool";
    public static String GET_PUBLISHING_QUEUE = "get_publishing_queue";
    public static String LIST_CMIS = "list_cmis";
    public static String LIST_REMOTES = "list_remotes";
    public static String PUBLISH = "Publish";
    public static String PULL_FROM_REMOTE = "pull_from_remote";
    public static String PUSH_TO_REMOTE = "push_to_remote";
    public static String READ = "Read";
    public static String READ_CLUSTER = "read_cluster";
    public static String READ_GROUPS = "read_groups";
    public static String READ_LOGS = "read_logs";
    public static String READ_USERS = "read_users";
    public static String REBUILD_DATABASE = "rebuild_database";
    public static String REMOVE_REMOTE = "remove_remote";
    public static String RESOLVE_CONFLICT = "resolve_conflict";
    public static String S3_READ = "S3 Read";
    public static String S3_WRITE = "S3 Write";
    public static String SEARCH_CMIS = "search_cmis";
    public static String SITE_DIFF_CONFLICTED_FILE = "site_diff_conflicted_file";
    public static String SITE_STATUS = "site_status";
    public static String UPDATE_CLUSTER = "update_cluster";
    public static String UPDATE_GROUPS = "update_groups";
    public static String UPDATE_USERS = "update_users";
    public static String UPLOAD_CONTENT_CMIS = "upload_content_cmis";
    public static String WEBDAV_READ = "webdav_read";
    public static String WEBDAV_WRITE = "webdav_write";
    public static String WRITE = "Write";
    public static String WRITE_CONFIGURATION = "write_configuration";
    public static String WRITE_GLOBAL_CONFIGURATION = "write_global_configuration";

    static {
        Map<String, Long> map = new HashMap<String, Long>();
        map.put(ADD_REMOTE, 0L);
        map.put(AUDIT_LOG, 0L);
        public String CANCEL_FAILED_PULL = "cancel_failed_pull";
        public String CANCEL_PUBLISH = "cancel_publish";
        public String CHANGE_CONTENT_TYPE = "Change Content Type";
        public String CLONE_CONTENT_CMIS = "clone_content_cmis";
        public String COMMIT_RESOLUTION = "commit_resolution";
        public String CREATE_CONTENT = "Create Content";
        public String CREATE_FOLDER = "Create Folder";
        public String CREATE_CLUSTER = "create_cluster";
        public String CREATE_GROUPS = "create_groups";
        public String CREATE_USERS = "create_users";
        public String CREATE_SITE = "create-site";
        public String DELETE = "Delete";
        public String DELETE_CLUSTER = "delete_cluster";
        public String DELETE_CONTENT = "delete_content";
        public String DELETE_GROUPS = "delete_groups";
        public String DELETE_USERS = "delete_users";
        public String ENCRYPTION_TOOL = "encryption_tool";
        public String GET_PUBLISHING_QUEUE = "get_publishing_queue";
        public String LIST_CMIS = "list_cmis";
        public String LIST_REMOTES = "list_remotes";
        public String PUBLISH = "Publish";
        public String PULL_FROM_REMOTE = "pull_from_remote";
        public String PUSH_TO_REMOTE = "push_to_remote";
        public String READ = "Read";
        public String READ_CLUSTER = "read_cluster";
        public String READ_GROUPS = "read_groups";
        public String READ_LOGS = "read_logs";
        public String READ_USERS = "read_users";
        public String REBUILD_DATABASE = "rebuild_database";
        public String REMOVE_REMOTE = "remove_remote";
        public String RESOLVE_CONFLICT = "resolve_conflict";
        public String S3_READ = "S3 Read";
        public String S3_WRITE = "S3 Write";
        public String SEARCH_CMIS = "search_cmis";
        public String SITE_DIFF_CONFLICTED_FILE = "site_diff_conflicted_file";
        public String SITE_STATUS = "site_status";
        public String UPDATE_CLUSTER = "update_cluster";
        public String UPDATE_GROUPS = "update_groups";
        public String UPDATE_USERS = "update_users";
        public String UPLOAD_CONTENT_CMIS = "upload_content_cmis";
        public String WEBDAV_READ = "webdav_read";
        public String WEBDAV_WRITE = "webdav_write";
        public String WRITE = "Write";
        public String WRITE_CONFIGURATION = "write_configuration";
        public String WRITE_GLOBAL_CONFIGURATION = "write_global_configuration";
        conversionMap = Collections.unmodifiableMap(map);
    }

    public static long getPerformableActionsValue(String permission) {
        return 0;
    }
}
