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

    String SORUCE_PATH_COLUMN_NAME = "source_path";
    String TARGET_PATH_COLUMN_NAME = "target_path";

    List<DependencyEntity> getDependencies(Map params);

    List<DependencyEntity> getDependant(Map params);

    void deleteAllSourceDependencies(Map params);

    void insertList(Map params);

    void deleteDependenciesForSite(Map params);

    void deleteDependenciesForSiteAndPath(Map params);

    List<String> getDependenciesForList(Map params);

    List<String> getItemSpecificDependenciesForList(Map params);

    List<String> getItemsDependingOn(Map params);

    List<Map<String, String>> calculatePublishingDependenciesForList(Map params);

    void moveDependency(Map params);
}
