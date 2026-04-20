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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;
import org.craftercms.studio.api.v2.dal.item.LightItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.List.copyOf;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.ListUtils.partition;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.MODIFIED_MASK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.NEW_MASK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;
import static org.craftercms.studio.api.v2.utils.DalUtils.MY_BATIS_QUERY_BATCH_SIZE;

/**
 * @author Dejan Brkic
 */
public interface DependencyDAO {

	/**
	 * Get soft dependencies from DB for list of content paths
	 * This query is recursive, so it will get soft deps of soft deps, filtering the
	 * non-new/non-edited items at the end.
	 *
	 * @param site                             site identifier
	 * @param paths                            list of content paths
	 * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
	 * @return List of soft dependencies
	 */
	default Collection<LightItem> getSoftDependenciesForList(String site, Set<String> paths,
															 List<String> itemSpecificDependenciesPatterns) {
		Map<String, LightItem> result = new HashMap<>();
		for (List<String> sublist : partition(copyOf(paths), MY_BATIS_QUERY_BATCH_SIZE)) {
			result.putAll(getSoftDependenciesForListInternal(site, sublist,
					itemSpecificDependenciesPatterns, ItemState.MODIFIED_MASK, ItemState.NEW_MASK).stream()
					.collect(toMap(LightItem::getPath, identity())));
		}
		return result.values();
	}

	/**
	 * Get soft dependencies from DB for list of content paths
	 * This query is recursive, so it will get soft deps of soft deps, filtering the
	 * non-new/non-edited items at the end.
	 *
	 * @param site                             site identifier
	 * @param paths                            list of content paths
	 * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
	 * @param modifiedMask                     state bit mask for modified item
	 * @param newMask                          state bit mask for new item
	 * @return List of soft dependencies
	 */
	List<LightItem> getSoftDependenciesForListInternal(@Param(SITE_ID) String site, @Param(PATHS) Collection<String> paths,
													   @Param(REGEX) List<String> itemSpecificDependenciesPatterns,
													   @Param(MODIFIED_MASK) long modifiedMask,
													   @Param(NEW_MASK) long newMask);

	/**
	 * Get publishing soft dependencies from DB for list of content paths
	 * This query is not recursive, so it will get only direct soft deps, including
	 * the new items only.
	 *
	 * @param site                             site identifier
	 * @param paths                            list of content paths
	 * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
	 * @return List of soft dependencies
	 */
	default Collection<LightItem> getPublishingSoftDependenciesForList(String site, Collection<String> paths,
																	   List<String> itemSpecificDependenciesPatterns,
																	   String target) {
		Map<String, LightItem> result = new HashMap<>();
		for (List<String> sublist : partition(copyOf(paths), MY_BATIS_QUERY_BATCH_SIZE)) {
			result.putAll(getPublishingSoftDependenciesForListInternal(site, new HashSet<>(sublist),
					itemSpecificDependenciesPatterns, ItemState.MODIFIED_MASK, ItemState.NEW_MASK, target).stream()
					.collect(toMap(LightItem::getPath, identity())));
		}

		return result.values();
	}

	/**
	 * Get publishing soft dependencies from DB for list of content paths
	 * This query is not recursive, so it will get only direct soft deps, including
	 * the new items only.
	 *
	 * @param site                             site identifier
	 * @param paths                            list of content paths
	 * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
	 * @param modifiedMask                     state bit mask for modified item
	 * @param newMask                          state bit mask for new item
	 * @return List of soft dependencies
	 */
	List<LightItem> getPublishingSoftDependenciesForListInternal(@Param(SITE_ID) String site, @Param(PATHS) Set<String> paths,
																 @Param(REGEX) List<String> itemSpecificDependenciesPatterns,
																 @Param(MODIFIED_MASK) long modifiedMask,
																 @Param(NEW_MASK) long newMask,
																 @Param(TARGET) String target);

	/**
	 * Get hard dependencies from DB for list of content paths
	 *
	 * @param site                             site identifier
	 * @param paths                            list of content paths
	 * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
	 * @param isLiveTarget                     true if publishing target is live, false if staging
	 * @return List of hard dependencies
	 */
	default Collection<LightItem> getHardDependenciesForList(final String site, final String target, final Collection<String> paths,
															 final List<String> itemSpecificDependenciesPatterns, boolean isLiveTarget) {
		long newMaskOff = isLiveTarget ? LIVE.value : STAGED.value;

		Map<String, LightItem> result = new HashMap<>();
		for (List<String> sublist : partition(copyOf(paths), MY_BATIS_QUERY_BATCH_SIZE)) {
			result.putAll(getHardDependenciesForListInternal(site, target, sublist, itemSpecificDependenciesPatterns,
					CONTENT_TYPE_FOLDER, NEW.value, newMaskOff, MODIFIED.value, isLiveTarget).stream()
					.collect(toMap(LightItem::getPath, identity())));
		}
		return result.values();
	}

	/**
	 * Get hard dependencies from DB for list of content paths
	 *
	 * @param site                             site identifier
	 * @param paths                            list of content paths
	 * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
	 * @param systemTypeFolder                 system type folder
	 * @param newInTargetMaskOn                state bit mask for new item in target (e.g: never published in staging)
	 *                                         items must contain the bits in this mask
	 * @param newInTargetMaskOff               state bit mask for new item in target (e.g: never published in live)
	 *                                         items must not contain the bits in this mask
	 * @param modifiedMask                     state bit mask for modified item
	 * @param isLiveTarget                     true if publishing target is live, false if staging
	 * @return List of hard dependencies
	 */
	List<LightItem> getHardDependenciesForListInternal(@Param(SITE_ID) String site, @Param(TARGET) String target,
													   @Param(PATHS) Collection<String> paths,
													   @Param(REGEX) List<String> itemSpecificDependenciesPatterns,
													   @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
													   @Param(NEW_IN_TARGET_MASK_ON) long newInTargetMaskOn,
													   @Param(NEW_IN_TARGET_MASK_OFF) long newInTargetMaskOff,
													   @Param(MODIFIED_MASK) long modifiedMask,
													   @Param(IS_LIVE_TARGET) boolean isLiveTarget);

