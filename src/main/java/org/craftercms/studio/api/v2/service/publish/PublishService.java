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

package org.craftercms.studio.api.v2.service.publish;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryGroup;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.model.rest.dashboard.PublishingDashboardItem;

import java.time.ZonedDateTime;
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

    /**
     * Get total number of publishing history items for given search parameters
     *
     * @param siteId site identifier
     * @param environment environment to get publishing history
     * @param path regular expression to filter paths
     * @param publisher filter publishing history for specified user
     * @param dateFrom lower boundary for date range
     * @param dateTo upper boundary for date range
     * @param contentType publishing history for specified content type
     * @param state filter items by their state
     *
     * @return total number of deployment history items
     */
    int getPublishingHistoryTotal(String siteId, String environment, String path, String publisher,
                                  ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType, long state);

    /**
     * Get deployment history items for given search parameters
     *
     * @param siteId site identifier
     * @param environment environment to get publishing history
     * @param path regular expression to filter paths
     * @param publisher filter publishing history for specified user
     * @param dateFrom lower boundary for date range
     * @param dateTo upper boundary for date range
     * @param contentType publishing history for specified content type
     * @param state filter items by their state
     * @param sortBy sort publishing history
     * @param order apply order to publishing history
     * @param offset offset of the first item in the result set
     * @param limit number of items to return
     *
     * @return total number of publishing packages
     */
    List<PublishingDashboardItem> getPublishingHistory(String siteId, String environment, String path, String publisher,
                                                       ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType,
                                                       long state, String sortBy, String order, int offset, int limit);

    /**
     * Get deployment history
     * @param siteId site identifier
     * @param daysFromToday number of days for history
     * @param numberOfItems number of items to display
     * @param filterType filter results by filter type
     * @return
     */
    List<DeploymentHistoryGroup> getDeploymentHistory(String siteId, int daysFromToday, int numberOfItems,
                                                      String filterType);

    /**
     * Publishes all changes for the given site & target
     *
     * @param siteId the id of the site
     * @param publishingTarget the publishing target
     * @throws ServiceLayerException if there is any error during publishing
     */
    void publishAll(String siteId, String publishingTarget) throws ServiceLayerException, UserNotFoundException, CryptoException;

}
