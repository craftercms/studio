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


import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.dal.ObjectState;
import org.craftercms.studio.api.v1.dal.ObjectStateMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class ObjectStateServiceImpl extends AbstractRegistrableService implements ObjectStateService {

    private static final Logger logger = LoggerFactory.getLogger(ObjectStateServiceImpl.class);

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
*/
    @Override
    public ObjectState getObjectState(String site, String path) {
        String lockId = site + ":" + path;
        ObjectState state = null;
        generalLockService.lock(lockId);
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", path);
            state = objectStateMapper.getObjectStateBySiteAndPath(params);
        } finally {
            generalLockService.unlock(lockId);
        }
        return state;
    }
/*
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
*/
    @Override
    public void setSystemProcessing(String site, String path, boolean isSystemProcessing) {
        String lockId = site + ":" + path;
        logger.debug("Locking with ID: {0}", lockId);
        generalLockService.lock(lockId);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("path", path);
            params.put("systemProcessing", isSystemProcessing);
            logger.debug("Updating system processing in DB: {0}:{1} - {2}", site, path, isSystemProcessing);
            objectStateMapper.setSystemProcessingBySiteAndPath(params);
        } finally {
            logger.debug("Unlocking with ID: {0}", lockId);
            generalLockService.unlock(lockId);
        }
    }
/*
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
*/
    @Override
    public void transition(String site, ContentItemTO item, TransitionEvent event) {
        String lockId = site + ":" + item.getPath();
        generalLockService.lock(lockId);
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", item.getPath());
            ObjectState currentState = objectStateMapper.getObjectStateBySiteAndPath(params);
            State nextState = null;
            if (currentState == null) {
                logger.debug("Preforming transition event " + event.name() + " on object " + lockId + " without current state");
                switch (event) {
                    case SAVE:
                        nextState = State.NEW_UNPUBLISHED_UNLOCKED;
                        break;
                    case SAVE_FOR_PREVIEW:
                        nextState = State.NEW_UNPUBLISHED_LOCKED;
                        break;
                }
            } else {
                logger.debug("Preforming transition event " + event + " on object " + lockId + " with " + currentState.getState() + " state");
                State currentStateValue = State.valueOf(currentState.getState());
                nextState = transitionTable[currentStateValue.ordinal()][event.ordinal()];
            }
            if (currentState == null) {
                ObjectState newEntry = new ObjectState();
                newEntry.setObjectId(item.getNodeRef());
                newEntry.setSite(site);
                newEntry.setPath(item.getPath());
                newEntry.setSystemProcessing(0);
                newEntry.setState(nextState.name());
                objectStateMapper.insertEntry(newEntry);
            } else if (nextState.toString() != currentState.getState() && nextState != State.NOOP) {
                currentState.setState(nextState.name());
                objectStateMapper.setObjectState(currentState);
            } else if (nextState == State.NOOP) {
                logger.warn("Transition not defined for event " + event.name() + " and current state " + currentState.getState() + " [object id: " + currentState.getObjectId() + "]");
            }
        } catch (Exception e) {
            logger.error("Transition not defined for event", e);
        } finally {
            generalLockService.unlock(lockId);
        }
        logger.debug("Transition finished for " + event.name() + " on object " + lockId);
    }

    @Override
    public void insertNewEntry(String site, ContentItemTO item) {
        ObjectState newEntry = new ObjectState();
        if (StringUtils.isEmpty(item.getNodeRef())) {
            newEntry.setObjectId(UUID.randomUUID().toString());
        } else {
            newEntry.setObjectId(item.getNodeRef());
        }
        newEntry.setSite(site);
        newEntry.setPath(item.getUri());
        newEntry.setSystemProcessing(0);
        newEntry.setState(State.NEW_UNPUBLISHED_UNLOCKED.name());
        objectStateMapper.insertEntry(newEntry);
    }

    @Override
    public void insertNewEntry(String site, String path) {
        ObjectState newEntry = new ObjectState();
        newEntry.setObjectId(UUID.randomUUID().toString());

        newEntry.setSite(site);
        newEntry.setPath(path);
        newEntry.setSystemProcessing(0);
        newEntry.setState(State.NEW_UNPUBLISHED_UNLOCKED.name());
        objectStateMapper.insertEntry(newEntry);
    }