	/**
	 * Get items depending on given paths
	 *
	 * @param siteId site identifier
	 * @param paths  list of content paths
	 * @return List of items depending on given paths
	 */
	default Collection<LightItem> getDependentItems(String siteId, List<String> paths) {
		Map<String, LightItem> result = new HashMap<>();
		for (List<String> sublist : partition(paths, MY_BATIS_QUERY_BATCH_SIZE)) {
			result.putAll(getDependentItemsInternal(siteId, sublist).stream()
					.collect(toMap(LightItem::getPath, identity())));
		}
		return result.values();
	}

	/**
	 * Get items depending on given paths
	 *
	 * @param siteId site identifier
	 * @param paths  list of content paths
	 * @return List of items depending on given paths
	 */
	List<LightItem> getDependentItemsInternal(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths);

	/**
	 * Get item specific dependencies for given paths
	 *
	 * @param siteId site identifier
	 * @param paths  list of content paths
	 * @param regex  list of patterns that define item specific dependencies
	 * @return list of item specific dependencies
	 */
	default List<LightItem> getItemSpecificDependencies(String siteId, Collection<String> paths,
														List<String> regex) {
		Map<String, LightItem> result = new HashMap<>();
		for (List<String> sublist : partition(copyOf(paths), MY_BATIS_QUERY_BATCH_SIZE)) {
			result.putAll(getItemSpecificDependenciesInternal(siteId, sublist, regex).stream()
					.collect(toMap(LightItem::getPath, identity())));
		}
		return new ArrayList<>(result.values());
	}

	/**
	 * Get item specific dependencies for given paths
	 *
	 * @param siteId site identifier
	 * @param paths  list of content paths
	 * @param regex  list of patterns that define item specific dependencies
	 * @return list of item specific dependencies
	 */
	List<LightItem> getItemSpecificDependenciesInternal(@Param(SITE_ID) String siteId, @Param(PATHS) Collection<String> paths,
														@Param(REGEX) List<String> regex);

	/**
	 * Get all valid dependency paths for given path.
	 *
	 * @param siteId the site id
	 * @param path   the content path to get dependencies for
	 * @return a collection of
	 */
	Collection<String> getDependencyPaths(@Param(SITE_ID) String siteId, @Param(PATH) String path);

	/**
	 * Get all valid dependencies for given path.
	 *
	 * @param siteId the site id
	 * @param path   the content path to get dependencies for
	 * @return a collection of {@link LightItem} representing the valid dependencies
	 */
	Collection<LightItem> getDependencies(@Param(SITE_ID) String siteId, @Param(PATH) String path);

	/**
	 * Delete the dependencies of sourcePath
	 *
	 * @param site       the site id
	 * @param sourcePath the source path of the dependencies to delete
	 */
	void deleteItemDependencies(@Param(SITE_ID) String site, @Param(PATH) String sourcePath);

	/**
	 * Insert a list of dependency records
	 *
	 * @param dependencies the list of dependencies to insert
	 */
	default void insertItemDependencies(List<Dependency> dependencies) {
		for (List<Dependency> sublist : partition(dependencies, MY_BATIS_QUERY_BATCH_SIZE)) {
			insertItemDependenciesInternal(sublist);
		}
	}

	/**
	 * Insert a list of dependency records
	 *
	 * @param dependencies the list of dependencies to insert
	 */
	void insertItemDependenciesInternal(@Param(DEPENDENCIES) List<Dependency> dependencies);

	/**
	 * Mark as invalid the dependency records with the given target path
	 *
	 * @param siteId     the site id
	 * @param targetPath the target path of the dependencies to invalidate
	 */
	void invalidateDependencies(@Param(SITE_ID) String siteId, @Param(PATH) String targetPath);

	/**
	 * Mark as valid the dependency records with the given target path
	 *
	 * @param siteId     the site id
	 * @param targetPath the target path of the dependencies to validate
	 */
	void validateDependencies(@Param(SITE_ID) String siteId, @Param(PATH) String targetPath);

	/**
	 * Mark as valid/invalid all site dependencies depending on the existence of the target_path in the site
	 *
	 * @param siteId the site id
	 */
	void validateDependenciesForSite(@Param(SITE_ID) String siteId);

	/**
	 * Update the dependencies to reflect the move operation of a subtree
	 *
	 * @param siteId the site id
	 * @param path   the removed content path
	 */
	@Transactional
	default void updateDependenciesOnTreeDelete(final String siteId, final String path) {
		deleteTreeDependencies(siteId, path);
		invalidateTreeDependencies(siteId, path);
	}

	/**
	 * Delete the dependencies entries of any item where the source is a child of the path
	 *
	 * @param siteId the site id
	 * @param path   the content path
	 */
	void deleteTreeDependencies(@Param(SITE_ID) String siteId, @Param(PATH) String path);

	/**
	 * Invalidate the dependencies entries of any item where the target is a child of the path
	 *
	 * @param siteId the site id
	 * @param path   the removed content path
	 */
	void invalidateTreeDependencies(@Param(SITE_ID) String siteId, @Param(PATH) String path);

	/**
	 * Validate any dependencies where the target exists in the site
	 * and is a child of the content subtree path
	 *
	 * @param siteId the site id
	 * @param path   the content subtree path
	 */
	void validateDependenciesForTree(@Param(SITE_ID) String siteId, @Param(PATH) String path);
}

