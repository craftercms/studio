/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v1.service.objectstate;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
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
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.RetryingOperationFacade;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.OBJECT_STATE_BULK_OPERATIONS_BATCH_SIZE;

public class ObjectStateServiceImpl extends AbstractRegistrableService implements ObjectStateService {

    private static final Logger logger = LoggerFactory.getLogger(ObjectStateServiceImpl.class);

    protected State[][] transitionTable = null;

    protected ItemStateMapper itemStateMapper;

    protected GeneralLockService generalLockService;
    protected ContentService contentService;
    protected StudioConfiguration studioConfiguration;
    protected RetryingOperationFacade retryingOperationFacade;

    @Override
    public void register() {
        getServicesManager().registerService(ObjectStateService.class, this);
        initializeTransitionTable();
    }

    @Override
    @ValidateParams
    public ItemState getObjectState(@ValidateStringParam(name = "site") String site,
                                    @ValidateSecurePathParam(name = "path") String path) {
        return getObjectState(site, path, true);
    }

    @Override
    @ValidateParams
    public ItemState getObjectState(@ValidateStringParam(name = "site") String site,
                                    @ValidateSecurePathParam(name = "path") String path, boolean insert) {
        String cleanPath = FilenameUtils.normalize(path, true);
        String lockId = site + ":" + cleanPath;
        ItemState state = null;
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", cleanPath);
        state = itemStateMapper.getObjectStateBySiteAndPath(params);

        if (state == null && insert) {
            if (contentService.contentExists(site, cleanPath)) {
                ContentItemTO item = contentService.getContentItem(site, cleanPath, 0);
                if (!item.isFolder()) {
                    insertNewEntry(site, item);
                    state = itemStateMapper.getObjectStateBySiteAndPath(params);
                }
            }
        }
        return state;
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void setSystemProcessing(@ValidateStringParam(name = "site") String site,
                                    @ValidateSecurePathParam(name = "path") String path, boolean isSystemProcessing) {
        String cleanPath = FilenameUtils.normalize(path, true);
        String lockId = site + ":" + cleanPath;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", cleanPath);
        params.put("systemProcessing", isSystemProcessing);
        logger.debug("Updating system processing in DB: {0}:{1} - {2}", site, cleanPath, isSystemProcessing);
        itemStateMapper.setSystemProcessingBySiteAndPath(params);
    }

    @Override
    @ValidateParams
    public void setSystemProcessingBulk(@ValidateStringParam(name = "site") String site, List<String> paths,
                                        boolean isSystemProcessing) {
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

    public void setSystemProcessingBulkPartial(String site, List<String> paths, boolean isSystemProcessing) {
        if (paths != null && !paths.isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("paths", paths);
            params.put("systemProcessing", isSystemProcessing);
            retryingOperationFacade.setSystemProcessingBySiteAndPathBulk(params);
        }
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void transition(@ValidateStringParam(name = "site") String site, ContentItemTO item, TransitionEvent event) {
        String path = FilenameUtils.normalize(item.getUri(), true);
        transition(site, path, event);
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void transition(@ValidateStringParam(name = "site") String site,
                           @ValidateSecurePathParam(name = "path") String path, TransitionEvent event) {
        String itemPath = FilenameUtils.normalize(path, true);
        String lockKey = site + ":" + path;
        generalLockService.lock(lockKey);
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", itemPath);
            ItemState currentState = itemStateMapper.getObjectStateBySiteAndPath(params);
            State nextState = null;
            if (currentState == null) {
                logger.debug("Preforming transition event " + event.name() + " on object " + lockKey +
                        " without current state");
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
                logger.debug("Preforming transition event " + event + " on object " + lockKey + " with " +
                        currentState.getState() + " state");
                State currentStateValue = State.valueOf(currentState.getState());
                nextState = transitionTable[currentStateValue.ordinal()][event.ordinal()];
            }
            if (currentState == null) {
                ItemState newEntry = new ItemState();
                newEntry.setObjectId(UUID.randomUUID().toString());
                newEntry.setSite(site);
                newEntry.setPath(itemPath);
                newEntry.setSystemProcessing(0);
                newEntry.setState(nextState.name());
                itemStateMapper.insertEntry(newEntry);
            } else if (nextState.toString() != currentState.getState() && nextState != State.NOOP) {
                currentState.setState(nextState.name());
                itemStateMapper.setObjectState(currentState);
            } else if (nextState == State.NOOP) {
                logger.warn("Transition not defined for event " + event.name() + " and current state " +
                        currentState.getState() + " [object id: " + currentState.getObjectId() + "]");
            }
        } catch (Exception e) {
            logger.error("Transition not defined for event", e);
        } finally {
            generalLockService.unlock(lockKey);
        }
        logger.debug("Transition finished for " + event.name() + " on object " + lockKey);
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void deployCommitId(@ValidateStringParam(name = "site") String site,
                               @ValidateStringParam(name = "commitId") String commitId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        params.put("state", State.EXISTING_UNEDITED_UNLOCKED.name());
        itemStateMapper.deployCommitId(params);
    }

    @Override
    @ValidateParams
    public void insertNewEntry(@ValidateStringParam(name = "site") String site, ContentItemTO item) {
        String path = FilenameUtils.normalize(item.getUri(), true);
        String lockKey = site + ":" + path;
        generalLockService.lock(lockKey);
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", path);
            ItemState state = itemStateMapper.getObjectStateBySiteAndPath(params);
            if (state == null) {

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
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    @ValidateParams
    public void insertNewEntry(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "path") String path) {
        String itemPath = FilenameUtils.normalize(path, true);
        String lockKey = site + ":" + path;
        generalLockService.lock(lockKey);
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", itemPath);
            ItemState state = itemStateMapper.getObjectStateBySiteAndPath(params);
            if (state == null) {
                ItemState newEntry = new ItemState();
                newEntry.setObjectId(UUID.randomUUID().toString());

                newEntry.setSite(site);
                newEntry.setPath(itemPath);
                newEntry.setSystemProcessing(0);
                newEntry.setState(State.NEW_UNPUBLISHED_UNLOCKED.name());
                itemStateMapper.insertEntry(newEntry);
            }
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    @ValidateParams
    public List<ItemState> getSubmittedItems(@ValidateStringParam(name = "site") String site) {
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

    @RetryingOperation
    @Override
    @ValidateParams
    public void updateObjectPath(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "oldPath") String oldPath,
                                 @ValidateSecurePathParam(name = "newPath") String newPath) {
        oldPath = FilenameUtils.normalize(oldPath, true);
        newPath = FilenameUtils.normalize(newPath, true);
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("oldPath", oldPath);
        params.put("newPath", newPath);
        itemStateMapper.updateObjectPath(params);
    }

    @Override
    @ValidateParams
    public boolean isUpdated(@ValidateStringParam(name = "site") String site,
                             @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isUpdated(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public boolean isUpdatedOrNew(@ValidateStringParam(name = "site") String site,
                                  @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isUpdateOrNew(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public boolean isUpdatedOrSubmitted(@ValidateStringParam(name = "site") String site,
                                        @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isUpdateOrSubmitted(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public boolean isSubmitted(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isSubmitted(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public boolean isNew(@ValidateStringParam(name = "site") String site,
                         @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isNew(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public boolean isScheduled(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isScheduled(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public boolean isInWorkflow(@ValidateStringParam(name = "site") String site,
                                @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        ItemState state = getObjectState(site, path);
        if (state != null) {
            return State.isInWorkflow(State.valueOf(state.getState()));
        } else {
            return false;
        }
    }

    @Override
    @ValidateParams
    public List<ItemState> getChangeSet(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> statesValues = new ArrayList<String>();
        for (State state : State.IN_PROGRESS_STATES) {
            statesValues.add(state.name());
        }
        params.put("states", statesValues);
        params.put("site", site);
        List<ItemState> objects = itemStateMapper.getObjectStateByStates(params);
        return objects;
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void deleteObjectState(@ValidateStringParam(name = "objectId") String objectId) {
        itemStateMapper.deleteObjectState(objectId);
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void deleteObjectStateForPath(@ValidateStringParam(name = "site") String site,
                                         @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path);
        itemStateMapper.deleteObjectStateForSiteAndPath(params);
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void deleteObjectStatesForFolder(@ValidateStringParam(name = "site") String site,
                                            @ValidateSecurePathParam(name = "path") String path) {
        path = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path + "/%");
        itemStateMapper.deleteObjectStateForSiteAndFolder(params);
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void transitionBulk(@ValidateStringParam(name = "site") String site, List<String> paths,
                               TransitionEvent event, State defaultTargetState) {
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
                        logger.warn("Transition not defined for event " + event.name() + " and current state " +
                                entry.getKey().name() + " [setting object state for multiple objects]");
                    }
                }
            }
        }
    }
    
    /**
     * get the object for a given set of states
     */
    @Override
    @ValidateParams
    public List<ItemState> getObjectStateByStates(@ValidateStringParam(name = "site") String site, List<String> states) {

        if (states != null && !states.isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("states", states);
            params.put("site", site);
            List<ItemState> result = itemStateMapper.getObjectStateByStates(params);
            return result;
        } else {
            return new ArrayList<>(0);
        }
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public String setObjectState(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "path") String path,
                                 @ValidateStringParam(name = "state") String state, boolean systemProcessing) {
        path = FilenameUtils.normalize(path, true);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path);
        ItemState objectState = itemStateMapper.getObjectStateBySiteAndPath(params);
        if (objectState == null) {
            insertNewEntry(site, path);
            objectState = itemStateMapper.getObjectStateBySiteAndPath(params);
        }
        objectState.setState(state);
        objectState.setSystemProcessing(systemProcessing ? 1 : 0);
        itemStateMapper.setObjectState(objectState);
        return "Success";
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void deleteObjectStatesForSite(@ValidateStringParam(name = "site") String site) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        itemStateMapper.deleteObjectStatesForSite(params);
    }

    public int getBulkOperationBatchSize() {
        int toReturn = Integer.parseInt(studioConfiguration.getProperty(OBJECT_STATE_BULK_OPERATIONS_BATCH_SIZE));
        return toReturn;
    }

    @RetryingOperation
    @Override
    @ValidateParams
    public void setStateForSiteContent(@ValidateStringParam(name = "site") String site, State state) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", site);
        params.put("state", state.name());
        itemStateMapper.setStateForSiteContent(params);
    }

    @Override
    @ValidateParams
    public List<String> getChangeSetForSubtree(@ValidateStringParam(name = "site") String site,
                                               @ValidateSecurePathParam(name = "path") String path) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("likepath", path + (path.endsWith(FILE_SEPARATOR) ? "" : FILE_SEPARATOR) + "%");
        params.put("states", State.CHANGE_SET_STATES);
        List<ItemState> result = itemStateMapper.getChangeSetForSubtree(params);
        List<String> toRet = new ArrayList<String>();
        for (ItemState state : result) {
            toRet.add(state.getPath());
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean deletedPathExists(@ValidateStringParam(name = "site") String site,
                                     @ValidateSecurePathParam(name = "path") String path) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("path", path);
        params.put("states", State.DELETED_STATES);
        return itemStateMapper.deletedPathExists(params) > 0;
    }

    private void initializeTransitionTable() {
        transitionTable = new State[][]{
                {State.NEW_DELETED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NOOP,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
                {State.NEW_DELETED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_DELETED,State.NEW_UNPUBLISHED_UNLOCKED,State.NEW_UNPUBLISHED_LOCKED,State.NEW_SUBMITTED_WITH_WF_SCHEDULED,State.NEW_SUBMITTED_WITH_WF_UNSCHEDULED,State.NEW_SUBMITTED_NO_WF_SCHEDULED,State.NEW_SUBMITTED_NO_WF_UNSCHEDULED,State.NOOP,State.NEW_UNPUBLISHED_UNLOCKED,State.EXISTING_UNEDITED_UNLOCKED,State.NEW_PUBLISHING_FAILED},
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


    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ItemStateMapper getItemStateMapper() {
        return itemStateMapper;
    }

    public void setItemStateMapper(ItemStateMapper itemStateMapper) {
        this.itemStateMapper = itemStateMapper;
    }

    public RetryingOperationFacade getRetryingOperationFacade() {
        return retryingOperationFacade;
    }

    public void setRetryingOperationFacade(RetryingOperationFacade retryingOperationFacade) {
        this.retryingOperationFacade = retryingOperationFacade;
    }
}
