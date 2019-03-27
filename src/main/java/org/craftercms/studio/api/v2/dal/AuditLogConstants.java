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
    public static final String OPERATION_ADD_REMOTE = "ADD_REMOTE";
    public static final String OPERATION_REMOVE_REMOTE = "REMOVE_REMOTE";
    public static final String OPERATION_PUSH_TO_REMOTE = "PUSH_TO_REMOTE";
    public static final String OPERATION_PULL_FROM_REMOTE = "PULL_FROM_REMOTE";
    public static final String OPERATION_REQUEST_PUBLISH = "REQUEST_PUBLISH";
    public static final String OPERATION_APPROVE = "APPROVE";
    public static final String OPERATION_APPROVE_SCHEDULED = "APPROVE_SCHEDULED";
    public static final String OPERATION_REJECT = "REJECT";
    public static final String OPERATION_PUBLISHED = "PUBLISHED";
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
    public static final String TARGET_TYPE_UNKNOWN = "unknown";
}
