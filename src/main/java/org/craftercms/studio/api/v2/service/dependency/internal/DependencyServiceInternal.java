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

package org.craftercms.studio.api.v2.service.dependency.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.Dependency;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DependencyServiceInternal {

    /**
     * Get a soft dependencies of a item. A soft
     * dependency is:
     * * an edited, shared (not item specific) dependency
     *
     * @param site Site to operate on
     * @param path Paths to item to retrieve deps for
     *
     * @return list of soft dependencies
     *
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    List<String> getSoftDependencies(String site, String path)
            throws SiteNotFoundException, ServiceLayerException;

    /**
     * Get a soft dependencies of a list of items. A soft
     * dependency is:
     * * an edited, shared (not item specific) dependency
     *
     * @param site Site to operate on
     * @param paths List of paths to items to retrieve deps for
     *
     * @return list of soft dependencies
     *
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    List<String> getSoftDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ServiceLayerException;

    /**
     * Get a hard dependencies of a item. A hard
     * dependency is:
     * * Never-published item that this item depends on
     * * Item-specific dependency that has been modified but not published
     *
     * @param site Site to operate on
     * @param path Paths to item to retrieve deps for
     *
     * @return list of hard dependencies
     *
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    List<String> getHardDependencies(String site, String path)
            throws SiteNotFoundException, ServiceLayerException;

    /**
     * Get a hard dependencies of a item. A hard
     * dependency is:
     * * Never-published item that this item depends on
     * * Item-specific dependency that has been modified but not published
     *
     * @param site Site to operate on
     * @param paths List of paths to items to retrieve deps for
     *
     * @return list of hard dependencies
     *
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    List<String> getHardDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ServiceLayerException;

    /**
     * Get list of paths of content items that are dependant on given paths
     *
     * @param siteId site identifier
     * @param paths list of paths to get dependent items for
     * @return list of paths dependent on given paths
     */
    List<String> getDependentItems(String siteId, List<String> paths);

    /**
     * Get item specific dependencies for given path
     *
     * @param siteId site identifier
     * @param path path to get item specific dependencies for
     * @return list of item specific dependencies
     */
    List<String> getItemSpecificDependencies(String siteId, String path);

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
     * @param site
     * @param path
     * @return set of paths of files that content is dependant on
     */
    Map<String, Set<String>> resolveDependnecies(String site, String path);

    /**
     * Get dependencies for content path by type
     * @param siteId site identifier
     * @param path content path
     * @param dependencyType dependency type
     * @return list of dependencies
     */
    List<Dependency> getDependenciesByType(String siteId, String path, String dependencyType);
}
