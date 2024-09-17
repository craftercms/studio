/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal.publish;

import org.apache.ibatis.annotations.Param;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.QueryParameterNames;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.COMPLETED_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DAYS;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.PublishState.PENDING;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.APPROVED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.SUBMITTED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.COMPLETED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.READY;

/**
 * Provide access to DB publish related tables
 */
public interface PublishDAO {
    String SITE_ID = "siteId";
    String PATH = "path";
    String PATHS = "paths";
    String TARGET = "target";
    String PACKAGE_ID = "packageId";
    String PACKAGE_IDS = "packageIds";
    String PUBLISH_PACKAGE = "publishPackage";
    String PACKAGE_READY_STATE = "readyState";
    String ITEMS = "items";
    String APPROVAL_STATES = "approvalStates";
    String PACKAGE_STATE = "packageState";
    String CANCELLED_STATE = "cancelledState";
    String SITE_STATES = "siteStates";
    String ERROR = "error";
    String ITEM_SUCCESS_STATE = "itemSuccessState";
    String ITEM_PUBLISHED_STATE = "publishState";
    String ON_STATES_BIT_MAP = "onStatesBitMap";
    String OFF_STATES_BIT_MAP = "offStatesBitMap";
    String INCLUDE_CHILDREN = "includeChildren";

    String PUBLISH_PACKAGE_STATE = "packageState";
    String IS_SCHEDULED_BIT = "isScheduledBit";
    String PUBLISH_PACKAGE_APPROVAL_STATES = "approvalStates";
    String SUCCESS_ON_BIT_MAP = "successOnStatesBitMap";
    String SUCCESS_OFF_BIT_MAP = "successOffStatesBitMap";
    String FAILURE_OFF_BIT_MAP = "failureOffStatesBitMap";

    String OFFSET = "offset";
    String LIMIT = "limit";
    String ACTION = "action";

    List<ApprovalState> ACTIVE_APPROVAL_STATES = List.of(SUBMITTED, APPROVED);

    /**
     * Convenience transactional method to create a package and its items
     *
     * @param publishPackage the package
     * @param publishItems   the items
     * @param isLiveTarget   if the target is live
     */
    @Transactional
    default void insertPackageAndItems(final PublishPackage publishPackage, final Collection<PublishItem> publishItems, boolean isLiveTarget) {
        insertPackage(publishPackage, READY.value);
        if (!isEmpty(publishItems)) {
            insertItems(publishPackage.getId(), publishItems, PENDING.value);
            insertItemPublishItems(publishPackage.getId());
            updateItemStateBitsForNewPackage(publishPackage, isLiveTarget);
        }
    }

    /**
     * Update the item state bits for all items in a newly created package
     *
     * @param publishPackage the package
     * @param isLiveTarget   if the target is live
     */
    default void updateItemStateBitsForNewPackage(final PublishPackage publishPackage, boolean isLiveTarget) {
        long onMask = 0;
        long offMask = 0;
        if (publishPackage.getSchedule() != null) {
            onMask |= SCHEDULED.value;
        }
        if (SUBMITTED.equals(publishPackage.getApprovalState())) {
            onMask |= ItemState.IN_WORKFLOW.value;
        }
        if (isLiveTarget) {
            onMask |= ItemState.DESTINATION.value;
        }
        updateItemStateBits(publishPackage.getId(), onMask, offMask);
    }

