/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.service.objectstate;


import org.craftercms.studio.api.v1.dal.ObjectState;
import org.craftercms.studio.api.v1.to.ContentItemTO;

import java.util.Arrays;
import java.util.List;

public interface ObjectStateService {

/*
    public void beginSystemProcessing(String fullPath);

    public void beginSystemProcessing(NodeRef nodeRef);

    public void endSystemProcessing(String fullPath);

    public void endSystemProcessing(NodeRef nodeRef);
*/
    public ObjectState getObjectState(String site, String path);
/*
    public State getObjectState(NodeRef nodeRef);

    public State getRealObjectState(NodeRef nodeRef);
*/
    void transition(String site, ContentItemTO item, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent event);

    void insertNewEntry(String site, ContentItemTO item);

    void insertNewEntry(String site, String path);

    /*
        public void transition(NodeRef nodeRef, TransitionEvent event);

        public void insertNewObjectEntry(String fullPath);

        public void insertNewObjectEntry(NodeRef nodeRef);
    */
    public List<ObjectState> getSubmittedItems(String site);

    ObjectState getObjectState(String site, String path, boolean insert);

    public void setSystemProcessing(String site, String path, boolean isSystemProcessing);
/*
    public void setSystemProcessing(NodeRef nodeRef, boolean isSystemProcessing);
*/
    void setSystemProcessingBulk(String site, List<String> paths, boolean isSystemProcessing);

    public void updateObjectPath(String site, String oldPath, String newPath);

    public boolean isUpdatedOrNew(String site, String path);
/*
    public boolean isUpdatedOrNew(NodeRef nodeRef);

    public State[][] getTransitionMapping();
*/
    public boolean isNew(String site, String path);
/*
    public boolean isNew(NodeRef nodeRef);

    public boolean isFolderLive(String fullPath);
*/
    public List<ObjectState> getChangeSet(String site);

    public void deleteObjectState(String objectId);

    public void deleteObjectStateForPath(String site, String path);
/*
    public void deleteObjectStateForPaths(String site, List<String> paths);
*/
    void transitionBulk(String site, List<String> paths, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent event, org.craftercms.studio.api.v1.service.objectstate.State defaultTargetState);

    /**
     * get the object for a given set of states
     */
    List<ObjectState> getObjectStateByStates(String site, List<String> states);

    public boolean isScheduled(String site, String path);

    public boolean isInWorkflow(String site,String path);
/*
    public boolean isInWorkflow(NodeRef nodeRef);
    */

    void deleteObjectStatesForSite(String site);

}
