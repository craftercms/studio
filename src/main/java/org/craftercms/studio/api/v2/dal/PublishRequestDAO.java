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

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PublishRequestDAO extends BaseDAO {

    /**
     * Get total number of publishing package for given search filters
     *
     * @param siteId site identifier
     * @param environment publishing environment
     * @param states publishing package state
     * @param path regular expression for paths
     *
     * @return number of publishing packages
     */
    int getPublishingPackagesTotal(@Param(PARAM_NAME_SITE_ID) String siteId,
                                   @Param(PARAM_NAME_ENVIRONMENT) String environment,
                                   @Param(PARAM_NAME_PATH) String path,
                                   @Param(PARAM_NAME_STATES) List<String> states);

    /**
     * Get publishing packages for given search filters
     * @param siteId site identifier
     * @param environment environment
     * @param states publishing states package
     * @param path regular expression for paths
     * @param offset offset for pagination
     * @param limit limit for pagination
     * @return list of publishing packages
     */
    List<PublishingPackage> getPublishingPackages(@Param(PARAM_NAME_SITE_ID) String siteId,
                                                  @Param(PARAM_NAME_ENVIRONMENT) String environment,
                                                  @Param(PARAM_NAME_PATH) String path,
                                                  @Param(PARAM_NAME_STATES) List<String> states,
                                                  @Param(PARAM_NAME_OFFSET) int offset,
                                                  @Param(PARAM_NAME_LIMIT) int limit);

    /**
     * Get publishing package details
     *
     * @param siteId site identifier
     * @param packageId package id
     * @return list of publishing requests belonging to the package
     */
    List<PublishRequest> getPublishingPackageDetails(@Param(PARAM_NAME_SITE_ID) String siteId,
                                                     @Param(PARAM_NAME_PACKAGE_ID) String packageId);

    /**
     * Cancel publishing packages
     *
     * @param siteId site identifier
     * @param packageIds list of package identifiers
     * @param cancelledState cancelled state
     */
    void cancelPackages(@Param(PARAM_NAME_SITE_ID) String siteId,
                        @Param(PARAM_NAME_PACKAGE_IDS) List<String> packageIds,
                        @Param(PARAM_NAME_CANCELLED_STATE) String cancelledState);
}
