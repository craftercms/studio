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


import java.util.Arrays;
import java.util.List;

public interface ObjectStateService {

    public enum State {

        NEW_UNPUBLISHED_LOCKED("New Unpublished Locked"),
        NEW_UNPUBLISHED_UNLOCKED("New Unpublished Unlocked"),
        NEW_SUBMITTED_WITH_WF_SCHEDULED("New Submitted with Workflow Scheduled"),
        NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED("New Submitted with Workflow Scheduled Locked"),
        NEW_SUBMITTED_WITH_WF_UNSCHEDULED("New Submitted with Workflow Unscheduled"),
        NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED("New Submitted with Workflow Unscheduled Locked"),
        NEW_SUBMITTED_NO_WF_SCHEDULED("New Submitted without Workflow Scheduled"),
        NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED("New Submitted without Workflow Scheduled Locked"),
        NEW_SUBMITTED_NO_WF_UNSCHEDULED("New Submitted without Workflow Unscheduled"),
        NEW_PUBLISHING_FAILED("New Publishing Failed"),
        NEW_DELETED("New Deleted"),
        EXISTING_UNEDITED_LOCKED("Existing Unedited Locked"),
        EXISTING_UNEDITED_UNLOCKED("Existing Unedited Unlocked"),
        EXISTING_EDITED_LOCKED("Existing Edited Locked"),
        EXISTING_EDITED_UNLOCKED("Existing Edited Unlocked"),
        EXISTING_SUBMITTED_WITH_WF_SCHEDULED("Existing Submitted with Workflow Scheduled"),
        EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED("Existing Submitted with Workflow Scheduled Locked"),
        EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED("Existing Submitted with Workflow Unscheduled"),
        EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED("Existing Submitted with Workflow Unscheduled Locked"),
        EXISTING_SUBMITTED_NO_WF_SCHEDULED("Existing Submitted without Workflow Scheduled"),
        EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED("Existing Submitted without Workflow Scheduled Locked"),
        EXISTING_SUBMITTED_NO_WF_UNSCHEDULED("Existing Submitted without Workflow Unscheduled"),
        EXISTING_PUBLISHING_FAILED("Existing Publishing Failed"),
        EXISTING_DELETED("Existing Deleted"),

        SYSTEM_PROCESSING("System Processing"),
        NOOP("INVALID STATE");

        private final String label;

        State(String label) {
            this.label = label;
        }


        @Override
        public String toString() {
            return this.label;
        }

        public static final List<State> NEW_STATES = Arrays.asList(
                NEW_UNPUBLISHED_LOCKED,
                NEW_UNPUBLISHED_UNLOCKED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_SCHEDULED,
                NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_UNSCHEDULED,
                NEW_PUBLISHING_FAILED,
                NEW_DELETED
        );

        public static final List<State> DELETED_STATES = Arrays.asList(
                NEW_DELETED, EXISTING_DELETED
        );

        public static final List<State> EDITED_STATES = Arrays.asList(
                EXISTING_EDITED_LOCKED, EXISTING_EDITED_UNLOCKED
        );

        public static final List<State> SUBMITTED_STATES = Arrays.asList(
                NEW_SUBMITTED_WITH_WF_SCHEDULED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED
        );

        public static final List<State> SCHEDULED_STATES = Arrays.asList(
                NEW_SUBMITTED_WITH_WF_SCHEDULED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_SCHEDULED,
                NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_NO_WF_SCHEDULED,
                EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED
        );

        public static final List<State> LOCKED_STATES = Arrays.asList(
                NEW_UNPUBLISHED_LOCKED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED,
                EXISTING_UNEDITED_LOCKED,
                EXISTING_EDITED_LOCKED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED
        );

        public static final List<State> CHANGE_SET_STATES = Arrays.asList(
                NEW_UNPUBLISHED_LOCKED,
                NEW_UNPUBLISHED_UNLOCKED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_SCHEDULED,
                NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_UNSCHEDULED,
                NEW_PUBLISHING_FAILED,
                NEW_DELETED,
                EXISTING_EDITED_LOCKED,
                EXISTING_EDITED_UNLOCKED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                EXISTING_SUBMITTED_NO_WF_SCHEDULED,
                EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_NO_WF_UNSCHEDULED,
                EXISTING_PUBLISHING_FAILED,
                EXISTING_DELETED
        );

        public static final List<State> LIVE_STATES = Arrays.asList(
                EXISTING_UNEDITED_LOCKED, EXISTING_UNEDITED_UNLOCKED, NEW_DELETED
        );

        public static final List<State> PUBLISHING_FAILED = Arrays.asList(
                NEW_PUBLISHING_FAILED, EXISTING_PUBLISHING_FAILED
        );

