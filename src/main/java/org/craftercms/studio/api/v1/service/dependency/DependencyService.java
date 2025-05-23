/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;

import java.util.Set;

/**
 * Dependency Service is the sole custodian of the dependency database
 * and provide the interface to add, sync, delete and retrieve dependencies
 * across the system.
 *
 * @author Sumer Jabri
 */
public interface DependencyService {

	/**
	 * Get item-specific dependencies of an item. An item-specific
	 * dependency is:
	 * * Only referenced by the parent item
	 * * Sits in a special path pattern defined during content modeling
	 *
	 * @param site  Site to operate on
	 * @param path  Path to items to retrieve deps for
	 * @param depth Depth of tree to traverse. Depth of -1 disables depth limits
	 * @return set of hard dependencies
	 * @throws SiteNotFoundException    Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException    Internal error, see exception details
	 */
	Set<String> getItemSpecificDependencies(String site, String path, int depth)
		throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Get all item dependencies of an item.
	 *
	 * @param site  Site to operate on
	 * @param path  Path to items to retrieve deps for
	 * @param depth Depth of tree to traverse. Depth of -1 disables depth limits
	 * @return set of items that given item depends on
	 * @throws SiteNotFoundException    Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException    Internal error, see exception details
	 */
	Set<String> getItemDependencies(String site, String path, int depth)
		throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;

	/**
	 * Get all items that depend on this item.
	 *
	 * @param site  Site to operate on
	 * @param path  Path to items to retrieve deps for
	 * @param depth Depth of tree to traverse. Depth of -1 disables depth limits
	 * @return set of items depending on given item path
	 * @throws SiteNotFoundException    Site doesn't exist
	 * @throws ContentNotFoundException Path doesn't exist
	 * @throws ServiceLayerException    Internal error, see exception details
	 */
	@Deprecated
	Set<String> getItemsDependingOn(String site, String path, int depth)
		throws SiteNotFoundException, ContentNotFoundException, ServiceLayerException;
}
