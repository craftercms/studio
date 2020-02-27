/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.service.objectstate.State;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dejan Brkic
 */
public interface DependencyDAO {

    String SITE_PARAM = "site";
    String SITE_ID_PARAM = "siteId";
    String PATH_PARAM = "path";
    String PATHS_PARAM = "paths";
    String OLD_PATH_PARAM = "oldPath";
    String NEW_PATH_PARAM = "newPath";
    String REGEX_PARAM = "regex";
    String EDITED_STATES_PARAM = "editedStates";
    String NEW_STATES_PARAM = "newStates";

    String SORUCE_PATH_COLUMN_NAME = "source_path";
    String TARGET_PATH_COLUMN_NAME = "target_path";

    /**
     * Get soft dependencies from DB for list of content paths
     *
     * @param site site identifier
     * @param paths list of content paths
     * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
     * @param editedStates list of edited states
     *
     * @return List of soft dependencies
     */
    List<Map<String, String>> getSoftDependenciesForList(@Param("site") String site, @Param("paths") Set<String> paths,
                                                         @Param("regex") List<String> itemSpecificDependenciesPatterns,
                                                         @Param("editedStates") Collection<State> editedStates);

    /**
     * Get hard dependencies from DB for list of content paths
     *
     * @param site site identifier
     * @param paths list of content paths
     * @param itemSpecificDependenciesPatterns list of patterns that define item specific dependencies
     * @param editedStates list of edited states
     * @param newStates list of new states
     *
     * @return List of hard dependencies
     */
    List<Map<String, String>> getHardDependenciesForList(@Param("site") String site, @Param("paths") Set<String> paths,
                                                         @Param("regex") List<String> itemSpecificDependenciesPatterns,
                                                         @Param("editedStates") Collection<State> editedStates,
                                                         @Param("newStates") List<State> newStates);

    /**
     * Get items depending on given paths
     *
     * @param siteId site identifier
     * @param paths list of content paths
     * @return List of items depending on given paths
     */
    List<String> getDependentItems(@Param("siteId") String siteId, @Param("paths") List<String> paths);

    /**
     * Get item specific dependencies for given paths
     *
     * @param siteId site identifier
     * @param paths list of content paths
     * @param regex list of patterns that define item specific dependencies
     *
     * @return list of item specific dependencies
     */
    List<String> getItemSpecificDependencies(@Param("siteId") String siteId, @Param("paths") List<String> paths,
                                             @Param("regex") List<String> regex);

}