        public static final List<State> WORKFLOW_STATES = Arrays.asList(
                NEW_SUBMITTED_WITH_WF_SCHEDULED,
                NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED,
                NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_SCHEDULED,
                NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED,
                NEW_SUBMITTED_NO_WF_UNSCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,
                EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
                EXISTING_SUBMITTED_NO_WF_SCHEDULED,
                EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED,
                EXISTING_SUBMITTED_NO_WF_UNSCHEDULED
        );

        public static final boolean isNew(State state) {
            return NEW_STATES.contains(state);
        }

        public static boolean isDeleted(State state) {
            return DELETED_STATES.contains(state);
        }

        public static boolean isUpdateOrNew(State state) {
            return EDITED_STATES.contains(state) || NEW_STATES.contains(state);
        }

        public static boolean isSubmitted(State state) {
            return SUBMITTED_STATES.contains(state);
        }

        public static boolean isScheduled(State state) {
            return SCHEDULED_STATES.contains(state);
        }

        public static boolean isLocked(State state) {
            return LOCKED_STATES.contains(state);
        }

        public static boolean isLive(State state) {
            return LIVE_STATES.contains(state);
        }

        public static boolean isSubmittedForDeletion(State state) {
            return DELETED_STATES.contains(state);
        }

        public static boolean isSystemProcessing(State state) {
            return SYSTEM_PROCESSING.equals(state);
        }

        public static boolean isPublishingFailed(State state) {
            return PUBLISHING_FAILED.contains(state);
        }

        public static boolean isInWorkflow(State state) {
            return WORKFLOW_STATES.contains(state);
        }
    }

    public enum TransitionEvent {

        DELETE,
        EDIT,
        SAVE,
        CANCEL_EDIT,
        SAVE_FOR_PREVIEW,
        REVERT,
        UNLOCK,
        CUT,
        PASTE,
        DUPLICATE,
        SUBMIT_WITH_WORKFLOW_SCHEDULED,
        SUBMIT_WITH_WORKFLOW_UNSCHEDULED,
        SUBMIT_WITHOUT_WORKFLOW_SCHEDULED,
        SUBMIT_WITHOUT_WORKFLOW_UNSCHEDULED,
        APPROVE,
        REJECT,
        DEPLOYMENT,
        DEPLOYMENT_FAILED;

        public static List<TransitionEvent> cacheInvalidateEvents = Arrays.asList(
                DELETE, SAVE, SAVE_FOR_PREVIEW, REVERT, PASTE, DUPLICATE, DEPLOYMENT
        );

        public static boolean isCacheInvalidateNeeded(TransitionEvent event) {
            return cacheInvalidateEvents.contains(event);
        }
    }

/*
    public void beginSystemProcessing(String fullPath);

    public void beginSystemProcessing(NodeRef nodeRef);

    public void endSystemProcessing(String fullPath);

    public void endSystemProcessing(NodeRef nodeRef);

    public State getObjectState(String fullPath);

    public State getObjectState(NodeRef nodeRef);

    public State getRealObjectState(NodeRef nodeRef);

    public void transition(String fullPath, TransitionEvent event);

    public void transition(NodeRef nodeRef, TransitionEvent event);

    public void insertNewObjectEntry(String fullPath);

    public void insertNewObjectEntry(NodeRef nodeRef);

    public List<NodeRef> getSubmittedItems(String site);

    public void setSystemProcessing(String fullPath, boolean isSystemProcessing);

    public void setSystemProcessing(NodeRef nodeRef, boolean isSystemProcessing);

    public void setSystemProcessingBulk(List<String> objectIds, boolean isSystemProcessing);

    public void updateObjectPath(String fullPath, String newPath);

    public void updateObjectPath(NodeRef nodeRef, String newPath);

    public boolean isUpdatedOrNew(String path);

    public boolean isUpdatedOrNew(NodeRef nodeRef);

    public State[][] getTransitionMapping();

    public boolean isNew(String fullPath);

    public boolean isNew(NodeRef nodeRef);

    public boolean isFolderLive(String fullPath);

    public List<NodeRef> getChangeSet(String site);

    public void deleteObjectState(String objectId);

    public void deleteObjectStateForPath(String site, String path);

    public void deleteObjectStateForPaths(String site, List<String> paths);

    public void transitionBulk(List<String> objectIds, TransitionEvent event, State defaultTargetState);
 */
    /**
     * get the object for a given set of states
     */
    /*
    List<ObjectStateTO> getObjectStateByStates(String site, List<State> states);

    public boolean isScheduled(String path);

    public boolean isScheduled(NodeRef nodeRef);

    public boolean isInWorkflow(String path);

    public boolean isInWorkflow(NodeRef nodeRef);
    */
}