    /**
     * Update the item state bits for all items in a package
     *
     * @param packageId       the package id
     * @param onStatesBitMap  the state bits to set to on
     * @param offStatesBitMap the state bits to set to off
     */
    void updateItemStateBits(@Param(PACKAGE_ID) long packageId,
                             @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                             @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Insert item_publish_item records for the publish_item's belonging to the given package.
     * Notice this will insert a record to item_publish_item table for each non-delete publishItem
     *
     * @param packageId the package id
     */
    void insertItemPublishItems(@Param(PACKAGE_ID) long packageId);

    /**
     * Insert a new publish package
     *
     * @param publishPackage the package to insert
     */
    void insertPackage(@Param(PUBLISH_PACKAGE) PublishPackage publishPackage, @Param(PACKAGE_READY_STATE) long packageState);

    /**
     * Insert items into a publish package
     *
     * @param packageId    the package id
     * @param publishItems the items to insert
     * @param publishState the state to set the items to
     */
    void insertItems(@Param(PACKAGE_ID) long packageId,
                     @Param(ITEMS) Collection<PublishItem> publishItems,
                     @Param(ITEM_PUBLISHED_STATE) long publishState);

    /**
     * Get the next publish packages to process for every site matching the given states
     *
     * @return the next publish packages to process
     */
    default List<PublishPackage> getNextPublishPackages() {
        return getNextPublishPackages(List.of(APPROVED), READY.value, List.of(Site.State.READY));
    }

    /**
     * Get the next publish packages to process for every site matching the given states
     *
     * @param approvalStates the package approval states to match
     * @param packageState   the package state to match
     * @param siteStates     the site states to match
     * @return the next publish packages to process
     */
    List<PublishPackage> getNextPublishPackages(@Param(APPROVAL_STATES) List<ApprovalState> approvalStates,
                                                @Param(PACKAGE_STATE) long packageState,
                                                @Param(SITE_STATES) List<String> siteStates);

    /**
     * Get a package by id
     *
     * @param siteId    the site id
     * @param packageId the package id
     * @return the {@link PublishPackage}
     */
    PublishPackage getById(@Param(SITE_ID) final long siteId, @Param(PACKAGE_ID) final long packageId);

    /**
     * Get multiple publish packages by ids
     *
     * @param siteId     the site id
     * @param packageIds the package ids
     * @return the collection of {@link PublishPackage}
     */
    Collection<PublishPackage> getByIds(@Param(SITE_ID) final long siteId, @Param(PACKAGE_IDS) final Collection<Long> packageIds);

    /**
     * Update a package
     */
    void updatePackage(@Param(PUBLISH_PACKAGE) final PublishPackage publishPackage);

    /**
     * Cancel all active (ready non-rejected) packages for a site and a target
     *
     * @param siteId the site id
     * @param target the target
     */
    default void cancelOutstandingPackages(final long siteId, final String target) {
        cancelOutstandingPackages(siteId, target, PackageState.CANCELLED.value, READY.value, ACTIVE_APPROVAL_STATES);
    }

    /**
     * Cancel all active (ready non-rejected) packages for a site
     *
     * @param siteId the site id
     */
    default void cancelAllOutstandingPackages(final long siteId) {
        cancelOutstandingPackages(siteId, null, PackageState.CANCELLED.value, READY.value, ACTIVE_APPROVAL_STATES);
    }


    /**
     * Cancel all packages matching the approval and processing states.
     * Set packages' state to cancelledState parameter value
     *
     * @param siteId         the site id
     * @param target         the target to cancel packages for, null for all targets
     * @param cancelledState the state to set the packages to
     * @param stateToCancel  the package state to match
     * @param approvalStates the approval states to filter by (it will match packages having any of the given flags in their approval_state)
     */
    void cancelOutstandingPackages(@Param(SITE_ID) long siteId,
                                   @Param(TARGET) String target,
                                   @Param(CANCELLED_STATE) long cancelledState,
                                   @Param(PACKAGE_STATE) long stateToCancel,
                                   @Param(APPROVAL_STATES) Collection<ApprovalState> approvalStates);

    /**
     * Update the corresponding items' states for successful publish items in the package.
     *
     * @param packageId        the package id
     * @param successOnMask    states to flip on for successful items
     * @param successOffMask   states to flip off for successful items
     * @param failureOffMask   states to flip off for failed items
     * @param itemSuccessState the state of the successful items to filter
     */
    @Transactional
    default void updateItemStatesForCompletePackage(final long packageId, final long successOnMask,
                                                    final long successOffMask, final long failureOffMask,
                                                    final long itemSuccessState, final String liveTarget) {
        updateItemStatesForCompletePackageInternal(packageId, successOnMask, successOffMask,
                failureOffMask, itemSuccessState);
        recalculateItemStateBits(packageId, liveTarget);
    }

    /**
     * Update the corresponding items' states for the items in the package.
     *
     * @param packageId        the package id
     * @param successOnMask    states to flip on for successful items
     * @param successOffMask   states to flip off for successful items
     * @param failureOffMask   states to flip off for failed items
     * @param itemSuccessState the state of the successful items to filter
     */
    void updateItemStatesForCompletePackageInternal(@Param(PACKAGE_ID) long packageId,
                                                    @Param(SUCCESS_ON_BIT_MAP) long successOnMask,
                                                    @Param(SUCCESS_OFF_BIT_MAP) long successOffMask,
                                                    @Param(FAILURE_OFF_BIT_MAP) long failureOffMask,
                                                    @Param(PublishDAO.ITEM_SUCCESS_STATE) long itemSuccessState);

    /**
     * Persist changes to a cancelled or rejected publish package.
     * This will update the package in the db and update the state bits for the items in the package.
     * Then imte state bits will be recalculated for affected publish_items
     * in the package, considering that the affected items might be part of other submitted packages.
     *
     * @param publishPackage the package to cancel
     * @param liveTarget     the live target for this site
     */
    @Transactional
    default void cancelPackage(final PublishPackage publishPackage, final String liveTarget) {
        updatePackage(publishPackage);
        updateItemStateBits(publishPackage.getId(), 0, CANCEL_PUBLISHING_PACKAGE_OFF_MASK);
        recalculateItemStateBits(publishPackage.getId(), liveTarget);
    }

    /**
     * Recalculate the state bits for the items in the given complete package.
     * It will update the state bits for the items in the package based on remaining submitted/approved packages
     * This method is meant to preserve certain bits ( workflow, scheduled, destination) that would otherwise be cleared
     * by the current complete/cancelled package
     *
     * @param packageId  the package id
     * @param liveTarget the live target for this site
     */
    default void recalculateItemStateBits(final long packageId, String liveTarget) {
        recalculateItemStateBits(packageId, List.of(SUBMITTED), IN_WORKFLOW.value, READY.value, false, null);
        recalculateItemStateBits(packageId, ACTIVE_APPROVAL_STATES, SCHEDULED.value, READY.value, true, null);
        recalculateItemStateBits(packageId, ACTIVE_APPROVAL_STATES, DESTINATION.value, READY.value, false, liveTarget);
    }

    /**
     * Apply the onStatesBitMap state bitmap mask to items in the completed package if there is another package
     * matching the approvalStates and packageState that contains the item.
     *
     * @param packageId      package id
     * @param approvalStates package approval states to filter packages
     * @param onStatesBitMap workflow state bit value
     * @param packageState   package state bit value to filter packages
     * @param isScheduled    indicates if this update is for scheduled bit (true) or in_workflow bit (false)
     */
    void recalculateItemStateBits(@Param(PACKAGE_ID) long packageId,
                                  @Param(PUBLISH_PACKAGE_APPROVAL_STATES) Collection<ApprovalState> approvalStates,
                                  @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                                  @Param(PUBLISH_PACKAGE_STATE) long packageState,
                                  @Param(IS_SCHEDULED_BIT) boolean isScheduled,
                                  @Param(TARGET) String target);

    /**
     * Get the publish items for the given package
     *
     * @param siteId    the site id
     * @param packageId the package id
     * @return PublishItem records for the package
     */
    default Collection<PublishItem> getPublishItems(final String siteId, final long packageId) {
        return getPublishItems(siteId, packageId, null, null);
    }

    /**
     * Get the publish items for the given package
     *
     * @param siteId    the site id
     * @param packageId the package id
     * @param offset    the offset to start from
     * @param limit     the max number of items to return
     * @return PublishItem records for the package
     */
    Collection<PublishItem> getPublishItems(@Param(SITE_ID) String siteId, @Param(PACKAGE_ID) long packageId,
                                            @Param(OFFSET) Integer offset, @Param(LIMIT) Integer limit);

    /**
     * Get the total number of items in a package
     *
     * @param siteId    the site id
     * @param packageId the package id
     * @return the total number of items in the package
     */
    int getPublishItemsCount(@Param(SITE_ID) String siteId, @Param(PACKAGE_ID) long packageId);

    /**
     * Update the state for all publish items in the package
     *
     * @param id              the package id
     * @param onStatesBitMap  the state bits to set to on
     * @param offStatesBitMap the state bits to set to off
     */
    void updatePublishItemState(@Param(PACKAGE_ID) long id,
                                @Param(ON_STATES_BIT_MAP) long onStatesBitMap,
                                @Param(OFF_STATES_BIT_MAP) long offStatesBitMap);

    /**
     * Update the state and error (if any) for the given publish items
     *
     * @param items the publish item to update state and error columns for
     */
    void updatePublishItemListState(@Param(ITEMS) Collection<PublishItem> items);

    /**
     * Get a submitted package with READY state containing the given item
     *
     * @param siteId          the site id
     * @param path            the path of the item
     * @param includeChildren whether to include the children of the paths in the search
     * @return the package containing the item, or null if the item is not submitted to be published
     */
    default PublishPackage getReadyPackageForItem(final String siteId, final String path, final boolean includeChildren) {
        Collection<PublishPackage> packages = getItemPackages(siteId, null, List.of(path), READY.value, ACTIVE_APPROVAL_STATES, includeChildren);
        return packages.isEmpty() ? null : packages.iterator().next();
    }

    /**
     * Get the ready packages containing the given item
     *
     * @param siteId the site id
     * @param path   the path of the item
     * @return collection of ready packages containing the item
     */
    default Collection<PublishPackage> getReadyPackagesForItem(final String siteId, final String path) {
        return getItemPackages(siteId, null, List.of(path), READY.value, ACTIVE_APPROVAL_STATES, false);
    }

    /**
     * Get the submitted/approved package containing the given item
     *
     * @param siteId       the site id
     * @param path         the path of the item
     * @param packageState the mask to apply to filter the package state
     * @return the package containing the item, or null if the item is not submitted to be published
     */
    default PublishPackage getPackageForItem(final String siteId,
                                             final String path,
                                             final long packageState) {
        return getPackageForItems(siteId, List.of(path), packageState, ACTIVE_APPROVAL_STATES);
    }

    /**
     * Get the submitted package containing the given items
     *
     * @param siteId       the site id
     * @param paths        the paths of the items
     * @param packageState the mask to apply to filter the package state
     * @return the package containing the items, or null if the items are not submitted to be published
     */
    PublishPackage getPackageForItems(@Param(SITE_ID) String siteId,
                                      @Param(PATHS) Collection<String> paths,
                                      @Param(PACKAGE_STATE) long packageState,
                                      @Param(APPROVAL_STATES) List<ApprovalState> approvalStates);

    /**
     * Get the packages containing the given item that match the given filters
     *
     * @param siteId          the site id
     * @param paths           the paths of the items
     * @param packageState    the mask to apply to filter the package state
     * @param includeChildren whether to include the children of the paths in the search
     * @return collection of matching packages
     */
    default Collection<PublishPackage> getItemPackages(@Param(SITE_ID) String siteId,
                                                       @Param(TARGET) String target,
                                                       @Param(PATHS) Collection<String> paths,
                                                       @Param(PACKAGE_STATE) long packageState,
                                                       @Param(APPROVAL_STATES) List<ApprovalState> approvalStates,
                                                       @Param(INCLUDE_CHILDREN) boolean includeChildren) {
        return getItemPackages(siteId, target, paths, packageState, approvalStates, includeChildren, null, null);
    }


    /**
     * Get the packages containing the given item that match the given filters
     *
     * @param siteId          the site id
     * @param paths           the paths of the items
     * @param packageState    the mask to apply to filter the package state
     * @param includeChildren whether to include the children of the paths in the search
     * @param offset          the offset to start from
     * @param limit           the max number of items to return
     * @return collection of matching packages
     */
    Collection<PublishPackage> getItemPackages(@Param(SITE_ID) String siteId,
                                               @Param(TARGET) String target,
                                               @Param(PATHS) Collection<String> paths,
                                               @Param(PACKAGE_STATE) Long packageState,
                                               @Param(APPROVAL_STATES) Collection<ApprovalState> approvalStates,
                                               @Param(INCLUDE_CHILDREN) boolean includeChildren,
                                               @Param(OFFSET) Integer offset,
                                               @Param(LIMIT) Integer limit);

    /**
     * Get the total number of packages containing matching the given filters
     *
     * @param siteId          the site id
     * @param target          the target
     * @param paths           the paths of the items
     * @param packageState    the mask to apply to filter the package state
     * @param approvalStates  the approval states to filter by
     * @param includeChildren whether to include the children of the paths in the search
     * @return the total number of packages matching the filters
     */
    int getItemPackagesCount(@Param(SITE_ID) String siteId,
                             @Param(TARGET) String target,
                             @Param(PATHS) Collection<String> paths,
                             @Param(PACKAGE_STATE) Long packageState,
                             @Param(APPROVAL_STATES) Collection<ApprovalState> approvalStates,
                             @Param(INCLUDE_CHILDREN) boolean includeChildren);

    /**
     * Get the publish packages in the history matching the given filters
     *
     * @param siteId       the site id
     * @param target       the publishing target
     * @param approver     the approver username. This corresponds to the reviewer, if it exists. Otherwise, it corresponds to the submitter (direct publish without workflow)
     * @param packageState the package state
     * @param dateFrom     the start date to filter packages
     * @param dateTo       the end date to filter packages
     * @param offset       the offset to start from
     * @param limit        the max number of items to return
     * @return the publish packages in the history matching the filters
     */
    Collection<DashboardPublishingPackage> getPublishPackageHistory(@Param(SITE_ID) String siteId,
                                                                    @Param(TARGET) String target,
                                                                    @Param(QueryParameterNames.APPROVER) String approver,
                                                                    @Param(PACKAGE_STATE) Long packageState,
                                                                    @Param(QueryParameterNames.DATE_FROM) Instant dateFrom,
                                                                    @Param(QueryParameterNames.DATE_TO) Instant dateTo,
                                                                    @Param(OFFSET) Integer offset,
                                                                    @Param(LIMIT) Integer limit);

    /**
     * Get the total number of publish packages in the history matching the given filters
     *
     * @param siteId       the site id
     * @param target       the publishing target
     * @param approver     the approver username. This corresponds to the reviewer, if it exists. Otherwise, it corresponds to the submitter (direct publish without workflow)
     * @param packageState the package state
     * @param dateFrom     the start date to filter packages
     * @param dateTo       the end date to filter packages
     * @return the total number of packages in the history matching the filters
     */
    int getPublishPackageHistoryTotal(@Param(SITE_ID) String siteId,
                                      @Param(TARGET) String target,
                                      @Param(QueryParameterNames.APPROVER) String approver,
                                      @Param(PACKAGE_STATE) long packageState,
                                      @Param(QueryParameterNames.DATE_FROM) Instant dateFrom,
                                      @Param(QueryParameterNames.DATE_TO) Instant dateTo);

    /**
     * Get the number of publishes for a site in the last n days
     *
     * @param siteId the site id
     * @param days   the number of days to look back
     * @return the number of publishes
     */
    int getNumberOfPublishes(@Param(SITE_ID) String siteId, @Param(DAYS) int days);

    /**
     * Get the number of published items for a site in the last n days
     *
     * @param siteId the site id
     * @param days   the number of days to look back
     * @param action the action to filter by
     * @return the number of published items
     */
    default int getNumberOfPublishedItemsByAction(String siteId, int days, PublishItem.Action action) {
        return getNumberOfPublishedItemsByActionInternal(siteId, days, action, COMPLETED.value);
    }

    /**
     * Get the number of published items for a site in the last n days
     *
     * @param siteId         the site id
     * @param days           the number of days to look back
     * @param action         the action to filter by
     * @param completedState the state to filter by
     * @return the number of published items
     */
    int getNumberOfPublishedItemsByActionInternal(@Param(SITE_ID) String siteId,
                                                  @Param(DAYS) int days,
                                                  @Param(ACTION) PublishItem.Action action,
                                                  @Param(COMPLETED_STATE) long completedState);
}
