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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ACTIVITY_ACTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.APPROVER;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CANCELLED_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.COMPLETED_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CONTENT_TYPE_CLASS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DAYS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ENVIRONMENT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ENVIRONMENTS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.FROM_DATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.NOW;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PACKAGE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PACKAGE_IDS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATHS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PUBLISHING_TARGET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PUBLISH_ACTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PUBLISH_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.READY_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SCHEDULED_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.TO_DATE;

public interface PublishRequestDAO {

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
    int getPublishingPackagesTotal(@Param(SITE_ID) String siteId,
                                   @Param(ENVIRONMENT) String environment,
                                   @Param(PATH) String path,
                                   @Param(STATES) List<String> states);

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
    List<PublishingPackage> getPublishingPackages(@Param(SITE_ID) String siteId,
                                                  @Param(ENVIRONMENT) String environment,
                                                  @Param(PATH) String path,
                                                  @Param(STATES) List<String> states,
                                                  @Param(OFFSET) int offset,
                                                  @Param(LIMIT) int limit);

    /**
     * Get publishing package details
     *
     * @param siteId site identifier
     * @param packageId package id
     * @return list of publishing requests belonging to the package
     */
    List<PublishRequest> getPublishingPackageDetails(@Param(SITE_ID) String siteId,
                                                     @Param(PACKAGE_ID) String packageId);

    /**
     * Cancel publishing packages
     *
     * @param siteId site identifier
     * @param packageIds list of package identifiers
     * @param cancelledState cancelled state
     */
    void cancelPackages(@Param(SITE_ID) String siteId,
                        @Param(PACKAGE_IDS) List<String> packageIds,
                        @Param(CANCELLED_STATE) String cancelledState);

    /**
     * Get scheduled date for environment item
     * @param siteId site identifier
     * @param path path of the item
     * @param environment environment
     * @param state publishing queue ready state
     * @param now now
     * @return Scheduled date
     */
    ZonedDateTime getScheduledDateForEnvironment(@Param(SITE_ID) String siteId,
                                                 @Param(PATH) String path,
                                                 @Param(ENVIRONMENT) String environment,
                                                 @Param(STATE) String state,
                                                 @Param(NOW) ZonedDateTime now);

    /**
     * Get deployment history
     * @param siteId site identifier
     * @param environments environments
     * @param completedState completed state
     * @param fromDate get history from date
     * @param toDate get history to date
     * @param offset offset for pagination
     * @param limit number of records to return
     * @return
     */
    List<PublishRequest> getDeploymentHistory(@Param(SITE_ID) String siteId,
                                              @Param(ENVIRONMENTS) List<String> environments,
                                              @Param(COMPLETED_STATE) String completedState,
                                              @Param(CONTENT_TYPE_CLASS) String contentTypeClass,
                                              @Param(FROM_DATE) ZonedDateTime fromDate,
                                              @Param(TO_DATE) ZonedDateTime toDate, @Param(OFFSET) int offset,
                                              @Param(LIMIT) int limit);

    /**
     * Get scheduled items for given site
     * @param siteId site identifier
     * @param state ready for live state
     * @param contentTypeClass filter by content type class
     * @param now current date time
     * @return
     */
    List<PublishRequest> getScheduledItems(@Param(SITE_ID) String siteId,
                                           @Param(STATE) String state,
                                           @Param(CONTENT_TYPE_CLASS) String contentTypeClass,
                                           @Param(NOW) ZonedDateTime now);

    /**
     * Cancel scheduled items from publishing queue
     * @param siteId site identifier
     * @param paths list of paths of content items to be cancelled
     * @param now timestamp now
     * @param cancelledState cancelled state value
     * @param readyState ready for live state value
     */
    void cancelScheduledQueueItems(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths,
                                   @Param(NOW) ZonedDateTime now, @Param(CANCELLED_STATE) String cancelledState,
                                   @Param(READY_STATE) String readyState);

    /**
     * Get number of scheduled publishing packages results for given filters
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param scheduledState scheduled state
     * @param fromDate get history from date
     * @param toDate get history to date
     * @return total number of results
     */
    Optional<Integer> getPublishingPackagesScheduledTotal(@Param(SITE_ID) String siteId,
                                                          @Param(PUBLISHING_TARGET) String publishingTarget,
                                                          @Param(SCHEDULED_STATE) String scheduledState,
                                                          @Param(FROM_DATE) ZonedDateTime fromDate,
                                                          @Param(TO_DATE) ZonedDateTime toDate);
    /**
     * Get scheduled publishing packages
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param scheduledState scheduled state
     * @param fromDate get history from date
     * @param toDate get history to date
     * @param offset offset for pagination
     * @param limit number of records to return
     * @return
     */
    List<DashboardPublishingPackage> getPublishingPackagesScheduled(@Param(SITE_ID) String siteId,
                                                                    @Param(PUBLISHING_TARGET) String publishingTarget,
                                                                    @Param(SCHEDULED_STATE) String scheduledState,
                                                                    @Param(FROM_DATE) ZonedDateTime fromDate,
                                                                    @Param(TO_DATE) ZonedDateTime toDate,
                                                                    @Param(OFFSET) int offset,
                                                                    @Param(LIMIT) int limit);

    /**
     * Get number of publishing packages history results for given filters
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param approver approver
     * @param completedState completed state
     * @param fromDate get history from date
     * @param toDate get history to date
     * @return total number of results
     */
    Optional<Integer> getPublishingPackagesHistoryTotal(@Param(SITE_ID) String siteId,
                                                        @Param(PUBLISHING_TARGET) String publishingTarget,
                                                        @Param(APPROVER) String approver,
                                                        @Param(COMPLETED_STATE) String completedState,
                                                        @Param(FROM_DATE) ZonedDateTime fromDate,
                                                        @Param(TO_DATE) ZonedDateTime toDate);
    /**
     * Get deployment history
     * @param siteId site identifier
     * @param publishingTarget publishing target
     * @param approver approver
     * @param completedState completed state
     * @param fromDate get history from date
     * @param toDate get history to date
     * @param offset offset for pagination
     * @param limit number of records to return
     * @return
     */
    List<DashboardPublishingPackage> getPublishingPackagesHistory(@Param(SITE_ID) String siteId,
                                                                  @Param(PUBLISHING_TARGET) String publishingTarget,
                                                                  @Param(APPROVER) String approver,
                                                                  @Param(COMPLETED_STATE) String completedState,
                                                                  @Param(FROM_DATE) ZonedDateTime fromDate,
                                                                  @Param(TO_DATE) ZonedDateTime toDate,
                                                                  @Param(OFFSET) int offset,
                                                                  @Param(LIMIT) int limit);

    /**
     * Get number of publishes for site in given number of days
     * @param siteId site identifiers
     * @param days number of days
     * @return number of publishes
     */
    int getNumberOfPublishes(@Param(SITE_ID) String siteId, @Param(DAYS) int days);

    /**
     * Get number of published items for site in given number of days filtered by their previous state
     * @param siteId site identifier
     * @param days number of days
     * @param activityAction the activity action to filter
     * @param publishState  the publishing state to filter
     * @param publishAction the publishing action to filter
     * @return number of newly created <nd published items
     */
    int getNumberOfPublishedItemsByState(@Param(SITE_ID) String siteId, @Param(DAYS) int days,
                                        @Param(ACTIVITY_ACTION) String activityAction,
                                        @Param(PUBLISH_STATE) String publishState,
                                        @Param(PUBLISH_ACTION) String publishAction);
}
