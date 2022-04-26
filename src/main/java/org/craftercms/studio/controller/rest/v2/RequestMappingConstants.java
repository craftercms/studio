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

public interface RequestMappingConstants {

    String ALL_SUB_URLS = "/**";

    /** API 2 Root */
    String API_2 = "/api/2";

    /** Proxy Controller */
    String PROXY_ENGINE = "/engine";

    /** Dashboard Controller */
    String DASHBOARD = "/dashboard";
    String AUDIT_DASHBOARD = "/audit";
    String PUBLISHING_DASHBOARD = "/publishing";
    String CONTENT_DASHBOARD = "/content";

    /** Users controller */
    String USERS = "/users";
    String PATH_PARAM_ID = "/{id}";
    String ENABLE = "/enable";
    String DISABLE = "/disable";
    String SITES = "/sites";
    String PATH_PARAM_SITE = "/{site}";
    String ROLES = "/roles";
    String ME = "/me";
    String LOGOUT_SSO_URL = "/logout/sso/url";
    String FORGOT_PASSWORD = "/forgot_password";
    String CHANGE_PASSWORD = "/change_password";
    String SET_PASSWORD = "/set_password";
    String RESET_PASSWORD = "/reset_password";
    String VALIDATE_TOKEN = "/validate_token";

    /** Repository Management controller **/
    String REPOSITORY = "/repository";
    String ADD_REMOTE = "/add_remote";
    String LIST_REMOTES = "/list_remotes";
    String PULL_FROM_REMOTE = "/pull_from_remote";
    String PUSH_TO_REMOTE = "/push_to_remote";
    String REBUILD_DATABASE = "/rebuild_database";
    String REMOVE_REMOTE = "/remove_remote";
    String STATUS = "/status";
    String RESOLVE_CONFLICT = "/resolve_conflict";
    String DIFF_CONFLICTED_FILE = "/diff_conflicted_file";
    String COMMIT_RESOLUTION = "/commit_resolution";
    String CANCEL_FAILED_PULL = "/cancel_failed_pull";
    String UNLOCK = "/unlock";

    String CORRUPTED = "/corrupted";

    String REPAIR = "/repair";

    /** Publish Controller */
    String PUBLISH = "/publish";
    String PACKAGES = "/packages";
    String PACKAGE = "/package";
    String CANCEL = "/cancel";
    String CLEAR_LOCK = "/clear_lock";
    String HISTORY = "/history";
}
