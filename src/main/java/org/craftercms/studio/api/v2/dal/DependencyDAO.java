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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.MODIFIED_MASK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.NEW_MASK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;

/**
 * @author Dejan Brkic
 */
public interface DependencyDAO {

    String SOURCE_PATH_COLUMN_NAME = "source_path";
    String TARGET_PATH_COLUMN_NAME = "target_path";

    /**
     * Get soft dependencies from DB for list of content paths
     *
     * @param site                             site identifier
     * @param paths                            list of content paths
     * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
     * @param modifiedMask                     state bit mask for modified item
     * @param newMask                          state bit mask for new item
     * @return List of soft dependencies
     */
    List<Map<String, String>> getSoftDependenciesForList(@Param(SITE_ID) String site, @Param(PATHS) Set<String> paths,
                                                         @Param(REGEX) List<String> itemSpecificDependenciesPatterns,
                                                         @Param(MODIFIED_MASK) long modifiedMask,
                                                         @Param(NEW_MASK) long newMask);

    /**
     * Get hard dependencies from DB for list of content paths
     *
     * @param site                             site identifier
     * @param paths                            list of content paths
     * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
     * @param isLiveTarget true if publishing target is live, false if staging
     * @return List of hard dependencies
     */
    default List<String> getHardDependenciesForList(final String site, final String target, final Collection<String> paths,
                                                    final List<String> itemSpecificDependenciesPatterns, boolean isLiveTarget) {
        long newMaskOn = NEW.value;
        long newMaskOff = isLiveTarget ? LIVE.value : STAGED.value;
        return getHardDependenciesForList(site, target, paths, itemSpecificDependenciesPatterns,
                CONTENT_TYPE_FOLDER, newMaskOn, newMaskOff, isLiveTarget);
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
     * @param isLiveTarget                     true if publishing target is live, false if staging
     * @return List of hard dependencies
     */
    List<String> getHardDependenciesForList(@Param(SITE_ID) String site, @Param(TARGET) String target,
                                            @Param(PATHS) Collection<String> paths,
                                            @Param(REGEX) List<String> itemSpecificDependenciesPatterns,
                                            @Param(SYSTEM_TYPE_FOLDER) String systemTypeFolder,
                                            @Param(NEW_IN_TARGET_MASK_ON) long newInTargetMaskOn,
                                            @Param(NEW_IN_TARGET_MASK_OFF) long newInTargetMaskOff,
                                            @Param(IS_LIVE_TARGET) boolean isLiveTarget);

    /**
     * Get items depending on given paths
     *
     * @param siteId site identifier
     * @param paths  list of content paths
     * @return List of items depending on given paths
     */
    List<String> getDependentItems(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths);

    /**
     * Get item specific dependencies for given paths
     *
     * @param siteId site identifier
     * @param paths  list of content paths
     * @param regex  list of patterns that define item specific dependencies
     * @return list of item specific dependencies
     */
    List<String> getItemSpecificDependencies(@Param(SITE_ID) String siteId, @Param(PATHS) List<String> paths,
                                             @Param(REGEX) List<String> regex);

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
    void insertItemDependencies(@Param(DEPENDENCIES) List<Dependency> dependencies);

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
     * Mark as valid all site dependencies if the target_path exists in the site
     *
     * @param siteId the site id
     */
    void validateDependenciesForSite(@Param(SITE_ID) String siteId);
}
