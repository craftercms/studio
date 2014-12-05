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
package org.craftercms.studio.impl.v1.service.objectstate;


import org.craftercms.studio.api.v1.dal.ObjectState;
import org.craftercms.studio.api.v1.dal.ObjectStateMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectStateServiceImpl extends AbstractRegistrableService implements ObjectStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectStateServiceImpl.class);

    protected State[][] transitionTable = null;

    @Override
    public void register() {
        getServicesManager().registerService(ObjectStateService.class, this);
        initializeTransitionTable();
    }
/*
    @Override
    public void beginSystemProcessing(String fullPath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        persistenceManagerService.setSystemProcessing(fullPath, true);
    }

    @Override
    public void beginSystemProcessing(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        persistenceManagerService.setSystemProcessing(nodeRef, true);
    }

    @Override
    public void endSystemProcessing(String fullPath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        persistenceManagerService.setSystemProcessing(fullPath, false);
    }

    @Override
    public void endSystemProcessing(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        persistenceManagerService.setSystemProcessing(nodeRef, false);
    }

    @Override
    public State getObjectState(String fullPath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        return getObjectState(nodeRef);
    }

    @Override
    public State getObjectState(NodeRef nodeRef) {
    	if (nodeRef != null) {
            GeneralLockService nodeLockService = getService(GeneralLockService.class);
            ObjectStateTO state;
            nodeLockService.lock(nodeRef.getId());
            try {
                state = objectStateDAOService.getObjectState(nodeRef.getId());
                if (state == null) {
                    PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                    DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
                    objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
                    state = objectStateDAOService.getObjectState(nodeRef.getId());
                }
            } finally {
                nodeLockService.unlock(nodeRef.getId());
            }

            if (state != null) {
                if (state.isSystemProcessing()) {
                    return State.SYSTEM_PROCESSING;
                } else {
                    return state.getState();
                }
            } else {
                return State.NOOP;
            }
        } else {
            return State.NOOP;
        }
    }

    @Override
	// TODO: CodeRev: get REAL state? looks like duplicate code minus processing
    public State getRealObjectState(NodeRef nodeRef) {
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        ObjectStateTO state = null;
        nodeLockService.lock(nodeRef.getId());
        try {
            state = objectStateDAOService.getObjectState(nodeRef.getId());
            if (state == null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
                objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
                state = objectStateDAOService.getObjectState(nodeRef.getId());
            }
        } finally {
            nodeLockService.unlock(nodeRef.getId());
        }
        return state.getState();
    }

    @Override
    public void setSystemProcessing(String fullPath, boolean isSystemProcessing) {
    	PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);

    	NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        if (nodeRef != null) {
    	    setSystemProcessing(nodeRef, isSystemProcessing);
        } else {
            LOGGER.error(String.format("Error setting system processing flag. Content at path %s does not exist", fullPath));
        }
    }

    @Override
    public void setSystemProcessing(NodeRef nodeRef, boolean isSystemProcessing) {
        if (nodeRef != null) {
            GeneralLockService nodeLockService = getService(GeneralLockService.class);

            nodeLockService.lock(nodeRef.getId());
            try {
                objectStateDAOService.setSystemProcessing(nodeRef.getId(), isSystemProcessing);
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error setting system processing flag", e);
                }
             // TODO: CodeRev: eating the error, how does caller know?
            } finally {
                nodeLockService.unlock(nodeRef.getId());

            }
        } else {
            LOGGER.error(String.format("Error setting system processing flag. NodeRef is null"));
        }
    }

    @Override
    public void setSystemProcessingBulk(List<String> objectIds, boolean isSystemProcessing) {
        if (objectIds == null || objectIds.isEmpty()) {
            return;
        }
        GeneralLockService nodeLockService = getService(GeneralLockService.class);

        // TODO: CodeRev: a lot of O(n) here, cant this be done in a single loop inside bulk op?
        for (String objectId : objectIds) {
            nodeLockService.lock(objectId);
        }
        
        try {
            objectStateDAOService.setSystemProcessingBulk(objectIds, isSystemProcessing);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error setting system processing flag", e);
            }
        } finally {
            for (String objectId : objectIds) {
                nodeLockService.unlock(objectId);
            }
        }
    }

    @Override
    public void transition(String fullPath, TransitionEvent event) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        if (nodeRef != null) {
            transition(nodeRef, event);
        } else {
            LOGGER.error(String.format("Transition can not be performed for event %s. Content at path %s does not exist.", event, fullPath));
        }
        // TODO: CodeRev:  no indication of failure to caller
    }

    @Override
    public void transition(NodeRef nodeRef, TransitionEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Transition started for " + event.name() + " on object " + nodeRef.getId());
        }
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        nodeLockService.lock(nodeRef.getId());
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            ObjectStateTO currentState = objectStateDAOService.getObjectState(nodeRef.getId());
            State nextState = null;
            if (currentState == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Preforming transition event " + event.name() + " on object " + nodeRef.getId() + " without current state");
                }
                switch (event) {
                    case SAVE:
                        nextState = State.NEW_UNPUBLISHED_UNLOCKED;
                        break;
                    case SAVE_FOR_PREVIEW:
                        nextState = State.NEW_UNPUBLISHED_LOCKED;
                        break;
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Preforming transition event " + event.name() + " on object " + nodeRef.getId() + " with " + currentState.getState().name() + " state");
                }
                nextState = transitionTable[currentState.getState().ordinal()][event.ordinal()];
            }
            if (currentState == null) {
                insertNewObjectEntry(nodeRef);
                persistenceManagerService.setObjectState(nodeRef, nextState);
            } else if (nextState != currentState.getState() && nextState != State.NOOP) {
                persistenceManagerService.setObjectState(nodeRef, nextState);
            } else if (nextState == State.NOOP) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Transition not defined for event " + event.name() + " and current state " + currentState.getState().name() + " [object id: " + nodeRef.getId() + "]");
                }
            }
        } catch (Exception e) {
        	// TODO: CodeRev: So item gets stuck in state and caller never know it.......
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Transition not defined for event", e);
            }
        } finally {
            nodeLockService.unlock(nodeRef.getId());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Transition finished for " + event.name() + " on object " + nodeRef.getId());
        }
    }

    protected void insertNewObjectEntryWithState(NodeRef nodeRef, State state) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
        if (StringUtils.isNotEmpty(dmPathTO.getSiteName())) {
            objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
        }
    }

    @Override
    public void insertNewObjectEntry(String fullPath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        insertNewObjectEntry(persistenceManagerService.getNodeRef(fullPath));
    }

    @Override
    public void insertNewObjectEntry(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
        if (StringUtils.isNotEmpty(dmPathTO.getSiteName())) {
                objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
        }
    }
*/
    @Override
    public List<ObjectState> getSubmittedItems(String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> statesValues = new ArrayList<String>();
        for (State state : State.SUBMITTED_STATES) {
            statesValues.add(state.name());
        }
        params.put("states", statesValues);
        params.put("site", site);
        List<ObjectState> objects = objectStateMapper.getObjectStateByStates(params);
        return objects;
    }
