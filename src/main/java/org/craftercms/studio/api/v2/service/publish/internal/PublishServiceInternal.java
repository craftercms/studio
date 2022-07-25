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

package org.craftercms.studio.api.v2.service.publish.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.time.ZonedDateTime;
import java.util.List;

public interface PublishServiceInternal {

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
    int getPublishingPackagesTotal(String siteId, String environment, String path, List<String> states);

    /**
     * Get publishing packages for given search parameters
     *
     * @param siteId site identifier
     * @param environment publishing environment
     * @param path regular expression for paths
     * @param states publishing package state
     * @param offset offset for pagination
     * @param limit limit for pagination
     * @return list of publishing packages
     */
    List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path, List<String> states,
                                                  int offset, int limit);

    /**
     * Get publishing package details
     *
     * @param siteId site identifier
     * @param packageId package identifier
     *
     * @return publishing package details
     */
    PublishingPackageDetails getPublishingPackageDetails(String siteId, String packageId);

    /**
     * Cancel publishing packages
     *
     * @param siteId site identifier
     * @param packageIds list of package identifiers
     */
    void cancelPublishingPackages(String siteId, List<String> packageIds);
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
    List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String path, String publisher,
                                                     ZonedDateTime dateFrom, ZonedDateTime dateTo, String contentType,
                                                     long state, String sortBy, String order, int offset, int limit);

    /**
     * Get deployment history from database
     * @param siteId site identifier
     * @param environments list of environments
     * @param fromDate starting date for filtering results
     * @param toDate end date for filtering results
     * @param filterType filter type
     * @param numberOfItems number of items to get
     * @return
     */
    List<DeploymentHistoryItem> getDeploymentHistory(String siteId, List<String> environments, ZonedDateTime fromDate,
                                                     ZonedDateTime toDate, String filterType, int numberOfItems);

    /**
     * Cancel scheduled items from publishing queue
     * @param siteId site identifier
     * @param paths list of paths of content items to be cancelled
     */
    void cancelScheduledQueueItems(String siteId, List<String> paths);

    /**
     * Check if site has ever been published.
     *
     * @param siteId site identifier
     * @return true if site has been published at least once, otherwise false
     */
    boolean isSitePublished(String siteId);

    /**
     * Execute initial publish for given site.
     *
     * @param siteId site identifier
     */
    void initialPublish(String siteId) throws SiteNotFoundException;

    /**
     * Get total number of scheduled publishing packages for given filters
     *
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param dateFrom lower boundary for schedule
     * @param dateTo upper boundary for schedule
     * @return total number of results
     */
    int getPublishingPackagesScheduledTotal(String siteId, String publishingTarget, ZonedDateTime dateFrom,
                                            ZonedDateTime dateTo);

    /**
     * Get scheduled publishing packages
     *
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param dateFrom lower boundary for schedule
     * @param dateTo upper boundary for schedule
     * @param offset offset of the first result
     * @param limit limit number of results
     * @return list of dashboard publishing packages
     */
    List<DashboardPublishingPackage> getPublishingPackagesScheduled(String siteId, String publishingTarget,
                                                                    ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                                    int offset, int limit);

    /**
     * Get total number of publishing packages for given filters
     *
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param approver approver
     * @param dateFrom lower boundary for history
     * @param dateTo upper boundary for history
     * @return total number of results
     */
    int getPublishingPackagesHistoryTotal(String siteId, String publishingTarget, String approver,
                                          ZonedDateTime dateFrom, ZonedDateTime dateTo);

    /**
     * Get publishing packages history
     *
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param approver approver
     * @param dateFrom lower boundary for history
     * @param dateTo upper boundary for history
     * @param offset offset of the first result
     * @param limit limit number of results
     * @return list of dashboard publishing packages
     */
    List<DashboardPublishingPackage> getPublishingPackagesHistory(String siteId, String publishingTarget,
                                                                  String approver, ZonedDateTime dateFrom,
                                                                  ZonedDateTime dateTo, int offset, int limit);

    /**
     * Get number of publishes for site in given number of days
     * @param siteId site identifiers
     * @param days number of days
     * @return number of publishes
     */
    int getNumberOfPublishes(String siteId, int days);

    /**
     * Get number of published items for site in given number of days filtered by their previous state
     * @param siteId site identifier
     * @param days number of days
     * @param activityAction the activity action to filter
     * @param publishState  the publishing state to filter
     * @param publishAction the publishing action to filter
     * @return number of newly created <nd published items
     */
    int getNumberOfPublishedItemsByState(String siteId, int days, String activityAction, String publishState,
                                         String publishAction);

    /**
     * Publishes all changes for the given site & target
     *
     * @param siteId the id of the site
     * @param publishingTarget the publishing target
     * @throws ServiceLayerException if there is any error during publishing
     */
    void publishAll(String siteId, String publishingTarget, String comment) throws ServiceLayerException;

}
