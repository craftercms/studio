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
}
