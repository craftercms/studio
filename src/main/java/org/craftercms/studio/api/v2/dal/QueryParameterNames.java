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

package org.craftercms.studio.api.v2.dal;

public final class  QueryParameterNames {

    // Id
    public static final String ID = "id";
    // Offset
    public static final String OFFSET = "offset";
    // Limit
    public static final String LIMIT = "limit";
    // Sort
    public static final String SORT = "sort";
    public static final String SORT_STRATEGY = "sortStrategy";
    // Order
    public static final String ORDER = "order";
    // Path
    public static final String PATH = "path";
    // Paths
    public static final String PATHS = "paths";
    // Modifier
    public static final String MODIFIER = "modifier";
    // Old path
    public static final String OLD_PATH = "oldPath";
    // New path
    public static final String NEW_PATH = "newPath";
    // commit id
    public static final String COMMIT_ID = "commitId";
    // state
    public static final String STATE = "state";

    /* Organizations */

    // Organization ID
    public static final String ORG_ID = "orgId";

    /* Sites */
    public static final String SITE_ID = "siteId";

    public static final String SITE = "site";

    public static final String LOCK_OWNER_ID = "lockOwnerId";

    public static final String TTL = "ttl";

    public static final String NAME = "name";

    public static final String DESC = "description";


    /* Groups */

    // Group ID
    public static final String GROUP_ID = "groupId";
    // Group IDs
    public static final String GROUP_IDS = "groupIds";
    // Group name
    public static final String GROUP_NAME = "groupName";
    // Group names
    public static final String GROUP_NAMES = "groupNames";
    // Group description
    public static final String GROUP_DESCRIPTION = "groupDescription";

    /* Users */

    // Usernames
    public static final String USERNAMES = "usernames";
    // User IDs
    public static final String USER_IDS = "userIds";
    // User ID
    public static final String USER_ID = "userId";
    // Username
    public static final String USERNAME = "username";
    // Password
    public static final String PASSWORD = "password";
    // First name
    public static final String FIRST_NAME = "firstName";
    // Last name
    public static final String LAST_NAME = "lastName";
    // Externally managed
    public static final String EXTERNALLY_MANAGED = "externallyManaged";
    // Timezone
    public static final String TIMEZONE = "timezone";
    // Locale
    public static final String LOCALE = "locale";
    // Email
    public static final String EMAIL = "email";
    // Active
    public static final String ENABLED = "enabled";
    // First name and Last name
    public static final String GIT_NAME = "gitName";

    public static final String KEYS = "keys";
    public static final String PROPERTIES = "properties";

    /* Cluster */
    // Local address
    public static final String CLUSTER_LOCAL_ADDRESS = "localAddress";
    // State
    public static final String CLUSTER_STATE = "state";
    // Member ids list
    public static final String CLUSTER_MEMBER_IDS = "memberIds";
    // Inactivity limit
    public static final String CLUSTER_INACTIVITY_LIMIT = "inactivityLimit";
    // Inactive state
    public static final String CLUSTER_INACTIVE_STATE = "inactiveState";
    // Stale heartbeat limit
    public static final String CLUSTER_HEARTBEAT_STALE_LIMIT = "heartbeatStaleLimit";
    // cluster id
    public static final String CLUSTER_ID = "clusterId";
    // remote repository id
    public static final String REMOTE_REPOSITORY_ID = "remoteRepositoryId";
    // node last commit id
    public static final String NODE_LAST_COMMIT_ID = "nodeLastCommitId";
    // cluster id
    public static final String NODE_LAST_VERIFIED_GITLOG_COMMIT_ID = "nodeLastVerifiedGitlogCommitId";
    public static final String NODE_LAST_SYNCED_GITLOG_COMMIT_ID = "nodeLastSyncedGitlogCommitId";

    /* Publish request */
    // Environment
    public static final String ENVIRONMENT = "environment";
    // Processing state
    public static final String PROCESSING_STATE = "processingState";
    // Ready state
    public static final String READY_STATE = "readyState";

    public static final String STATES = "states";

    public static final String PACKAGE_ID = "packageId";

    public static final String PACKAGE_IDS = "packageIds";

    public static final String CANCELLED_STATE = "cancelledState";

    public static final String NOW = "now";

    /* Audit */
    public static final String ACTIONS = "actions";

    public static final String OPERATIONS = "operations";

    public static final String DATE_FROM = "dateFrom";

    public static final String DATE_TO = "dateTo";

    public static final String TARGET = "target";

    public static final String ORIGIN = "origin";

    public static final String CLUSTER_NODE_ID = "clusterNodeId";

    public static final String INCLUDE_PARAMETERS = "includeParameters";

    /* Item */
    public static final String LOCALE_CODE = "localeCode";

    public static final String PARENT_PATH = "parentPath";

    public static final String PARENT_ID = "parentId";

    public static final String LEVEL_DESCRIPTOR_PATH = "ldPath";

    public static final String LEVEL_DESCRIPTOR_NAME = "ldName";

    public static final String ENTRIES = "entries";

    public static final String ROOT_PATH = "rootPath";

    public static final String STATES_BIT_MAP = "statesBitMap";

    public static final String ON_STATES_BIT_MAP = "onStatesBitMap";

    public static final String OFF_STATES_BIT_MAP = "offStatesBitMap";

    public static final String ITEM_IDS = "itemIds";

    public static final String CONTENT_TYPE = "contentType";

    public static final String MOVE_MASK = "moveMask";

    public static final String IGNORE_NAMES = "ignoreNames";

    public static final String OLD_PREVIEW_URL = "oldPreviewUrl";

    public static final String NEW_PREVIEW_URL = "newPreviewUrl";

    public static final String EXCLUDES = "excludes";

    public static final String KEYWORD = "keyword";

    /** Gitlog */
    public static final String AUDITED = "audited";
    // list of commit ids
    public static final String COMMIT_IDS = "commitIds";

    public static final String MARKER = "marker";

    private QueryParameterNames() {
    }
}
