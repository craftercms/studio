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
public interface ItemStateMapper {

    String SITE_PARAM = "site";
    String EDITED_STATES_PARAM = "editedStates";
    String NEW_STATES_PARAM = "newStates";
    String POSSIBLE_PARENTS_PARAM = "possibleParents";

    List<ItemState> getObjectStateByStates(Map params);

    void deleteObjectState(String objectId);

    ItemState getObjectStateBySiteAndPath(Map params);

    void setSystemProcessingBySiteAndPath(Map params);

    void setSystemProcessingBySiteAndPathBulk(Map params);

    void insertEntry(ItemState itemState);

    void setObjectState(ItemState itemState);

    List<ItemState> getObjectStateForSiteAndPaths(Map params);

    void setObjectStateForSiteAndPaths(Map params);

    void updateObjectPath(Map params);

    void deleteObjectStatesForSite(Map params);

    void deleteObjectStateForSiteAndPath(Map params);

    void deleteObjectStateForSiteAndFolder(Map params);

    int isFolderLive(Map<String, Object> params);

    void setStateForSiteContent(Map params);

    List<ItemState> getChangeSetForSubtree(Map params);

    int deletedPathExists(Map params);

    void deployCommitId(Map params);

    List<String> getMandatoryParentsForPublishing(Map params);
}