/*
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

    @Override
    public void updateObjectPath(String site, String oldPath, String newPath) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("oldPath", oldPath);
        params.put("newPath", newPath);
        objectStateMapper.updateObjectPath(params);
    }
/*
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
*/
    @Override
    public boolean isUpdatedOrNew(String site, String path) {
        ObjectState state = getObjectState(site, path);
        if (state != null) {
            return State.isUpdateOrNew(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isNew(String site, String path) {
        ObjectState state = getObjectState(site, path);
        if (state != null) {
            return State.isNew(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isScheduled(String site, String path) {
        ObjectState state = getObjectState(site, path);
        if (state != null) {
            return State.isScheduled(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isInWorkflow(String site, String path) {
        ObjectState state = getObjectState(site, path);
        if (state != null) {
            return State.isInWorkflow(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }
/*
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
*/
    @Override
    public void transitionBulk(String site, List<String> paths, TransitionEvent event, State defaultTargetState) {
        if (paths != null && !paths.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("site", site);
            params.put("paths", paths);
            List<ObjectState> objectStates = objectStateMapper.getObjectStateForSiteAndPaths(params);
            Map<State, List<String>> bulkSubsets = new HashMap<>();
            for (ObjectState state : objectStates) {
                if (!bulkSubsets.containsKey(state.getState())) {
                    bulkSubsets.put(State.valueOf(state.getState()), new ArrayList<String>());
                }
                bulkSubsets.get(state.getState()).add(state.getObjectId());
            }
            State nextState = null;
            for (Map.Entry<State, List<String>> entry : bulkSubsets.entrySet()) {
                if (entry.getKey() == null) {
                    params = new HashMap<>();
                    params.put("site", site);
                    params.put("paths", paths);
                    params.put("state", defaultTargetState.name());
                    objectStateMapper.setObjectStateForSiteAndPaths(params);
                } else {
                    nextState = transitionTable[entry.getKey().ordinal()][event.ordinal()];
                    if (nextState != entry.getKey() && nextState != State.NOOP) {
                        params = new HashMap<>();
                        params.put("site", site);
                        params.put("paths", paths);
                        params.put("state", nextState.name());
                        objectStateMapper.setObjectStateForSiteAndPaths(params);
                    } else if (nextState == State.NOOP) {
                        logger.warn("Transition not defined for event " + event.name() + " and current state " + entry.getKey().name() + " [setting object state for multiple objects]");
                    }
                }
            }
        }
    }
    
    /**
     * get the object for a given set of states
     */
    public List<ObjectState> getObjectStateByStates(String site, List<String> states) {

        if (states != null && !states.isEmpty()) {
            /*
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> statesValues = new ArrayList<String>();
            for (State state : enumStates) {
                statesValues.add(state.name());
            }*/
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("states", states);
            params.put("site", site);
            List<ObjectState> result = objectStateMapper.getObjectStateByStates(params);
            return result;
        } else {
            return new ArrayList<>(0);
        }
    }

    public String setObjectState(String site, String path, String state, boolean systemProcessing) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path);
        ObjectState objectState = objectStateMapper.getObjectStateBySiteAndPath(params);
        if (objectState == null) {
            insertNewEntry(site, path);
            objectState = objectStateMapper.getObjectStateBySiteAndPath(params);
        }
        objectState.setState(state);
        objectState.setSystemProcessing(systemProcessing ? 1 : 0);
        objectStateMapper.setObjectState(objectState);
        return "Success";
    }

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

    protected GeneralLockService generalLockService;

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }
}
