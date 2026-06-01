/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v2.dal.item.LightItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DependencyService {

	/**
	 * Get a soft dependencies of a list of items. A soft
	 * dependency is:
	 * * an edited, shared (not item specific) dependency
	 * <p>
	 * This method will also get transitive soft dependencies, even across published items.
	 * e.g.: if A depends on B and B depends on C, then A depends on C, even if B is published.
	 *
	 * @param site  Site to operate on
	 * @param paths List of paths to items to retrieve deps for
	 * @return list of soft dependencies
	 */
	Collection<LightItem> getSoftDependencies(String site, Set<String> paths);

	/**
	 * Get the publishing soft dependencies of a list of items. A soft
	 * dependency is:
	 * * an edited, shared (not item specific) dependency
	 * <p>
	 * This method will NOT get transitive soft dependencies.
	 *
	 * @param site  Site to operate on
	 * @param paths List of paths to items to retrieve deps for
	 * @return list of soft dependencies
	 */
	Collection<LightItem> getPublishingSoftDependencies(String site, Set<String> paths, String target);

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
	Collection<LightItem> getHardDependencies(String site, String publishingTarget, Collection<String> paths)
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
	Collection<LightItem> getHardDependencies(String site, Collection<String> paths) throws SiteNotFoundException;

	/**
	 * Get list of paths of content items that are dependant on given paths
	 *
	 * @param siteId site identifier
	 * @param paths  list of paths to get dependent items for
	 * @return list of paths dependent on given paths
	 */
	Collection<LightItem> getDependentPaths(String siteId, List<String> paths) throws SiteNotFoundException;

	/**
	 * Get all items that depend on this item
	 *
	 * @param siteId site identifier
	 * @param path   path to get dependent items for
	 * @return list of {@link LightItem} dependent on given path
	 * @throws ContentNotFoundException if the item at the given path does not exist
	 */
	Collection<LightItem> getDependentItems(String siteId, String path) throws ContentNotFoundException;

	/**
	 * Get item specific dependencies for given path
	 *
	 * @param siteId site identifier
	 * @param paths  path to get item specific dependencies for
	 * @return list of item specific dependencies
	 */
	List<LightItem> getItemSpecificDependencies(String siteId, Collection<String> paths);

	/**
	 * Get all valid dependency paths for the given path.
	 *
	 * @param siteId the site id
	 * @param path   source path to get dependencies for
	 * @return collection of paths of the dependencies for given path
	 */
	Collection<String> getDependencyPaths(String siteId, String path);

	/**
	 * Get dependency items for given path
	 *
	 * @param siteId the site id
	 * @param path   source path to get dependencies for
	 * @return collection of {@link LightItem} that are dependencies for given path
	 */
	Collection<LightItem> getDependencies(String siteId, String path);

	/**
	 * Resolves dependent files for given content of given path
	 *
	 * @param site       the site id
	 * @param sourcePath the path to resolve dependencies for
	 * @return Map of ResolvedDependency's of files that content is dependent on by type
	 */
	Map<String, Set<DependencyResolver.ResolvedDependency>> resolveDependencies(String site, String sourcePath) throws SiteNotFoundException;

	/**
	 * Scan item for direct dependencies and synchronize those to
	 * the dependencies database adding the new deps, updating existing,
	 * and removing what was removed from the item.
	 *
	 * @param site Site to operate on
	 * @param path Path to item to scan
	 * @throws SiteNotFoundException    Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException    Internal error, see exception details
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

	/**
	 * Indicate if the given path is a valid dependency source. e.g.: templates, pages, components
	 * Some files cannot have dependencies, like images or txt files
	 *
	 * @param siteId the site id
	 * @param path   the path to check
	 * @return true if the path is a valid dependency source, false otherwise
	 */
	boolean isValidDependencySource(String siteId, String path) throws SiteNotFoundException;

	/**
	 * Update the dependencies to reflect the removal of a content subtree.
	 * This will invalidate any dependencies where the target is a child of the path,
	 * and will delete any dependencies where the source is a child of the path.
	 *
	 * @param siteId the site id
	 * @param path   the removed content path
	 */
	void updateDependenciesOnTreeDelete(String siteId, String path);

	/**
	 * Validate any dependencies where the target exists and it is a child
	 * of the content subtree path
	 *
	 * @param siteId the site id
	 * @param path   the content subtree path
	 */
	void validateDependenciesForTree(String siteId, String path);
}
