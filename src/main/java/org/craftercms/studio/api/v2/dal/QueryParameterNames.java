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
    // LIKE path
    public static final String LIKE_PATH = "likePath";
    // Folder Path
    public static final String FOLDER_PATH = "folderPath";
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

    public static final String LOCK_OWNER_ID = "lockOwnerId";

    public static final String TTL = "ttl";

    public static final String NAME = "name";

    public static final String DESC = "description";


    public static final String PUBLISHING_STATUS = "publishingStatus";

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

    /* Publish request */
    // Environment
    public static final String ENVIRONMENT = "environment";
    // Environments
    public static final String ENVIRONMENTS = "environments";
    // Processing state
    public static final String PROCESSING_STATE = "processingState";
    // Ready state
    public static final String READY_STATE = "readyState";

    public static final String STATES = "states";

    public static final String PACKAGE_ID = "packageId";

    public static final String PACKAGE_IDS = "packageIds";

    public static final String CANCELLED_STATE = "cancelledState";

    public static final String NOW = "now";

    public static final String COMPLETED_STATE = "completedState";

    public static final String FROM_DATE = "fromDate";

    public static final String TO_DATE = "toDate";

    public static final String CONTENT_TYPE_CLASS = "contentTypeClass";

    public static final String STAGING_ENVIRONMENT = "stagingEnvironment";

    public static final String LIVE_ENVIRONMENT = "liveEnvironment";

    public static final String APPROVER = "approver";

    public static final String PUBLISHING_TARGET = "publishingTarget";

    public static final String SCHEDULED_STATE  = "scheduledState";

    public static final String DAYS = "days";

    public static final String ACTIVITY_ACTION = "activityAction";

    public static final String PUBLISH_STATE = "publishState";

    public static final String PUBLISH_ACTION = "publishAction";

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

    public static final String PARENT_ID = "parentId";

    public static final String ENTRIES = "entries";

    public static final String STATES_BIT_MAP = "statesBitMap";

    public static final String ON_STATES_BIT_MAP = "onStatesBitMap";

    public static final String OFF_STATES_BIT_MAP = "offStatesBitMap";

    public static final String ITEM_IDS = "itemIds";

    public static final String CONTENT_TYPE = "contentType";

    public static final String OLD_PREVIEW_URL = "oldPreviewUrl";

    public static final String NEW_PREVIEW_URL = "newPreviewUrl";

    public static final String EXCLUDES = "excludes";

    public static final String SYSTEM_TYPES = "systemTypes";

    public static final String KEYWORD = "keyword";

    public static final String POSSIBLE_PARENTS = "possibleParents";

    public static final String NEW_MASK = "newMask";

    public static final String MODIFIED_MASK = "modifiedMask";

    public static final String NON_CONTENT_ITEM_TYPES = "nonContentItemTypes";

    public static final String IN_PROGRESS_MASK = "inProgressMask";

    public static final String SUBMITTED_MASK = "submittedMask";

    public static final String PREVIOUS_PATH = "previousPath";

    public static final String PARENTS = "parents";

    public static final String LAST_PUBLISHED_ON = "lastPublishedOn";

    public static final String SCRIPT_PATH = "scriptPath";

    public static final String LOCKED_BIT_ON = "lockedBitOn";

    public static final String LOCKED_BIT_OFF = "lockedBitOff";

    public static final String SYSTEM_TYPE_FOLDER = "systemTypeFolder";

    public static final String PREFER_CONTENT = "preferContent";

    /** Gitlog */
    public static final String AUDITED = "audited";
    // list of commit ids
    public static final String COMMIT_IDS = "commitIds";

    public static final String MARKER = "marker";

    public static final String PROCESSED = "processed";

    public static final String UNPROCESSED = "unprocessed";

    /** Workflow */
    public static final String PUBLISHING_PACKAGE_ID = "publishingPackageId";

    public static final String WORKFLOW = "workflow";

    public static final String WORKFLOW_ENTRIES = "workflowEntries";

    public static final String STATE_OPENED = "stateOpened";

    public static final String ITEM_ID = "itemId";

    public static final String TYPE = "type";

    public static final String SOURCE_PATH = "sourcePath";

    /** Activity Stream */
    public static final String ACTION = "action";
    public static final String ACTION_TIMESTAMP = "actionTimestamp";
    public static final String ITEM = "item";

    private QueryParameterNames() {
    }
}
