/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.task;

import org.craftercms.studio.model.task.Task;
import org.craftercms.studio.model.task.TaskState;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.SequencedCollection;

/**
 * Represent the progress of a task
 *
 * @param <K> type of the task id
 */
public interface TaskProgress<K extends TaskId, R> {

	/**
	 * Get the task stages
	 *
	 * @return the task stages collection
	 */
	SequencedCollection<Stage> getStages();

	/**
	 * Start a new stage with the total number of items to process
	 *
	 * @param name  the stage name
	 * @param total total number of items to process
	 * @return the stage
	 */
	Stage startStage(String name, int total);

	/**
	 * Start a new stage with a default item count of 1
	 *
	 * @param name the stage name
	 * @return the stage
	 */
	default Stage startStage(String name) {
		return startStage(name, 1);
	}

	/**
	 * Mark the task as completed
	 */
	void complete(R result);

	/**
	 * Mark the task as completed and set an error message
	 */
	void completeWithErrors(R result, String errorMessage);

	/**
	 * Mark the task as started
	 */
	void start();

	/**
	 * Get the task state
	 *
	 * @return the task state
	 */
	TaskState getState();

	/**
	 * Get the start time of the task progress
	 *
	 * @return the start time, or null if the task is not started
	 */
	Instant getStartTime();

	/**
	 * Get the result of the task
	 *
	 * @return the task result, or null if the task is not completed
	 */
	TaskResult<R> getResult();

	/**
	 * Get the end time of the task progress
	 *
	 * @return the end time, or null if the task is not completed
	 */
	Instant getEndTime();

	/**
	 * Get the actual task
	 *
	 * @return the task
	 */
	@NonNull
	Task<K> getTask();

	/**
	 * Represent the result of a task and an optional message in case of failure
	 */
	record TaskResult<R>(boolean success, String message, R result) {
	}

	/**
	 * Represent a stage of a complex task
	 */
	interface Stage {

		/**
		 * Indicate if there has been errors in this stage
		 *
		 * @return true if there has been errors
		 */
		boolean getErrors();

		/**
		 * Set the stage as having errors
		 */
		void setErrors();

		/**
		 * Get the stage name
		 *
		 * @return the stage name
		 */
		String getName();

		/**
		 * Get the number of items processed so far
		 *
		 * @return the number of items processed
		 */
		int getProcessed();

		/**
		 * Increase the number of processed items
		 */
		default void advanceOne() {
			advance(1);
		}

		/**
		 * Increase the number of processed items by a given amount
		 *
		 * @param increase the amount to increase
		 */
		void advance(int increase);

		/**
		 * Mark the stage as started
		 *
		 * @return the stage
		 */
		Stage start();

		/**
		 * Mark the stage as completed
		 */
		void complete();

		/**
		 * Get the stage state
		 *
		 * @return the stage state
		 */
		TaskState getState();

		/**
		 * Set the stage state
		 *
		 * @param state
		 */
		void setState(TaskState state);

		/**
		 * Get the total number of items to process in this stage
		 *
		 * @return the total number of items
		 */
		int getTotal();

		/**
		 * Set the total number of items to process in this stage
		 *
		 * @param total the total number of items
		 * @return the stage
		 */
		Stage setTotal(int total);
	}
}
