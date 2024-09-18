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

package org.craftercms.studio.api.v2.service.publish;

import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState;
import org.craftercms.studio.api.v2.exception.publish.PublishPackageNotFoundException;
import org.craftercms.studio.impl.v2.publish.Publisher;
import org.craftercms.studio.model.publish.PublishingTarget;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.craftercms.studio.model.rest.publish.PublishPackageDetails;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Service for publishing submissions
 * This service is responsible for creating publishing packages,
 * calculating dependencies and retrieving information packages and the publishing queue.
 * For the actual publishing queue processing, see {@link Publisher}
 */
public interface PublishService {

    /**
     * Get total number of publishing packages for given search parameters
     *
     * @param siteId         site identifier
     * @param target    publishing target
     * @param path           regular expression for paths
     * @param states         publishing package states bits
     * @param approvalStates approval states to filter packages
     * @return total number of publishing packages
     * @throws SiteNotFoundException site not found
     */
    int getPublishingPackagesCount(String siteId, String target, String path, Long states, final Collection<ApprovalState> approvalStates)
            throws SiteNotFoundException;

    /**
     * Get publishing packages for given search parameters
     *
     * @param siteId         site identifier
     * @param target         publishing target
     * @param path           regular expression for paths
     * @param states         publishing package state bits
     * @param approvalStates approval states to filter packages
     * @param offset         offset for pagination
     * @param limit          limit for pagination
     * @return list of publishing packages
     * @throws SiteNotFoundException site not found
     */
    Collection<PublishPackage> getPublishingPackages(String siteId, String target, String path, Long states,
                                                             final Collection<ApprovalState> approvalStates,
                                                             int offset, int limit) throws SiteNotFoundException;

    /**
     * Get publishing package details
     *
     * @param siteId    site identifier
     * @param packageId package identifier
     * @return publishing package details
     * @throws SiteNotFoundException site not found
     */
    PublishPackageDetails getPublishingPackageDetails(String siteId, long packageId) throws SiteNotFoundException, PublishPackageNotFoundException;

    /**
     * Get available publishing targets for given site
     *
     * @param siteId site identifier
     * @return list of available publishing targets
     * @throws SiteNotFoundException Site doesn't exist
     */
    List<PublishingTarget> getAvailablePublishingTargets(String siteId) throws SiteNotFoundException;

    /**
     * Check if site has ever been published.
     *
     * @param siteId site identifier
     * @return true if site has been published at least once, otherwise false
     * @throws SiteNotFoundException Site doesn't exist
     */
    boolean isSitePublished(String siteId) throws SiteNotFoundException;

    /**
     * Create a 'APPROVED' publishing package. The created package will be ready to be published.
     *
     * @param siteId           the id of the site
     * @param publishingTarget the publishing target
     * @param paths            the paths to publish
     * @param commitIds        the commit ids to publish
     * @param schedule         the scheduled date for the publishing (null to publish immediately)
     * @param comment          the comment for the publishing
     * @param publishAll       if this is a publish-all request
     * @return the id of the created package
     */
    long publish(String siteId, String publishingTarget, List<PublishRequestPath> paths,
                 List<String> commitIds, Instant schedule, String comment, boolean publishAll)
            throws ServiceLayerException, AuthenticationException;

    /**
     * Create a 'SUBMITTED' publishing package. The created package will require approval.
     *
     * @param siteId           the id of the site
     * @param publishingTarget the publishing target
     * @param paths            the paths to publish
     * @param commitIds        the commit ids to publish
     * @param schedule         the scheduled date for the publishing (null to publish immediately)
     * @param comment          the comment for the publishing
     * @param publishAll       if this is a publish-all request
     * @return the id of the created package
     */
    long requestPublish(String siteId, String publishingTarget, List<PublishRequestPath> paths,
                        List<String> commitIds, Instant schedule, String comment, boolean publishAll)
            throws AuthenticationException, ServiceLayerException;

    int getPublishingItemsScheduledCount(String siteId, String publishingTarget, String approver, ZonedDateTime dateFrom,
                                         ZonedDateTime dateTo, List<String> systemTypes);

    /**
     * Get the total number of publishing packages in the history matching the given parameters
     *
     * @param siteId   the site id
     * @param target   the publishing target
     * @param approver the approver username
     * @param dateFrom to filter packages published after this date
     * @param dateTo   to filter packages published before this date
     * @return the number of packages matching the given parameters
     */
    int getPublishingHistoryCount(String siteId, String target, String approver, Instant dateFrom,
                                  Instant dateTo);

