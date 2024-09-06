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

package org.craftercms.studio.api.v2.service.dependency;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.model.rest.content.DependencyItem;

import java.util.*;

public interface DependencyService {

    /**
     * Get a soft dependencies of a list of items. A soft
     * dependency is:
     * * an edited, shared (not item specific) dependency
     *
     * @param site  Site to operate on
     * @param paths List of paths to items to retrieve deps for
     * @return list of soft dependencies
     */
    Collection<String> getSoftDependencies(String site, Collection<String> paths);

    /**
     * Get then hard dependencies of an item. A hard
     * dependency is:
     * * Never-published item that this item depends on
     * * Item-specific dependency that has been modified but not published
     *
     * @param site             Site to operate on
     * @param publishingTarget Publishing target to get hard dependencies for
     * @param paths            List of paths to items to retrieve deps for
     * @return list of hard dependencies
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    Collection<String> getHardDependencies(String site, String publishingTarget, Collection<String> paths)
            throws ServiceLayerException;

    /**
     * Get the hard dependencies of an item. A hard
     * dependency is:
     * * Never-published item that this item depends on
     * * Item-specific dependency that has been modified but not published
     *
     * @param site  Site to operate on
     * @param paths List of paths to items to retrieve deps for
     * @return list of hard dependencies
     */
    Collection<String> getHardDependencies(String site, Collection<String> paths);

    /**
     * Get list of paths of content items that are dependant on given paths
     *
     * @param siteId site identifier
     * @param paths list of paths to get dependent items for
     * @return list of paths dependent on given paths
     */
    List<String> getDependentPaths(String siteId, List<String> paths) throws SiteNotFoundException;

    /**
     * Get all items that depend on this item
     * @param siteId site identifier
     * @param path path to get dependent items for
     * @return list of {@link DependencyItem} dependent on given path
     */
    List<DependencyItem> getDependentItems(String siteId, String path);

    /**
     * Get item specific dependencies for given path
     *
     * @param siteId site identifier
     * @param paths path to get item specific dependencies for
     * @return list of item specific dependencies
     */
    List<String> getItemSpecificDependencies(String siteId, List<String> paths);

    /**
     * Resolves dependent files for given content of given path
     *
     * @param site the site id
     * @param sourcePath the path to resolve dependencies for
     * @return Map of ResolvedDependency's of files that content is dependent on by type
     */
    Map<String, Set<DependencyResolver.ResolvedDependency>> resolveDependencies(String site, String sourcePath);

    /**
     * Scan item for direct dependencies and synchronize those to
     * the dependencies database adding the new deps, updating existing,
     * and removing what was removed from the item.
     *
     * @param site Site to operate on
     * @param path Path to item to scan
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ContentNotFoundException Path doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    void upsertDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

    /**
     * Delete the dependencies of sourcePath
     *
     * @param site       the site id
     * @param sourcePath the source path of the dependencies to delete
     */
    void deleteItemDependencies(String site, String sourcePath) throws ServiceLayerException;

    /**
     * Mark as invalid the dependency records with the given target path
     *
     * @param siteId     the site id
     * @param targetPath the target path of the dependencies to invalidate
     */
    void invalidateDependencies(String siteId, String targetPath) throws ServiceLayerException;

    /**
     * Mark as valid the dependency records with the given target path
     *
     * @param siteId     the site id
     * @param targetPath the target path of the dependencies to validate
     */
    void validateDependencies(String siteId, String targetPath) throws ServiceLayerException;
}
