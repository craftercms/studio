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

package org.craftercms.studio.api.v1.service.objectstate;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dejan Brkic
 */
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

    public static final List<State> IN_PROGRESS_STATES = Arrays.asList(
            NEW_UNPUBLISHED_LOCKED,
            NEW_UNPUBLISHED_UNLOCKED,
            NEW_SUBMITTED_WITH_WF_SCHEDULED,
            NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
            NEW_SUBMITTED_WITH_WF_UNSCHEDULED,
            NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
            NEW_PUBLISHING_FAILED,
            NEW_DELETED,
            EXISTING_EDITED_LOCKED,
            EXISTING_EDITED_UNLOCKED,
            EXISTING_SUBMITTED_WITH_WF_SCHEDULED,
            EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED,
            EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED,
            EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED,
            EXISTING_PUBLISHING_FAILED,
            EXISTING_DELETED
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

    public static boolean isUpdated(State state) {
        return EDITED_STATES.contains(state);
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

    public static boolean isUpdateOrSubmitted(State state) {
        return EDITED_STATES.contains(state) || SUBMITTED_STATES.contains(state);
    }
}
