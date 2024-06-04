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
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.APPROVED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.SUBMITTED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.READY;

/**
 * Provide access to DB publish related tables
 */
public interface PublishDAO {
    String SITE_ID = "siteId";
    String PACKAGE_ID = "packageId";
    String PUBLISH_PACKAGE = "publishPackage";
    String ITEMS_PARAM = "items";
    String APPROVAL_STATES = "approvalStates";
    String PACKAGE_STATES = "packageStates";
    String PACKAGE_STATE = "packageState";
    String ITEM_IDS = "itemIds";
    String CANCELLED_STATE = "cancelledState";
    String SITE_STATES = "siteStates";
    String ERROR = "error";

    /**
     * Convenience transactional method to create a package and its items
     *
     * @param publishPackage the package
     * @param publishItems   the items
     */
    @Transactional
    default void insertPackageAndItems(final PublishPackage publishPackage, final Collection<PublishItem> publishItems) {
        insertPackage(publishPackage);
        if (!isEmpty(publishItems)) {
            insertItems(publishPackage.getId(), publishItems);
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
    void insertPackage(PublishPackage publishPackage);

    /**
     * Insert items into a publish package
     *
     * @param packageId    the package id
     * @param publishItems the items to insert
     */
    void insertItems(@Param(PACKAGE_ID) long packageId, @Param(ITEMS_PARAM) Collection<PublishItem> publishItems);

    /**
     * Get the next publish packages to process for every site matching the given states
     *
     * @return the next publish packages to process
     */
    default List<PublishPackage> getNextPublishPackages() {
        return getNextPublishPackages(List.of(APPROVED), List.of(READY), List.of(Site.State.READY));
    }

    /**
     * Get the next publish packages to process for every site matching the given states
     *
     * @param approvalStates the package approval states to match
     * @param packageStates  the package states to match
     * @param siteStates     the site states to match
     * @return the next publish packages to process
     */
    List<PublishPackage> getNextPublishPackages(@Param(APPROVAL_STATES) List<ApprovalState> approvalStates,
                                                @Param(PACKAGE_STATES) List<PackageState> packageStates,
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
     *                       for the updatable fields
     */
    void updatePackage(@Param(PUBLISH_PACKAGE) final PublishPackage publishPackage);

    /**
     * Update state and error of a failed package
     *
     * @param packageId the package id
     * @param failureState     the new state
     * @param error     the error message
     */
    void updateFailedPackage(@Param(PACKAGE_ID) final long packageId,
                             @Param(PACKAGE_STATE) final PackageState failureState,
                             @Param(ERROR) final String error);

    /**
     * Cancel all active (ready non-rejected) packages for a site
     *
     * @param siteId the site id
     */
    default void cancelOutstandingPackages(final String siteId) {
        cancelOutstandingPackages(siteId, PackageState.CANCELLED, List.of(READY), List.of(SUBMITTED, APPROVED));
    }


    /**
     * Cancel all packages matching the approval and processing states.
     * Set packages' state to cancelledState parameter value
     *
     * @param siteId         the site id
     * @param cancelledState the state to set the packages to
     * @param statesToCancel the package states to match
     * @param approvalStates the approval states to match
     */
    void cancelOutstandingPackages(@Param(SITE_ID) String siteId,
                                   @Param(CANCELLED_STATE) PackageState cancelledState,
                                   @Param(PACKAGE_STATES) Collection<PackageState> statesToCancel,
                                   @Param(APPROVAL_STATES) Collection<ApprovalState> approvalStates);

    /**
     * Update the state of a package
     *
     * @param packageId    the package id
     * @param packageState the new state
     */
    void updatePackageState(@Param(PACKAGE_ID) long packageId, @Param(PACKAGE_STATE) PackageState packageState);

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
     * @param id    the package id
     * @param state the new state to set
     */
    void updatePublishItemState(@Param(PACKAGE_ID) long id, @Param(PACKAGE_STATE) PublishItem.State state);

    /**
     * Update the state for the publish items matching the given ids
     *
     * @param itemIds the publish item ids to match
     * @param state   the new state to set
     */
    void updatePublishItemListState(@Param(ITEM_IDS) List<Long> itemIds, @Param(PACKAGE_STATE) PublishItem.State state);
}