/*
    @Override
    public void updateObjectPath(String fullPath, String newPath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        updateObjectPath(persistenceManagerService.getNodeRef(fullPath), newPath);
    }

    @Override
    public void updateObjectPath(NodeRef nodeRef, String newPath) {
        GeneralLockService lockService = getService(GeneralLockService.class);
        lockService.lock(nodeRef.getId());
        try {
            objectStateDAOService.updateObjectPath(nodeRef.getId(), newPath);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while updating object path: " + nodeRef.getId() + ", " + newPath);
            }
        } finally {
            lockService.unlock(nodeRef.getId());
        }
    }

    @Override
    public boolean isUpdatedOrNew(String path) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return isUpdatedOrNew(persistenceManagerService.getNodeRef(path));
    }

    @Override
    public boolean isUpdatedOrNew(NodeRef nodeRef) {
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        ObjectStateTO state;
        nodeLockService.lock(nodeRef.getId());
        try {
            state = objectStateDAOService.getObjectState(nodeRef.getId());
            if (state == null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
                    objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
                state = objectStateDAOService.getObjectState(nodeRef.getId());
            }
        } finally {
            nodeLockService.unlock(nodeRef.getId());
        }
        if (state != null) {
            return ObjectStateService.State.isUpdateOrNew(state.getState());
        } else {
            return false;
        }
    }

    @Override
    public boolean isNew(String path) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return isNew(persistenceManagerService.getNodeRef(path));
    }

    @Override
    public boolean isNew(NodeRef nodeRef) {
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        ObjectStateTO state;
        nodeLockService.lock(nodeRef.getId());
        try {
            state = objectStateDAOService.getObjectState(nodeRef.getId());
            if (state == null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
                    objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
                state = objectStateDAOService.getObjectState(nodeRef.getId());
            }
        } finally {
            nodeLockService.unlock(nodeRef.getId());
        }
        if (state != null) {
            return ObjectStateService.State.isNew(state.getState());
        } else {
            return false;
        }
    }

    @Override
    public boolean isScheduled(String path) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return isScheduled(persistenceManagerService.getNodeRef(path));
    }

    @Override
    public boolean isScheduled(NodeRef nodeRef) {
        if (nodeRef == null) return false;
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        ObjectStateTO state;
        nodeLockService.lock(nodeRef.getId());
        try {
            state = objectStateDAOService.getObjectState(nodeRef.getId());
            if (state == null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
                    objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
                state = objectStateDAOService.getObjectState(nodeRef.getId());
            }
        } finally {
            nodeLockService.unlock(nodeRef.getId());
        }
        if (state != null) {
            return ObjectStateService.State.isScheduled(state.getState());
        } else {
            return false;
        }
    }

    @Override
    public boolean isInWorkflow(String path) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return isInWorkflow(persistenceManagerService.getNodeRef(path));
    }

    @Override
    public boolean isInWorkflow(NodeRef nodeRef) {
        if (nodeRef == null) return false;
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        ObjectStateTO state;
        nodeLockService.lock(nodeRef.getId());
        try {
            state = objectStateDAOService.getObjectState(nodeRef.getId());
            if (state == null) {
                PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
                DmPathTO dmPathTO = new DmPathTO(persistenceManagerService.getNodePath(nodeRef));
                    objectStateDAOService.insertNewObject(nodeRef.getId(), dmPathTO.getSiteName(), dmPathTO.getRelativePath());
                state = objectStateDAOService.getObjectState(nodeRef.getId());
            }
        } finally {
            nodeLockService.unlock(nodeRef.getId());
        }
        if (state != null) {
            return ObjectStateService.State.isInWorkflow(state.getState());
        } else {
            return false;
        }
    }

    @Override
    public State[][] getTransitionMapping() {
        return this.transitionTable;
    }

    @Override
    public boolean isFolderLive(String fullPath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        DmPathTO dmPathTO = new DmPathTO(fullPath);
        return objectStateDAOService.isFolderLive(dmPathTO.getSiteName(), dmPathTO.getRelativePath());
    }
*/
    @Override
    public List<ObjectState> getChangeSet(String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> statesValues = new ArrayList<String>();
        for (State state : State.CHANGE_SET_STATES) {
            statesValues.add(state.name());
        }
        params.put("states", statesValues);
        params.put("site", site);
        List<ObjectState> objects = objectStateMapper.getObjectStateByStates(params);
        return objects;
    }

    @Override
    public void deleteObjectState(String objectId) {
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        nodeLockService.lock(objectId);
        try {
            objectStateMapper.deleteObjectState(objectId);
        } finally {
            nodeLockService.unlock(objectId);
        }
    }
