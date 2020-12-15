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

package org.craftercms.studio.api.v2.dal;

public interface QueryParameterNames {

    // Id
    String ID = "id";
    // Offset
    String OFFSET = "offset";
    // Limit
    String LIMIT = "limit";
    // Sort
    String SORT = "sort";
    // Order
    String ORDER = "order";
    // Path
    String PATH = "path";
    // Paths
    String PATHS = "paths";
    // Modifier
    String MODIFIER = "modifier";
    // Old path
    String OLD_PATH = "oldPath";
    // New path
    String NEW_PATH = "newPath";
    // commit id
    String COMMIT_ID = "commitId";
    // state
    String STATE = "state";

    /* Organizations */

    // Organization ID
    String ORG_ID = "orgId";

    /* Sites */
    String SITE_ID = "siteId";

    String SITE = "site";

    String LOCK_OWNER_ID = "lockOwnerId";

    String TTL = "ttl";

    String NAME = "name";

    String DESC = "description";


    /* Groups */

    // Group ID
    String GROUP_ID = "groupId";
    // Group IDs
    String GROUP_IDS = "groupIds";
    // Group name
    String GROUP_NAME = "groupName";
    // Group names
    String GROUP_NAMES = "groupNames";
    // Group description
    String GROUP_DESCRIPTION = "groupDescription";

    /* Users */

    // Usernames
    String USERNAMES = "usernames";
    // User IDs
    String USER_IDS = "userIds";
    // User ID
    String USER_ID = "userId";
    // Username
    String USERNAME = "username";
    // Password
    String PASSWORD = "password";
    // First name
    String FIRST_NAME = "firstName";
    // Last name
    String LAST_NAME = "lastName";
    // Externally managed
    String EXTERNALLY_MANAGED = "externallyManaged";
    // Timezone
    String TIMEZONE = "timezone";
    // Locale
    String LOCALE = "locale";
    // Email
    String EMAIL = "email";
    // Active
    String ENABLED = "enabled";
    // First name and Last name
    String GIT_NAME = "gitName";

    String KEYS = "keys";
    String PROPERTIES = "properties";

    /* Cluster */
    // Local address
    String CLUSTER_LOCAL_ADDRESS = "localAddress";
    // State
    String CLUSTER_STATE = "state";
    // Member ids list
    String CLUSTER_MEMBER_IDS = "memberIds";
    // Inactivity limit
    String CLUSTER_INACTIVITY_LIMIT = "inactivityLimit";
    // Inactive state
    String CLUSTER_INACTIVE_STATE = "inactiveState";
    // Stale heartbeat limit
    String CLUSTER_HEARTBEAT_STALE_LIMIT = "heartbeatStaleLimit";
    // cluster id
    String CLUSTER_ID = "clusterId";
    // remote repository id
    String REMOTE_REPOSITORY_ID = "remoteRepositoryId";
    // node last commit id
    String NODE_LAST_COMMIT_ID = "nodeLastCommitId";
    // cluster id
    String NODE_LAST_VERIFIED_GITLOG_COMMIT_ID = "nodeLastVerifiedGitlogCommitId";

    /* Publish request */
    // Environment
    String ENVIRONMENT = "environment";
    // Processing state
    String PROCESSING_STATE = "processingState";
    // Ready state
    String READY_STATE = "readyState";

    /* Audit */
    String ACTIONS = "actions";

    String OPERATIONS = "operations";

    String DATE_FROM = "dateFrom";

    String DATE_TO = "dateTo";

    String TARGET = "target";

    String ORIGIN = "origin";

    String CLUSTER_NODE_ID = "clusterNodeId";

    String INCLUDE_PARAMETERS = "includeParameters";

    /* Item */
    String LOCALE_CODE = "localeCode";

    String PARENT_PATH = "parentPath";

    String PARENT_ID = "parentId";

    String LEVEL_DESCRIPTOR_PATH = "ldPath";

    String LEVEL_DESCRIPTOR_NAME = "ldName";

    String ENTRIES = "entries";

    String ROOT_PATH = "rootPath";

    String STATES_BIT_MAP = "statesBitMap";

    String ON_STATES_BIT_MAP = "onStatesBitMap";

    String OFF_STATES_BIT_MAP = "offStatesBitMap";

    String ITEM_IDS = "itemIds";

    String CONTENT_TYPE = "contentType";

}
