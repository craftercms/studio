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

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.task.TaskId;
import org.craftercms.studio.api.v2.task.TaskManager;
import org.craftercms.studio.api.v2.task.TaskProgress;
import org.craftercms.studio.model.task.PublishTask;
import org.craftercms.studio.model.task.SiteTask;
import org.craftercms.studio.model.task.Task;
import org.jspecify.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskManagerImplTest {

	private static final String SITE_1_ID = "site1";
	private static final String SITE_2_ID = "site2";

	TaskProgress<PublishTask.PublishTaskId, Long> publishTaskProgressSite1;
	TaskProgress<PublishTask.PublishTaskId, Long> publishTaskProgressSite1_2;
	TaskProgress<PublishTask.PublishTaskId, Long> publishTaskProgressSite2;
	TaskProgress<?, ?> testTaskProgressSite1;

	@Mock
	ApplicationContext applicationContext;

	@Spy
	@InjectMocks
	TaskManagerImpl taskManager;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws SiteNotFoundException {

		when(applicationContext.getBean(eq(TaskProgressImpl.class), any(Object[].class)))
				.thenAnswer(invocation -> {
					TaskProgressImpl<?, ?> taskProgress = new TaskProgressImpl<TaskId, Object>(invocation.getArgument(1, Task.class), invocation.getArgument(2, TaskManager.class));
					taskProgress.setApplicationEventPublisher(applicationContext);

					return taskProgress;
				});

		PublishTask publishTaskSite1 = new PublishTask(SITE_1_ID, 123);
		PublishTask publishTaskSite1_2 = new PublishTask(SITE_1_ID, 456);
		TestTask testTaskSite1 = new TestTask(SITE_1_ID, 234);
		PublishTask publishTaskSite2 = new PublishTask(SITE_2_ID, 789);
		publishTaskProgressSite1 = taskManager.registerTask(publishTaskSite1);
		publishTaskProgressSite1_2 = taskManager.registerTask(publishTaskSite1_2);
		testTaskProgressSite1 = taskManager.registerTask(testTaskSite1);
		publishTaskProgressSite2 = taskManager.registerTask(publishTaskSite2);
	}

	@Test
	public void getSiteTasksTest() {
		Collection<TaskProgress<? extends TaskId.SiteTaskId, ?>> siteTasks = taskManager.getSiteTasks(SITE_1_ID);
		assertEquals(3, siteTasks.size());
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(publishTaskProgressSite1.getTask())));
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(publishTaskProgressSite1_2.getTask())));
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(testTaskProgressSite1.getTask())));
		assertTrue(siteTasks.stream().noneMatch(p -> p.getTask().equals(publishTaskProgressSite2.getTask())));
	}

	@Test
	public void getTasksByTypeTest() {
		Collection<TaskProgress<TaskId.SiteTaskId, Object>> siteTasks = taskManager.getSiteTasksByType(SITE_1_ID, PublishTask.PUBLISH_TASK_TYPE);
		assertEquals(2, siteTasks.size());
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(publishTaskProgressSite1.getTask())));
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(publishTaskProgressSite1_2.getTask())));
		assertTrue(siteTasks.stream().noneMatch(p -> p.getTask().equals(publishTaskProgressSite2.getTask())));
		assertTrue(siteTasks.stream().noneMatch(p -> p.getTask().equals(testTaskProgressSite1.getTask())));
	}

	@Test
	public void removeTaskTest() {
		Collection<TaskProgress<? extends TaskId.SiteTaskId, ?>> siteTasks = taskManager.getSiteTasks(SITE_1_ID);
		assertEquals(3, siteTasks.size());
		taskManager.removeTask(publishTaskProgressSite1.getTask().getTaskId());
		siteTasks = taskManager.getSiteTasks(SITE_1_ID);
		assertEquals(2, siteTasks.size());
		assertTrue(siteTasks.stream().noneMatch(p -> p.getTask().equals(publishTaskProgressSite1.getTask())));
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(publishTaskProgressSite1_2.getTask())));
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(testTaskProgressSite1.getTask())));
		assertTrue(siteTasks.stream().noneMatch(p -> p.getTask().equals(publishTaskProgressSite2.getTask())));
	}

	@Test
	public void taskCompletedTest() {
		Collection<TaskProgress<? extends TaskId.SiteTaskId, ?>> siteTasks = taskManager.getSiteTasks(SITE_1_ID);
		assertEquals(3, siteTasks.size());
		publishTaskProgressSite1.complete(PublishPackage.PackageState.LIVE_SUCCESS.value);
		siteTasks = taskManager.getSiteTasks(SITE_1_ID);
		assertEquals(2, siteTasks.size());
		assertTrue(siteTasks.stream().noneMatch(p -> p.getTask().equals(publishTaskProgressSite1.getTask())));
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(publishTaskProgressSite1_2.getTask())));
		assertTrue(siteTasks.stream().anyMatch(p -> p.getTask().equals(testTaskProgressSite1.getTask())));
		assertTrue(siteTasks.stream().noneMatch(p -> p.getTask().equals(publishTaskProgressSite2.getTask())));
	}

	static class TestTask extends SiteTask<TestTask.TestTaskId> {

		final static String TYPE = "testTask";

		public TestTask(String siteId, int taskId) {
			super(TYPE, new TestTaskId(siteId, taskId));
		}

		@Override
		@NonNull
		public String getType() {
			return TYPE;
		}

		record TestTaskId(String siteId, int taskId) implements TaskId.SiteTaskId {
			@Override
			public String getSiteId() {
				return siteId;
			}
		}
	}
}
