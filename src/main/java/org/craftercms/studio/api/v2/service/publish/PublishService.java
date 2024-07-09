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

package org.craftercms.studio.api.v2.service.publish;

import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryGroup;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.PublishingPackageNotFoundException;
import org.craftercms.studio.model.publish.PublishingTarget;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
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
     *
     * @throws SiteNotFoundException site not found
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
     *
     * @return list of publishing packages
     *
     * @throws SiteNotFoundException site not found
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
     *
     * @throws SiteNotFoundException site not found
     */
    PublishingPackageDetails getPublishingPackageDetails(String siteId, String packageId) throws SiteNotFoundException, PublishingPackageNotFoundException;

    /**
     * Cancel publishing packages
     *
     * @param siteId site identifier
     * @param packageIds list of package identifiers
     *
     * @throws SiteNotFoundException site not found
     */
    void cancelPublishingPackages(String siteId, List<String> packageIds)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Get deployment history
     * @param siteId site identifier
     * @param daysFromToday number of days for history
     * @param numberOfItems number of items to display
     * @param filterType filter results by filter type
     * @return
     */
    List<DeploymentHistoryGroup> getDeploymentHistory(String siteId, int daysFromToday, int numberOfItems,
                                                      String filterType) throws ServiceLayerException, UserNotFoundException;

    /**
     * Get available publishing targets for given site
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

    int getPublishingItemsScheduledTotal(String siteId, String publishingTarget, String approver, ZonedDateTime dateFrom,
                                         ZonedDateTime dateTo, List<String> systemTypes);

    int getPublishingPackagesHistoryTotal(String siteId, String publishingTarget, String approver, ZonedDateTime dateFrom,
                                          ZonedDateTime dateTo);

    int getPublishingHistoryDetailTotalItems(String siteId, String publishingPackageId);

    List<DashboardPublishingPackage> getPublishingPackagesHistory(String siteId, String publishingTarget, String approver,
                                                                  ZonedDateTime dateFrom, ZonedDateTime dateTo, int offset, int limit);

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
     * @param siteId the site id
     * @param path the path of the item
     * @return the package containing the item, or null if the item is not submitted to be published
     */
    PublishPackage getPackageForItem(String siteId, String path);

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
