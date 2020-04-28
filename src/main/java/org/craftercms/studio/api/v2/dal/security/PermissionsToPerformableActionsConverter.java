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
        map.put(CANCEL_FAILED_PULL, 0L);
        map.put(CANCEL_PUBLISH, 0L);
        map.put(CHANGE_CONTENT_TYPE, 0L);
        map.put(CLONE_CONTENT_CMIS, 0L);
        map.put(COMMIT_RESOLUTION, 0L);
        map.put(CREATE_CONTENT, 0L);
        map.put(CREATE_FOLDER, 0L);
        map.put(CREATE_CLUSTER, 0L);
        map.put(CREATE_GROUPS, 0L);
        map.put(CREATE_USERS, 0L);
        map.put(CREATE_SITE, 0L);
        map.put(DELETE, 0L);
        map.put(DELETE_CLUSTER, 0L);
        map.put(DELETE_CONTENT, 0L);
        map.put(DELETE_GROUPS, 0L);
        map.put(DELETE_USERS, 0L);
        map.put(ENCRYPTION_TOOL, 0L);
        map.put(GET_PUBLISHING_QUEUE, 0L);
        map.put(LIST_CMIS, 0L);
        map.put(LIST_REMOTES, 0L);
        map.put(PUBLISH, 0L);
        map.put(PULL_FROM_REMOTE, 0L);
        map.put(PUSH_TO_REMOTE, 0L);
        map.put(READ, 0L);
        map.put(READ_CLUSTER, 0L);
        map.put(READ_GROUPS, 0L);
        map.put(READ_LOGS, 0L);
        map.put(READ_USERS, 0L);
        map.put(REBUILD_DATABASE, 0L);
        map.put(REMOVE_REMOTE, 0L);
        map.put(RESOLVE_CONFLICT, 0L);
        map.put(S3_READ, 0L);
        map.put(S3_WRITE, 0L);
        map.put(SEARCH_CMIS, 0L);
        map.put(SITE_DIFF_CONFLICTED_FILE, 0L);
        map.put(SITE_STATUS, 0L);
        map.put(UPDATE_CLUSTER, 0L);
        map.put(UPDATE_GROUPS, 0L);
        map.put(UPDATE_USERS, 0L);
        map.put(UPLOAD_CONTENT_CMIS, 0L);
        map.put(WEBDAV_READ, 0L);
        map.put(WEBDAV_WRITE, 0L);
        map.put(WRITE, 0L);
        map.put(WRITE_CONFIGURATION, 0L);
        map.put(WRITE_GLOBAL_CONFIGURATION, 0L);
        conversionMap = Collections.unmodifiableMap(map);
    }

    public static long getPerformableActionsValue(String permission) {
        return 0;
    }
}
