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

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param modifiedMask                     state bit map for modified item
     * @param newMask                          state bit map for new item
     * @return List of hard dependencies
     */
    List<Map<String, String>> getHardDependenciesForList(@Param("site") String site, @Param("paths") Set<String> paths,
                                                         @Param("regex") List<String> itemSpecificDependenciesPatterns,
                                                         @Param(MODIFIED_MASK) long modifiedMask,
                                                         @Param(NEW_MASK) long newMask);

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
     * Mark as valid/invalid all site dependencies depending on the existence of the target_path in the site
     *
     * @param siteId the site id
     */
    void validateDependenciesForSite(@Param(SITE_ID) String siteId);
}
