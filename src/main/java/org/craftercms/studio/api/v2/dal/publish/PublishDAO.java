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
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.PublishState.PENDING;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.APPROVED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.SUBMITTED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.READY;

/**
 * Provide access to DB publish related tables
 */
public interface PublishDAO {
    String SITE_ID = "siteId";
    String TARGET = "target";
    String PACKAGE_ID = "packageId";
    String PUBLISH_PACKAGE = "publishPackage";
    String PACKAGE_READY_STATE = "readyState";
    String ITEMS = "items";
    String APPROVAL_STATES = "approvalStates";
    String PACKAGE_STATES = "packageStates";
    String PACKAGE_STATE = "packageState";
    String ITEM_STATE = "itemState";
    String CANCELLED_STATE = "cancelledState";
    String SITE_STATES = "siteStates";
    String ERROR = "error";
    String LIVE_ERROR = "liveError";
    String STAGING_ERROR = "stagingError";
    String ITEM_SUCCESS_STATE = "itemSuccessState";
    String ITEM_PUBLISHED_STATE = "publishState";

    /**
     * Convenience transactional method to create a package and its items
     *
     * @param publishPackage the package
     * @param publishItems   the items
     */
    @Transactional
    default void insertPackageAndItems(final PublishPackage publishPackage, final Collection<PublishItem> publishItems) {
        insertPackage(publishPackage, READY.value);
        if (!isEmpty(publishItems)) {
            insertItems(publishPackage.getId(), publishItems, PENDING.value);
            insertItemPublishItems(publishItems);
        }
    }

    /**
     * Insert item_publish_item records for the given publish items.
     * Notice this will insert a record to item_publish_item table for each non-delete publishItem
     *
     * @param publishItems the publish items
     */
    void insertItemPublishItems(Collection<PublishItem> publishItems);

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
     * @param packageState  the package state to match
     * @param siteStates     the site states to match
     * @return the next publish packages to process
     */
    List<PublishPackage> getNextPublishPackages(@Param(APPROVAL_STATES) List<ApprovalState> approvalStates,
                                                @Param(PACKAGE_STATE) long packageState,
                                                @Param(SITE_STATES) List<String> siteStates);

    /**
     * Get a package by id
     *
     * @param packageId the package id
     * @return the {@link PublishPackage}
     */
    PublishPackage getById(@Param(PACKAGE_ID) final long packageId);

    /**
     * Update a package
     *
     * @param publishPackage the package to update, containing the new values
     *                       for the updatable fields:
     *                       <ul>
     *                           <li>approval_state</li>
     *                           <li>package_state</li>
     *                           <li>live_error</li>
     *                           <li>staging_error</li>
     *                           <li>reviewed_on</li>
     *                           <li>published_on</li>
     *                           <li>published_staging_commit_id</li>
     *                           <li>published_live_commit_id</li>
     *                       </ul>
     */
    void updatePackage(@Param(PUBLISH_PACKAGE) final PublishPackage publishPackage);

    /**
     * Update state and error of a failed package
     *
     * @param packageId    the package id
     * @param failureState the new state
     * @param stagingError the staging error code (0 if none)
     * @param liveError    the live error code (0 if none)
     */
    void updateFailedPackage(@Param(PACKAGE_ID) final long packageId,
                             @Param(PACKAGE_STATE) final long failureState,
                             @Param(STAGING_ERROR) final int stagingError,
                             @Param(LIVE_ERROR) final int liveError);

    /**
     * Cancel all active (ready non-rejected) packages for a site and a target
     *
     * @param siteId the site id
     * @param target the target
     */
    default void cancelOutstandingPackages(final String siteId, final String target) {
        cancelOutstandingPackages(siteId, target, PackageState.CANCELLED, READY, List.of(SUBMITTED, APPROVED));
    }

    /**
     * Cancel all active (ready non-rejected) packages for a site
     *
     * @param siteId the site id
     */
    default void cancelAllOutstandingPackages(final String siteId) {
        cancelOutstandingPackages(siteId, null, PackageState.CANCELLED, READY, List.of(SUBMITTED, APPROVED));
    }


    /**
     * Cancel all packages matching the approval and processing states.
     * Set packages' state to cancelledState parameter value
     *
     * @param siteId         the site id
     * @param target         the target to cancel packages for, null for any target
     * @param cancelledState the state to set the packages to
     * @param stateToCancel  the package state to match
     * @param approvalStates the approval states to match
     */
    void cancelOutstandingPackages(@Param(SITE_ID) String siteId,
                                   @Param(TARGET) String target,
                                   @Param(CANCELLED_STATE) PackageState cancelledState,
                                   @Param(PACKAGE_STATE) PackageState stateToCancel,
                                   @Param(APPROVAL_STATES) Collection<ApprovalState> approvalStates);

    /**
     * Update the state of a package
     *
     * @param packageId    the package id
     * @param packageState the new state
     */
    void updatePackageState(@Param(PACKAGE_ID) long packageId, @Param(PACKAGE_STATE) long packageState);

    /**
     * Get the publish items for the given package
     *
     * @param packageId the package id
     * @return PublishItem records for the package
     */
    Collection<PublishItem> getPublishItems(@Param(PACKAGE_ID) long packageId);

    /**
     * Update the state for all publish items in the package
     *
     * @param id        the package id
     * @param itemState the new state to set
     */
    void updatePublishItemState(@Param(PACKAGE_ID) long id, @Param(ITEM_STATE) long itemState);

    /**
     * Update the state and error (if any) for the given publish items
     *
     * @param items the publish item to update state and error columns for
     */
    void updatePublishItemListState(@Param(ITEMS) Collection<PublishItem> items);
}
