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

package org.craftercms.studio.controller.rest.v2;

/**
 * Keys used for the results in the Rest API.
 * @author joseross
 */
public final class ResultConstants {

    public static final String RESULT_KEY_MESSAGE = "message";

    public static final String RESULT_KEY_GROUP = "group";
    public static final String RESULT_KEY_GROUPS = "groups";

    public static final String RESULT_KEY_USER = "user";
    public static final String RESULT_KEY_USERS = "users";
    public static final String RESULT_KEY_CURRENT_USER = "authenticatedUser";
    public static final String RESULT_KEY_LOGOUT_URL = "logoutUrl";

    public static final String RESULT_KEY_SITES = "sites";

    public static final String RESULT_KEY_ROLES = "roles";

    public static final String RESULT_KEY_ITEM = "item";
    public static final String RESULT_KEY_ITEMS = "items";
    public static final String RESULT_KEY_MENU_ITEMS = "menuItems";

    public static final String RESULT_KEY_CLUSTER_MEMBER = "clusterMember";
    public static final String RESULT_KEY_CLUSTER_MEMBERS = "clusterMembers";

    public static final String RESULT_KEY_ENVIRONMENT = "environment";

    public static final String RESULT_KEY_BLUEPRINTS = "blueprints";

    public static final String RESULT_KEY_RESULT = "result";

    /* Audit Controller */
    public static final String RESULT_KEY_AUDIT_LOG = "auditLog";

    public static final String RESULT_KEY_STAUS = "status";
    public static final String RESULT_KEY_VERSION = "version";
    public static final String RESULT_KEY_MEMORY = "memory";
    public static final String RESULT_KEY_EVENTS = "events";

    /* Repository management controller */
    public static final String RESULT_KEY_REMOTES = "remotes";
    public static final String RESULT_KEY_REPOSITORY_STATUS = "repositoryStatus";
    public static final String RESULT_KEY_DIFF = "diff";

    public static final String RESULT_KEY_CORRUPTED = "corrupted";

    /* Dependency controller */
    public static final String RESULT_KEY_SOFT_DEPENDENCIES = "softDependencies";
    public static final String RESULT_KEY_HARD_DEPENDENCIES = "hardDependencies";

    /* Marketplace controller */
    public static final String RESULT_KEY_PLUGINS = "plugins";

    /* Configuration controller */
    public static final String RESULT_KEY_HISTORY = "history";

    /* Content controller */
    public static final String RESULT_KEY_CHILD_ITEMS = "childItems";
    public static final String RESULT_KEY_DEPENDENT_ITEMS = "dependentItems";

    /** Publish controller */
    public static final String RESULT_KEY_PACKAGES = "packages";
    public static final String RESULT_KEY_PACKAGE = "package";
    public static final String RESULT_KEY_PUBLISH_STATUS = "publishingStatus";
    public static final String RESULT_KEY_PUBLISH_HISTORY = "documents";

    private ResultConstants() { }
}
