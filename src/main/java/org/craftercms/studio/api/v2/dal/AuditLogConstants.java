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

import java.util.List;

public abstract class AuditLogConstants {

    /** Operation **/
    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_UPDATE = "UPDATE";
    public static final String OPERATION_DELETE = "DELETE";
    public static final String OPERATION_MOVE = "MOVE";
    public static final String OPERATION_ADD_MEMBERS = "ADD_MEMBERS";
    public static final String OPERATION_REMOVE_MEMBERS = "REMOVE_MEMBERS";
    public static final String OPERATION_LOGIN = "LOGIN";
    public static final String OPERATION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String OPERATION_LOGOUT = "LOGOUT";
    public static final String OPERATION_PRE_AUTH = "PRE_AUTH";
    public static final String OPERATION_ADD_REMOTE = "ADD_REMOTE";
    public static final String OPERATION_REMOVE_REMOTE = "REMOVE_REMOTE";
    public static final String OPERATION_PUSH_TO_REMOTE = "PUSH_TO_REMOTE";
    public static final String OPERATION_PULL_FROM_REMOTE = "PULL_FROM_REMOTE";
    public static final String OPERATION_REQUEST_PUBLISH = "REQUEST_PUBLISH";
    public static final String OPERATION_APPROVE = "APPROVE";
    public static final String OPERATION_APPROVE_SCHEDULED = "APPROVE_SCHEDULED";
    public static final String OPERATION_REJECT = "REJECT";
    public static final String OPERATION_PUBLISHED = "PUBLISHED";
    public static final String OPERATION_REVERT = "REVERT";
    public static final String OPERATION_ENABLE = "ENABLE";
    public static final String OPERATION_DISABLE = "DISABLE";
    public static final String OPERATION_START_PUBLISHER = "START_PUBLISHER";
    public static final String OPERATION_STOP_PUBLISHER = "STOP_PUBLISHER";
    public static final String OPERATION_REMOVE_CLUSTER_NODE = "REMOVE_CLUSTER_NODE";
    public static final String OPERATION_CANCEL_PUBLISHING_PACKAGE = "CANCEL_PUBLISHING_PACKAGE";
    public static final String OPERATION_PUBLISH = "PUBLISH";
    public static final String OPERATION_INITIAL_PUBLISH = "INITIAL_PUBLISH";

    public static final String OPERATION_PUBLISH_ALL = "PUBLISH_ALL";
    public static final String OPERATION_UNKNOWN = "UNKNOWN";

    /** Origin **/
    public static final String ORIGIN_API = "API";
    public static final String ORIGIN_GIT = "GIT";

    /** Target Type **/
    public static final String TARGET_TYPE_USER = "User";
    public static final String TARGET_TYPE_SITE = "Site";
    public static final String TARGET_TYPE_GROUP = "Group";
    public static final String TARGET_TYPE_FOLDER = "Folder";
    public static final String TARGET_TYPE_CONTENT_ITEM = "Content Item";
    public static final String TARGET_TYPE_REMOTE_REPOSITORY = "Remote Repository";
    public static final String TARGET_TYPE_CLUSTER_NODE = "Cluster Node";
    public static final String TARGET_TYPE_ACCESS_TOKEN = "Access Token";
    public static final String TARGET_TYPE_REFRESH_TOKEN = "Refresh Token";
    public static final String TARGET_TYPE_BLUEPRINT = "Blueprint";
    public static final String TARGET_TYPE_PUBLISHING_PACKAGE = "Publishing Package";
    public static final String TARGET_TYPE_SUBMISSION_COMMENT = "Submission Comment";
    public static final String TARGET_TYPE_REJECTION_COMMENT = "Rejection Comment";
    public static final String TARGET_TYPE_UNKNOWN = "unknown";

    public static final List<String> ACTIVITY_STREAM_OPERATIONS = List.of(
            OPERATION_CREATE, OPERATION_UPDATE, OPERATION_DELETE, OPERATION_MOVE, OPERATION_REQUEST_PUBLISH,
            OPERATION_APPROVE, OPERATION_APPROVE_SCHEDULED, OPERATION_REJECT, OPERATION_REVERT, OPERATION_PUBLISH,
            OPERATION_INITIAL_PUBLISH, OPERATION_UNKNOWN);

}
