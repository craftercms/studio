/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.task;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.craftercms.studio.api.v2.event.task.TaskEvent;
import org.craftercms.studio.api.v2.task.TaskId;
import org.craftercms.studio.api.v2.task.TaskManager;
import org.craftercms.studio.api.v2.task.TaskProgress;
import org.craftercms.studio.model.task.Task;
import org.craftercms.studio.model.task.TaskState;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.time.Instant;
import java.util.LinkedList;
import java.util.SequencedCollection;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedList;
import static java.util.Collections.unmodifiableSequencedCollection;
import static org.craftercms.studio.api.v2.event.task.TaskEvent.*;

/**
 * Default {@link TaskProgress} implementation that will publish events when the task state changes
 *
 * @param <K> the type of the task id
 */
class TaskProgressImpl<K extends TaskId, R> implements TaskProgress<K, R>, ApplicationEventPublisherAware {

	private final SequencedCollection<Stage> stages = synchronizedList(new LinkedList<>());
	private TaskState state = TaskState.READY;
	private final Task<K> task;
	private final TaskManager taskManager;
	private Instant startTime;
	private Instant endTime;
	private TaskResult<R> taskResult;

	private ApplicationEventPublisher eventPublisher;

	public TaskProgressImpl(final Task<K> task, final TaskManager taskManager) {
		this.task = task;
		this.taskManager = taskManager;
	}

	@Override
	public SequencedCollection<TaskProgress.Stage> getStages() {
		return unmodifiableSequencedCollection(stages);
	}

	@Override
	public Stage startStage(final String name, final int total) {
		Stage stage = new StageImpl(name);
		stage.setTotal(total);
		stage.start();
		stages.add(stage);
		notifyProgress();
		return stage;
	}

	@Override
	public void complete(final R result) {
		complete(true, result, null);
	}

	@Override
	public void completeWithErrors(final R result, final String errorMessage) {
		complete(false, result, errorMessage);
	}

	/**
	 * Mark the task as completed
	 *
	 * @param success      true if the task was successful
	 * @param errorMessage the error message if the task failed
	 */
	private void complete(final boolean success, final R result, final String errorMessage) {
		state = TaskState.COMPLETED;
		endTime = Instant.now();
		taskResult = new TaskResult<>(success, errorMessage, result);
		notifyComplete();
		taskManager.removeTask(getTask().getTaskId());
	}

	@Override
	public void start() {
		state = TaskState.IN_PROGRESS;
		startTime = Instant.now();
		notifyStart();
	}

	@Override
	public TaskState getState() {
		return state;
	}

	@Override
	public Instant getEndTime() {
		return endTime;
	}

	@Override
	public Instant getStartTime() {
		return startTime;
	}

	@Override
	public TaskResult<R> getResult() {
		return taskResult;
	}

	@Override
	@JsonUnwrapped
	@NonNull
	public Task<K> getTask() {
		return task;
	}

	private void notifyStart() {
		eventPublisher.publishEvent(new TaskEvent(this, EVENT_TYPE_TASK_STARTED));
	}

	private void notifyProgress() {
		eventPublisher.publishEvent(new TaskEvent(this, EVENT_TYPE_TASK_PROGRESS));
	}

	private void notifyComplete() {
		eventPublisher.publishEvent(new TaskEvent(this, EVENT_TYPE_TASK_COMPLETED));
	}

	@Override
	public void setApplicationEventPublisher(@NonNull final ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}

	@Override
	public String toString() {
		return """
			TaskProgressImpl {task=%s, startTime="%s", endTime="%s", state=%s, result=%s, stages=%s}"""
			.formatted(task, startTime, endTime, state, taskResult, stages);
	}

	/**
	 * Default {@link TaskProgress.Stage} implementation
	 */
	private class StageImpl implements TaskProgress.Stage {
		private final String name;
		private int total;
		private final AtomicInteger processed = new AtomicInteger(0);
		private TaskState state = TaskState.READY;
		private boolean errors;

		public StageImpl(String name) {
			this.name = name;
		}

		@Override
		public boolean getErrors() {
			return errors;
		}

		@Override
		public void setErrors() {
			this.errors = true;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getProcessed() {
			return processed.get();
		}

		@Override
		public void advance(final int increase) {
			this.processed.addAndGet(increase);
			notifyProgress();
		}

		@Override
		public Stage start() {
			state = TaskState.IN_PROGRESS;
			notifyProgress();
			return this;
		}

		@Override
		public void complete() {
			this.processed.set(total);
			this.state = TaskState.COMPLETED;
			notifyProgress();
		}

		@Override
		public TaskState getState() {
			return state;
		}

		@Override
		public void setState(final TaskState state) {
			this.state = state;
			notifyProgress();
		}

		@Override
		public int getTotal() {
			return total;
		}

		@Override
		public Stage setTotal(int total) {
			this.total = total;
			return this;
		}

		@Override
		public String toString() {
			return """
				StageImpl {name="%s", state=%s, total=%s, processed=%s, errors=%s}"""
				.formatted(name, state, total, processed, errors);
		}
	}
}
