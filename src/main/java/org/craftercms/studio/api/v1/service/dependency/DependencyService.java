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
package org.craftercms.studio.api.v1.service.dependency;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.to.CalculateDependenciesEntityTO;

/**
 * Dependency Service is the sole custodian of the dependency database
 * and provide the interface to add, sync, delete and retrieve dependencies
 * across the system.
 * 
 * @author Sumer Jabri
 */
public interface DependencyService {
	/**
	 * Scan item for direct dependencies and synchronize those to
	 * the depenencies database adding the new deps, updating existing,
	 * and removing what was removed from the item.
	 * 
	 * @param site Site to operate on
	 * @param path Path to item to scan
	 * @return set of upserted dependencies
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	Set<String> upsertDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Scan a list of items for direct dependencies and synchroniz
	 * those to the depenencies database adding the new deps, updating
	 * existing, and removing what was removed from the item.
	 * 
	 * @param site Site to operate on
	 * @param paths List of paths to items to scan
	 * @return set of upserted dependencies
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException One or more paths doesn't exist
	 *  (database won't be updated for any of the items)
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	Set<String> upsertDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Get a all publishing dependencies of a list of items. A publishing
	 * dependency is:
	 * * Never-published item that this item depends on
	 * * Item-specific dependency that has been modified but not published
	 * 
	 * @param site Site to operate on
	 * @param path Paths to item to retrieve deps for
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 * @return list of mandatory dependencies paths for publishing
	 */
	List<String> getPublishingDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Get a all publishing dependencies of a list of items. A publishing
	 * dependency is:
	 * * Never-published item that this item depends on
	 * * Item-specific dependency that has been modified but not published
	 * 
	 * @param site Site to operate on
	 * @param paths List of paths to items to retrieve deps for
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException One or more paths doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 * @return list of mandatory dependencies paths for publishing
	 */
	List<String> getPublishingDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Get item-specific dependencies of an item. An item-specific
	 * dependency is:
	 * * Only referenced by the parent item
	 * * Sits in a special path pattern defined during content modeling
	 * 
	 * @param site Site to operate on
	 * @param path Path to items to retrieve deps for
	 * @param depth Depth of tree to traverse. Depth of -1 disables depth limits
	 * @return set of hard dependencies
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	Set<String> getItemSpecificDependencies(String site, String path, int depth)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Get all item dependencies of an item.
	 * 
	 * @param site Site to operate on
	 * @param path Path to items to retrieve deps for
	 * @param depth Depth of tree to traverse. Depth of -1 disables depth limits
	 * @return set of items that given item depends on
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	Set<String> getItemDependencies(String site, String path, int depth)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Get all items that depend on this item.
	 * 
	 * @param site Site to operate on
	 * @param path Path to items to retrieve deps for
	 * @param depth Depth of tree to traverse. Depth of -1 disables depth limits
	 * @return set of items depending on given item path
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	Set<String> getItemsDependingOn(String site, String path, int depth)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Move an item and make sure dependency paths remain correct.
	 * 
	 * @param site Site to operate on
	 * @param oldPath Path to item to move
	 * @param newPath Path the item moves to
	 * @return set of updated dependencies
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	Set<String> moveDependencies(String site, String oldPath, String newPath)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Delete an item dependencies from the database.
	 * 
	 * @param site Site to operate on
	 * @param path Path to items to retrieve deps for
	 * @throws SiteNotFoundException Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	void deleteItemDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Delete all dependencies from the database for a given site.
	 * 
	 * @param site Site to operate on
	 * @throws ServiceLayerException Internal error, see exception details
	 */
	void deleteSiteDependencies(String site) throws ServiceLayerException;

    /**
     *
     * @param site Site to operate on
     * @param path Path to item to retrieve delete dependencies for
     * @return Set of paths included as delete dependencies
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ContentNotFoundException Content doesn't exist
     * @throws ServiceLayerException Internal error
     */
	Set<String> getDeleteDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

    /**
     *
     * @param site Site to operate on
     * @param paths Paths to items to retrieve delete dependencies for
     * @return Set of paths included as delete dependencies
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ContentNotFoundException Content doesn't exist
     * @throws ServiceLayerException Internal error
     */
	Set<String> getDeleteDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

    /**
     * Calculate dependencies for publishing
     *
     * @param site Site to operate on
     * @param paths List of items to calculate dependencies for
     * @return Formatted result set
     * @throws ServiceLayerException general service error
     */
	Map<String, List<CalculateDependenciesEntityTO>> calculateDependencies(String site, List<String> paths)
            throws ServiceLayerException;

    /**
     * Calculate dependencies paths for publishing
     * @param site site to use
     * @param paths list of items to calculate dependencies for
     * @return dependencies paths
	 *
	 * @throws ServiceLayerException general service error
     */
	Set<String> calculateDependenciesPaths(String site, List<String> paths) throws ServiceLayerException;
}
