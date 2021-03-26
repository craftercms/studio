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

public abstract class QueryParameterNames {

    // Id
    public static final String ID = "id";
    // Offset
    public static final String OFFSET = "offset";
    // Limit
    public static final String LIMIT = "limit";
    // Sort
    public static final String SORT = "sort";

    public static final String ORDER = "order";
    // commit id
    public static final String COMMIT_ID = "commitId";

    public static final String PATH = "path";

    public static final String PATHS = "paths";

    public static final String PUBLISHED_DATE = "publishedDate";

    /* Organizations */

    // Organization ID
    public static final String ORG_ID = "orgId";

    /* Sites */
    public static final String SITE_ID = "siteId";

    public static final String SITE = "site";

    public static final String LOCK_OWNER_ID = "lockOwnerId";

    public static final String TTL = "ttl";

    public static final String STATE = "state";

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

    /* Audit */
    public static final String ACTIONS = "actions";

    public static final String OPERATIONS = "operations";

    public static final String DATE_FROM = "dateFrom";

    public static final String DATE_TO = "dateTo";

    public static final String TARGET = "target";

    public static final String ORIGIN = "origin";

    public static final String CLUSTER_NODE_ID = "clusterNodeId";

    public static final String INCLUDE_PARAMETERS = "includeParameters";

    /** Gitlog */
    public static final String AUDITED = "audited";
    // list of commit ids
    public static final String COMMIT_IDS = "commitIds";

    public static final String MARKER = "marker";

    public static final String PROCESSED = "processed";

    private QueryParameterNames() { }
}
