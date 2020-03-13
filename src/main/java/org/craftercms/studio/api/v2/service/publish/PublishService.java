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

package org.craftercms.studio.api.v2.service.publish;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;

import java.util.List;

public interface PublishService {

    /**
     * Get total number of publishing packages for given search parameters
     *
     * @param siteId site identifier
     * @param environment publishing environment
     * @param path  regular expression for paths
     * @param states publishing package states
     *
     * @return total number of publishing packages
     */
    int getPublishingPackagesTotal(String siteId, String environment, String path, List<String> states)
            throws SiteNotFoundException;

    /**
     * Get publishing packages for given search parameters
     *
     * @param siteId site identifier
     * @param environment publishing environment
     * @param path regular expression for paths
     * @param states publishing package states
     * @param offset offset for pagination
     * @param limit limit for pagination
     * @return list of publishing packages
     */
    List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path, List<String> states,
                                                  int offset, int limit) throws SiteNotFoundException;

    /**
     * Get publishing package details
     *
     * @param siteId site identifier
     * @param packageId package identifier
     *
     * @return publishing package details
     */
    PublishingPackageDetails getPublishingPackageDetails(String siteId, String packageId) throws SiteNotFoundException;

    /**
     * Cancel publishing packages
     *
     * @param siteId site identifier
     * @param packageIds list of package identifiers
     */
    void cancelPublishingPackages(String siteId, List<String> packageIds) throws SiteNotFoundException;
}
