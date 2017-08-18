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
package org.craftercms.studio.impl.v1.service.objectstate;


import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.dal.ItemState;
import org.craftercms.studio.api.v1.dal.ItemStateMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.OBJECT_STATE_BULK_OPERATIONS_BATCH_SIZE;

public class ObjectStateServiceImpl extends AbstractRegistrableService implements ObjectStateService {

    private static final Logger logger = LoggerFactory.getLogger(ObjectStateServiceImpl.class);

    protected State[][] transitionTable = null;

    @Override
    public void register() {
        getServicesManager().registerService(ObjectStateService.class, this);
        initializeTransitionTable();
    }

    @Override
    public ItemState getObjectState(String site, String path) {
        return getObjectState(site, path, true);
    }

    @Override
    public ItemState getObjectState(String site, String path, boolean insert) {
        path = path.replace("//", "/");
        String lockId = site + ":" + path;
        ItemState state = null;
        generalLockService.lock(lockId);
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", path);
            state = itemStateMapper.getObjectStateBySiteAndPath(params);

            if (state == null && insert) {
                if (contentService.contentExists(site, path)) {
                    ContentItemTO item = contentService.getContentItem(site, path, 0);
                    if (!item.isFolder()) {
                        insertNewEntry(site, item);
                        state = itemStateMapper.getObjectStateBySiteAndPath(params);
                    }
                }
            }
        } finally {
            generalLockService.unlock(lockId);
        }
        return state;
    }

    @Override
    public void setSystemProcessing(String site, String path, boolean isSystemProcessing) {
        path = path.replace("//", "/");
        String lockId = site + ":" + path;
        logger.debug("Locking with ID: {0}", lockId);
        generalLockService.lock(lockId);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("path", path);
            params.put("systemProcessing", isSystemProcessing);
            logger.debug("Updating system processing in DB: {0}:{1} - {2}", site, path, isSystemProcessing);
            itemStateMapper.setSystemProcessingBySiteAndPath(params);
        } finally {
            logger.debug("Unlocking with ID: {0}", lockId);
            generalLockService.unlock(lockId);
        }
    }

    @Override
    public void setSystemProcessingBulk(String site, List<String> paths, boolean isSystemProcessing) {
        if (paths == null || paths.isEmpty()) {
            return;
        }

        if (paths.size() < getBulkOperationBatchSize()) {
            setSystemProcessingBulkPartial(site, paths, isSystemProcessing);
        } else {
            List<List<String>> partitions = new ArrayList<List<String>>();
            for (int i = 0; i < paths.size();i = i +  getBulkOperationBatchSize()) {
                partitions.add(paths.subList(i, Math.min(i + getBulkOperationBatchSize(), paths.size())));
            }
            for (List<String> part : partitions) {
                setSystemProcessingBulkPartial(site, part, isSystemProcessing);
            }
        }
    }

    private void setSystemProcessingBulkPartial(String site, List<String> paths, boolean isSystemProcessing) {
        if (paths != null && !paths.isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("paths", paths);
            params.put("systemProcessing", isSystemProcessing);
            itemStateMapper.setSystemProcessingBySiteAndPathBulk(params);
        }
    }

    @Override
    public void transition(String site, ContentItemTO item, TransitionEvent event) {
        String path = item.getUri().replace("//", "/");
        transition(site, path, event);
    }

    @Override
    public void transition(String site, String path, TransitionEvent event) {
        String lockId = site + ":" + path;
        generalLockService.lock(lockId);
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", path);
            ItemState currentState = itemStateMapper.getObjectStateBySiteAndPath(params);
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
                    default:
                        nextState = State.NEW_UNPUBLISHED_UNLOCKED;
                }
            } else {
                logger.debug("Preforming transition event " + event + " on object " + lockId + " with " + currentState.getState() + " state");
                State currentStateValue = State.valueOf(currentState.getState());
                nextState = transitionTable[currentStateValue.ordinal()][event.ordinal()];
            }
            if (currentState == null) {
                ItemState newEntry = new ItemState();
                newEntry.setObjectId(UUID.randomUUID().toString());
                newEntry.setSite(site);
                newEntry.setPath(path);
                newEntry.setSystemProcessing(0);
                newEntry.setState(nextState.name());
                itemStateMapper.insertEntry(newEntry);
            } else if (nextState.toString() != currentState.getState() && nextState != State.NOOP) {
                currentState.setState(nextState.name());
                itemStateMapper.setObjectState(currentState);
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
        String path = item.getUri().replace("//", "/");
        ItemState newEntry = new ItemState();
        if (StringUtils.isEmpty(item.getNodeRef())) {
            newEntry.setObjectId(UUID.randomUUID().toString());
        } else {
            newEntry.setObjectId(item.getNodeRef());
        }
        newEntry.setSite(site);
        newEntry.setPath(path);
        newEntry.setSystemProcessing(0);
        newEntry.setState(State.NEW_UNPUBLISHED_UNLOCKED.name());
        itemStateMapper.insertEntry(newEntry);
    }

    @Override
    public void insertNewEntry(String site, String path) {
        path = path.replace("//", "/");
        ItemState newEntry = new ItemState();
        newEntry.setObjectId(UUID.randomUUID().toString());

        newEntry.setSite(site);
        newEntry.setPath(path);
        newEntry.setSystemProcessing(0);
        newEntry.setState(State.NEW_UNPUBLISHED_UNLOCKED.name());
        itemStateMapper.insertEntry(newEntry);
    }

    @Override
    public List<ItemState> getSubmittedItems(String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> statesValues = new ArrayList<String>();
        for (State state : State.SUBMITTED_STATES) {
            statesValues.add(state.name());
        }
        params.put("states", statesValues);
        params.put("site", site);
        List<ItemState> objects = itemStateMapper.getObjectStateByStates(params);
        return objects;
    }

    @Override
    public void updateObjectPath(String site, String oldPath, String newPath) {
        oldPath = oldPath.replace("//", "/");
        newPath = newPath.replace("//", "/");
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("oldPath", oldPath);
        params.put("newPath", newPath);
        itemStateMapper.updateObjectPath(params);
    }

    @Override
    public boolean isUpdated(String site, String path) {
        path = path.replace("//", "/");
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isUpdated(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isUpdatedOrNew(String site, String path) {
        path = path.replace("//", "/");
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isUpdateOrNew(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isUpdatedOrSubmitted(String site, String path) {
        path = path.replace("//", "/");
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isUpdateOrSubmitted(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isSubmitted(String site, String path) {
        path = path.replace("//", "/");
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isSubmitted(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isNew(String site, String path) {
        path = path.replace("//", "/");
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isNew(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isFolderLive(String site, String folderPath) {
        folderPath = folderPath.replace("//", "");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("folderPath", folderPath + "%");
        return itemStateMapper.isFolderLive(params) > 0;
    }

    @Override
    public boolean isScheduled(String site, String path) {
        path = path.replace("//", "/");
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isScheduled(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public boolean isInWorkflow(String site, String path) {
        path = path.replace("//", "/");
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isInWorkflow(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    public List<ItemState> getChangeSet(String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> statesValues = new ArrayList<String>();
        for (State state : State.CHANGE_SET_STATES) {
            statesValues.add(state.name());
        }
        params.put("states", statesValues);
        params.put("site", site);
        List<ItemState> objects = itemStateMapper.getObjectStateByStates(params);
        return objects;
    }

    @Override
    public void deleteObjectState(String objectId) {
        GeneralLockService nodeLockService = getService(GeneralLockService.class);
        nodeLockService.lock(objectId);
        try {
            itemStateMapper.deleteObjectState(objectId);
        } finally {
            nodeLockService.unlock(objectId);
        }
    }

    @Override
    public void deleteObjectStateForPath(String site, String path) {
        path = path.replace("//", "/");
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path);
        itemStateMapper.deleteObjectStateForSiteAndPath(params);
    }

    @Override
    public void transitionBulk(String site, List<String> paths, TransitionEvent event, State defaultTargetState) {
        if (paths != null && !paths.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("site", site);
            params.put("paths", paths);
            List<ItemState> itemStates = itemStateMapper.getObjectStateForSiteAndPaths(params);
            Map<State, List<String>> bulkSubsets = new HashMap<>();
            for (ItemState state : itemStates) {
                if (!bulkSubsets.containsKey(state.getState())) {
                    bulkSubsets.put(State.valueOf(state.getState()), new ArrayList<String>());
                }
                bulkSubsets.get(State.valueOf(state.getState())).add(state.getObjectId());
            }
            State nextState = null;
            for (Map.Entry<State, List<String>> entry : bulkSubsets.entrySet()) {
                if (entry.getKey() == null) {
                    params = new HashMap<>();
                    params.put("site", site);
                    params.put("paths", paths);
                    params.put("state", defaultTargetState.name());
                    itemStateMapper.setObjectStateForSiteAndPaths(params);
                } else {
                    nextState = transitionTable[entry.getKey().ordinal()][event.ordinal()];
                    if (nextState != entry.getKey() && nextState != State.NOOP) {
                        params = new HashMap<>();
                        params.put("site", site);
                        params.put("paths", paths);
                        params.put("state", nextState.name());
                        itemStateMapper.setObjectStateForSiteAndPaths(params);
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
        path = path.replace("//", "/");
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

    @Override
    public void deleteObjectStatesForSite(String site) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        objectStateMapper.deleteObjectStatesForSite(params);
    }

    public int getBulkOperationBatchSize() {
        int toReturn = Integer.parseInt(studioConfiguration.getProperty(OBJECT_STATE_BULK_OPERATIONS_BATCH_SIZE));
        return toReturn;
    }

    @Override
    public void setStateForSiteContent(String site, State state) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", site);
        params.put("state", state.name());
        objectStateMapper.setStateForSiteContent(params);
    }

    @Override
    public List<String> getChangeSetForSubtree(String site, String path) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("likepath", path + (path.endsWith("/") ? "" : File.separator) + "%");
        params.put("states", State.CHANGE_SET_STATES);
        List<ObjectState> result = objectStateMapper.getChangeSetForSubtree(params);
        List<String> toRet = new ArrayList<String>();
        for (ObjectState state : result) {
            toRet.add(state.getPath());
        }
        return toRet;
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
    protected ContentService contentService;
    protected StudioConfiguration studioConfiguration;

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }
}
