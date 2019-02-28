/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

    /* Organizations */

    // Organization ID
    public static final String ORG_ID = "orgId";

    /* Sites */
    public static final String SITE_ID = "siteId";

    public static final String SITE = "site";

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

    /* Publish request */
    // Environment
    public static final String ENVIRONMENT = "environment";
    // Processing state
    public static final String PROCESSING_STATE = "processingState";
    // Ready state
    public static final String READY_STATE = "readyState";

    /* Audit */
    public static final String ACTIONS = "actions";

    private QueryParameterNames() { }
}
