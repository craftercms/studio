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

public interface BaseDAO {

    /** Parameter Names */
    String PARAM_NAME_SITE_ID = "siteId";
    String PARAM_NAME_PATH = "path";

    /** Pagination parameter names */
    String PARAM_NAME_OFFSET = "offset";
    String PARAM_NAME_LIMIT = "limit";

    /** Publish Request */
    String PARAM_NAME_ENVIRONMENT = "environment";
    String PARAM_NAME_STATE = "state";
    String PARAM_NAME_STATES = "states";
    String PARAM_NAME_PACKAGE_ID = "packageId";
    String PARAM_NAME_PACKAGE_IDS = "packageIds";
    String PARAM_NAME_CANCELLED_STATE = "cancelledState";
}
