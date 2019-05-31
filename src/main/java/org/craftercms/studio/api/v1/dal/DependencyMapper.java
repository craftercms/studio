/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v1.dal;

import java.util.List;
import java.util.Map;

/**
 * @author Dejan Brkic
 */
public interface DependencyMapper {

    String SITE_PARAM = "site";
    String SITE_ID_PARAM = "siteId";
    String PATH_PARAM = "path";
    String PATHS_PARAM = "paths";
    String OLD_PATH_PARAM = "oldPath";
    String NEW_PATH_PARAM = "newPath";
    String REGEX_PARAM = "regex";
    String EDITED_STATES_PARAM = "editedStates";
    String NEW_STATES_PARAM = "newStates";
    String CHANGE_SET_STATES_PARAM = "changeSetStates";

    String SORUCE_PATH_COLUMN_NAME = "source_path";
    String TARGET_PATH_COLUMN_NAME = "target_path";

    List<DependencyEntity> getDependencies(Map params);

    List<DependencyEntity> getDependant(Map params);

    void deleteAllSourceDependencies(Map params);

    void insertList(Map params);

    List<DependencyEntity> getDependenciesByType(Map params);

    void deleteDependenciesForSite(Map params);

    void deleteDependenciesForSiteAndPath(Map params);

    void deleteDependenciesForSiteAndListOfPaths(Map params);

    List<String> getDependenciesForList(Map params);

    List<String> getPublishingDependenciesForList(Map params);

    List<String> getItemsDependingOn(Map params);

    List<Map<String, String>> calculatePublishingDependenciesForList(Map params);

    List<Map<String, String>> calculateAllPublishingDependenciesForList(Map params);

    void moveDependency(Map params);
}