/*
    @Override
    public void deleteObjectStateForPath(String site, String path) {
        objectStateDAOService.deleteObjectStatesForPath(site, path);
    }

    @Override
    public void deleteObjectStateForPaths(String site, List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        objectStateDAOService.deleteObjectStatesForPaths(site, paths);
    }

    @Override
    public void transitionBulk(List<String> objectIds, TransitionEvent event, State defaultTargetState) {
        if (objectIds != null && !objectIds.isEmpty()) {
            List<ObjectStateTO> objectStates = objectStateDAOService.getObjectStates(objectIds);
            Map<State, List<String>> bulkSubsets = new FastMap<State, List<String>>();
            for (ObjectStateTO state : objectStates) {
                if (!bulkSubsets.containsKey(state.getState())) {
                    bulkSubsets.put(state.getState(), new FastList<String>());
                }
                bulkSubsets.get(state.getState()).add(state.getObjectId());
            }
            State nextState = null;
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            for (Map.Entry<State, List<String>> entry : bulkSubsets.entrySet()) {
                if (entry.getKey() == null) {
                    objectStateDAOService.setObjectStateBulk(entry.getValue(), defaultTargetState);
                } else {
                    nextState = transitionTable[entry.getKey().ordinal()][event.ordinal()];
                    if (nextState != entry.getKey() && nextState != State.NOOP) {
                        objectStateDAOService.setObjectStateBulk(entry.getValue(), nextState);
                    } else if (nextState == State.NOOP) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Transition not defined for event " + event.name() + " and current state " + entry.getKey().name() + " [setting object state for multiple objects]");
                        }
                    }
                }
            }
        }
    }*/
    
    /**
     * get the object for a given set of states
     *//*
    public List<ObjectStateTO> getObjectStateByStates(String site, List<ObjectStateService.State> states) {
    	return  objectStateDAOService.getObjectStateByStates(site, states);
    }
*/

    private void initializeTransitionTable() {
        transitionTable = new State[][]{
                {State.NEW_DELETED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NOOP,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_PUBLISHING_FAILED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.NOOP,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP},
                {State.EXISTING_DELETED,State.EXISTING_UNEDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_UNEDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.NOOP,State.EXISTING_UNEDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.EXISTING_DELETED,State.EXISTING_UNEDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED,State.EXISTING_EDITED_LOCKED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.EXISTING_SUBMITTED_WITH_WF_SCHEDULED,State.EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,State.EXISTING_SUBMITTED_NO_WF_SCHEDULED,State.EXISTING_EDITED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.EXISTING_PUBLISHING_FAILED},
                {State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.EXISTING_EDITED_UNLOCKED,State.NOOP,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP,State.NOOP}
        };
    }

    @Autowired
    protected ObjectStateMapper objectStateMapper;
}