    /**
     * Get the publishing packages in the history matching the given parameters
     *
     * @param siteId   the site id
     * @param target   the publishing target
     * @param approver the approver username
     * @param dateFrom to filter packages published after this date
     * @param dateTo   to filter packages published before this date
     * @param offset   the offset to start from
     * @param limit    the max number of packages to return
     * @return the packages matching the given parameters
     */
    Collection<DashboardPublishingPackage> getPublishingHistory(String siteId, String target, String approver,
                                                                Instant dateFrom, Instant dateTo, int offset, int limit);

    /**
     * Get publishing package details total item count
     *
     * @param siteId    site identifier
     * @param packageId publishing package identifier
     * @return number of items in the package
     */
    int getPublishItemsCount(String siteId, long packageId);

    /**
     * Get the number of publishes for the given site in the last days
     * @param siteId the site id
     * @param days the number of days to look back
     * @return the number of publishes
     */
    int getNumberOfPublishes(String siteId, int days);

    /**
     * Get the dependencies for the given paths and commit ids
     *
     * @param siteId           site identifier
     * @param publishingTarget the publishing target
     * @param paths            paths to get dependencies for
     * @param commitIds        commit ids to get dependencies for
     * @return a package containing:
     * <ul>
     *     <li>items: the items to publish</li>
     *     <li>deletedItems: the deleted paths found in the requested commits</li>
     *     <li>hardDependencies: the hard dependencies of the items</li>
     *     <li>softDependencies: the soft dependencies of the items</li>
     *     </ul>
     * @throws ServiceLayerException
     * @throws IOException
     */
    PublishDependenciesResult getPublishDependencies(String siteId, String publishingTarget, Collection<PublishRequestPath> paths, Collection<String> commitIds) throws ServiceLayerException, IOException;

    /**
     * Get the submitted package containing the given item
     *
     * @param siteId          the site id
     * @param path            the path of the item
     * @param includeChildren whether to include the children of the paths in the search
     * @return the package containing the item, or null if the item is not submitted to be published
     */
    PublishPackage getReadyPackageForItem(String siteId, String path, boolean includeChildren);

    /**
     * Get the READY or PROCESSING publish packages containing the given items
     *
     * @param siteId          the site id
     * @param paths           the paths of the items
     * @param includeChildren whether to include the children of the paths in the search
     * @return the READY or PROCESSING packages containing the items
     */
    Collection<PublishPackage> getActivePackagesForItems(String siteId, Collection<String> paths, boolean includeChildren);

    /**
     * Publish the deletion of the given paths.
     *
     * @param siteId             the site id
     * @param userRequestedPaths the paths to delete as requested by the user
     * @param dependencies       the delete dependencies of the requested paths
     * @param comment            user user comment
     */
    long publishDelete(String siteId, Collection<String> userRequestedPaths, Collection<String> dependencies, String comment) throws ServiceLayerException;

    /**
     * Get a publishing package by site and package id
     *
     * @param siteId    the site id
     * @param packageId the package id
     * @return the publishing package
     * @throws PublishPackageNotFoundException if the package is not found
     * @throws SiteNotFoundException           if the site is not found
     */
    PublishPackage getPackage(String siteId, long packageId) throws PublishPackageNotFoundException, SiteNotFoundException;

    /**
     * Get the publish items for a package
     *
     * @param siteId    the site id
     * @param packageId the package id
     * @param offset    the offset to start from
     * @param limit     the max number of items to return
     * @return the publish items
     */
    Collection<PublishItem> getPublishItems(String siteId, long packageId, int offset, int limit) throws PublishPackageNotFoundException, SiteNotFoundException;

    /**
     * Get the total number of published items in the last <code>days</code>number of days matching the action
     *
     * @param siteId the site id
     * @param days   the number of days to look back
     * @param action the action to filter publish items by
     * @return the number of published items matching the filters
     */
    int getNumberOfPublishedItemsByAction(String siteId, int days, PublishItem.Action action);

    /**
     * A request to include a path in a publish request.
     *
     * @param path            the path to include
     * @param includeChildren whether to include the children of the path
     * @param includeSoftDeps whether to include the soft dependencies of the path (and children's soft-deps when including children)
     */
    record PublishRequestPath(@ValidExistingContentPath String path, boolean includeChildren, boolean includeSoftDeps) {
    }

    /**
     * Result of a get-dependencies request
     *
     * @param items            the items to publish. Includes paths selected by the user.
     *                         i.e.: each path with children (if requested) and their soft dependencies (if requested).
     *                         paths extracted from commit ids
     * @param deletedItems     the deleted paths found in the requested commits
     * @param hardDependencies the hard dependencies of the items
     * @param softDependencies the soft dependencies of the items
     */
    record PublishDependenciesResult(Collection<String> items, Collection<String> deletedItems,
                                     Collection<String> hardDependencies, Collection<String> softDependencies) {
    }
}
