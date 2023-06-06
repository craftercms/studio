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

package org.craftercms.studio.controller.rest.v2;

public final class RequestConstants {

    public static final String REQUEST_PARAM_OFFSET = "offset";
    public static final String REQUEST_PARAM_LIMIT = "limit";
    public static final String REQUEST_PARAM_SORT = "sort";
    public static final String REQUEST_PARAM_ID = "id";
    public static final String REQUEST_PARAM_SITE_ID = "site_id";
    public static final String REQUEST_PARAM_SITEID = "siteId";
    public static final String REQUEST_PARAM_SITE_NAME = "siteName";
    public static final String REQUEST_PARAM_SITE = "site";
    public static final String REQUEST_PARAM_USER = "user";
    public static final String REQUEST_PARAM_USER_ID = "userId";
    public static final String REQUEST_PARAM_USERNAME = "username";
    public static final String REQUEST_PARAM_OPERATIONS = "operations";
    public static final String REQUEST_PARAM_INCLUDE_PARAMETERS = "includeParameters";
    public static final String REQUEST_PARAM_DATE_FROM = "dateFrom";
    public static final String REQUEST_PARAM_DATE_TO = "dateTo";
    public static final String REQUEST_PARAM_TARGET = "target";
    public static final String REQUEST_PARAM_ORIGIN = "origin";
    public static final String REQUEST_PARAM_CLUSTER_NODE_ID = "clusterNodeId";
    public static final String REQUEST_PARAM_ORDER = "order";
    public static final String REQUEST_PARAM_PATH = "path";
    public static final String REQUEST_PARAM_PROFILE_ID = "profileId";
    public static final String REQUEST_PARAM_TYPE = "type";
    public static final String REQUEST_PARAM_ENVIRONMENT = "environment";
    public static final String REQUEST_PARAM_STATES = "states";
    public static final String REQUEST_PARAM_PACKAGE_ID = "packageId";
    public static final String REQUEST_PARAM_TOKEN = "token";
    public static final String REQUEST_PARAM_PREFER_CONTENT = "preferContent";
    public static final String REQUEST_PARAM_DAYS = "days";
    public static final String REQUEST_PARAM_NUM = "num";
    public static final String REQUEST_PARAM_FILTER_TYPE = "filterType";
    public static final String REQUEST_PARAM_KEYWORD = "keyword";
    public static final String REQUEST_PARAM_COMMIT_ID = "commitId";
    public static final String REQUEST_PARAM_USERNAMES = "usernames";
    public static final String REQUEST_PARAM_PUBLISHING_TARGET = "publishingTarget";
    public static final String REQUEST_PARAM_APPROVER = "approver";
    public static final String REQUEST_PARAM_ITEM_TYPE = "itemType";

    public static final String GROUP_SORT_COLUMNS = "id record_last_updated group_name externally_managed";

    public static final String USER_SORT_COLUMNS = "id username firstName lastName externally_managed email enabled";

    public static final String ITEM_SORT_FIELDS = "id dateModified label";

    public static final String PUBLISH_REQUEST_SORT_FIELDS = "id dateScheduled label";

    public static final String ITEM_TYPE_VALUES = "asset|component|content type|document|file|folder|levelDescriptor|page|renderingTemplate|script|taxonomy";

    private RequestConstants() {
    }
}
