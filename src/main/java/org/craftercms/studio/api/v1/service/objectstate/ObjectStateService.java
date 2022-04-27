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
package org.craftercms.studio.api.v1.service.objectstate;


import org.craftercms.studio.api.v1.dal.ItemState;
import org.craftercms.studio.api.v1.to.ContentItemTO;

import java.util.List;

public interface ObjectStateService {

    ItemState getObjectState(String site, String path);

    void transition(String site, ContentItemTO item, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent event);

    void transition(String site, String path, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent event);

    void insertNewEntry(String site, ContentItemTO item);

    void insertNewEntry(String site, String path);

    List<ItemState> getSubmittedItems(String site);

    ItemState getObjectState(String site, String path, boolean insert);

    void setSystemProcessing(String site, String path, boolean isSystemProcessing);

    void setSystemProcessingBulk(String site, List<String> paths, boolean isSystemProcessing);

    void updateObjectPath(String site, String oldPath, String newPath);

    boolean isUpdated(String site, String path);

    boolean isUpdatedOrNew(String site, String path);

    boolean isUpdatedOrSubmitted(String site, String path);

    boolean isNew(String site, String path);

    List<ItemState> getChangeSet(String site);

    void deleteObjectState(String objectId);

    void deleteObjectStateForPath(String site, String path);

    void deleteObjectStatesForFolder(String site, String path);

    void transitionBulk(String site, List<String> paths, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent event, org.craftercms.studio.api.v1.service.objectstate.State defaultTargetState);

    /**
     * get the object for a given set of states
     */
    List<ItemState> getObjectStateByStates(String site, List<String> states);

    boolean isScheduled(String site, String path);

    boolean isInWorkflow(String site,String path);

    void deleteObjectStatesForSite(String site);

    boolean isSubmitted(String site, String dep);

    void setStateForSiteContent(String site, State state);

    List<String> getChangeSetForSubtree(String site, String path);

    boolean deletedPathExists(String site, String path);

    void deployCommitId(String site, String commitId);

    String setObjectState(String site, String path, String state, boolean systemProcessing);
}
