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
package org.craftercms.studio.impl.v2.monitor;

import org.craftercms.commons.monitoring.DiskInfo;
import org.craftercms.studio.impl.v1.repository.job.RepositoryCleanupJob;
import org.craftercms.studio.model.rest.monitoring.DiskStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.time.Instant.now;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DiskMonitorTest {

	@Mock
	protected RepositoryCleanupJob gitGCJob;

	protected DiskMonitor getDiskMonitor(int lowWaterMark, int highWaterMark) {
		return getDiskMonitor(lowWaterMark, highWaterMark, true);
	}

	protected DiskMonitor getDiskMonitor(int lowWaterMark, int highWaterMark, boolean systemReady) {
		DiskMonitor spy = spy(new DiskMonitor(gitGCJob, null, () -> systemReady, ".", lowWaterMark, highWaterMark));
		spy.afterPropertiesSet();
		return spy;
	}

	@Test
	public void outOfRangeLimitsTest() {
		assertThrows("Expected IllegalArgumentException for low watermark less than 0",
				IllegalArgumentException.class,
				() ->
						getDiskMonitor(-1, 95));
		assertThrows("Expected IllegalArgumentException for high watermark less than 0",
				IllegalArgumentException.class,
				() -> getDiskMonitor(14, -23));
		assertThrows("Expected IllegalArgumentException for high watermark less than low watermark",
				IllegalArgumentException.class,
				() -> getDiskMonitor(90, 75));
	}

	@Test
	public void noPreviousAlarmHighUsageTest() {
		DiskMonitor diskMonitor = getDiskMonitor(85, 95);

		DiskInfo mockInfo = mock(DiskInfo.class);
		doReturn(96).when(mockInfo).getDiskUsage();
		doReturn(mockInfo).when(diskMonitor).getDiskInfo();

		diskMonitor.checkDiskUsage();

		verify(gitGCJob).cleanupAllRepositories();

		verify(diskMonitor, times(1).description("Notification should be sent when disk usage is in alarm state"))
				.sendAlarm();

		assertTrue("Disk status should be in alarm state due to high usage", diskMonitor.getDiskStatus().isAlarm());
	}

	@Test
	public void noPreviousAlarmMiddleUsageTest() {
		DiskMonitor diskMonitor = getDiskMonitor(85, 95);

		DiskInfo mockInfo = mock(DiskInfo.class);
		doReturn(90).when(mockInfo).getDiskUsage();
		doReturn(mockInfo).when(diskMonitor).getDiskInfo();

		diskMonitor.checkDiskUsage();

		verify(gitGCJob, never().description("Repositories should only be gc'ed the first time alarm is raised"))
				.cleanupAllRepositories();

		verify(diskMonitor, never().description("Notification should not be sent when disk usage is not in alarm state"))
				.sendAlarm();

		assertFalse("Disk status should only be in alarm state after reaching high water mark", diskMonitor.getDiskStatus().isAlarm());
	}

	@Test
	public void previousAlarmLowUsageTest() {
		DiskMonitor diskMonitor = getDiskMonitor(85, 95);

		DiskInfo previousMockInfo = mock(DiskInfo.class);
		diskMonitor.diskStatus = new DiskStatus(
				previousMockInfo,
				95,
				85,
				true, // previous alarm state
				null,
				now(),
				null
		);

		DiskInfo mockInfo = mock(DiskInfo.class);
		doReturn(80).when(mockInfo).getDiskUsage();
		doReturn(mockInfo).when(diskMonitor).getDiskInfo();

		diskMonitor.checkDiskUsage();

		verify(gitGCJob, never().description("Repositories should only be gc'ed the first time alarm is raised"))
				.cleanupAllRepositories();
		verify(diskMonitor, never().description("Notification should not be sent when disk usage is not in alarm state"))
				.sendAlarm();

		assertFalse("Disk status should not be in alarm state due to low usage", diskMonitor.getDiskStatus().isAlarm());
	}

	@Test
	public void previousAlarmMiddleUsageTest() {
		DiskMonitor diskMonitor = getDiskMonitor(85, 95);

		DiskInfo previousMockInfo = mock(DiskInfo.class);
		diskMonitor.diskStatus = new DiskStatus(
				previousMockInfo,
				95,
				85,
				true, // previous alarm state
				null,
				now(),
				null
		);

		DiskInfo mockInfo = mock(DiskInfo.class);
		doReturn(88).when(mockInfo).getDiskUsage();
		doReturn(mockInfo).when(diskMonitor).getDiskInfo();

		diskMonitor.checkDiskUsage();

		verify(gitGCJob, never().description("Repositories should only be gc'ed the first time alarm is raised"))
				.cleanupAllRepositories();
		verify(diskMonitor, times(1).description("Notification should be sent when disk usage is in alarm state"))
				.sendAlarm();

		assertTrue("Disk status should be kept in alarm state if above the low watermark", diskMonitor.getDiskStatus().isAlarm());
	}

	@Test
	public void previousAlarmHighUsageTest() {
		DiskMonitor diskMonitor = getDiskMonitor(85, 95);

		DiskInfo previousMockInfo = mock(DiskInfo.class);
		diskMonitor.diskStatus = new DiskStatus(
				previousMockInfo,
				95,
				85,
				true, // previous alarm state
				null,
				now(),
				null
		);

		DiskInfo mockInfo = mock(DiskInfo.class);
		doReturn(99).when(mockInfo).getDiskUsage();
		doReturn(mockInfo).when(diskMonitor).getDiskInfo();

		diskMonitor.checkDiskUsage();

		verify(gitGCJob, never().description("Repositories should only be gc'ed the first time alarm is raised"))
				.cleanupAllRepositories();
		verify(diskMonitor, times(1).description("Notification should be sent when disk usage is in alarm state"))
				.sendAlarm();

		assertTrue("Disk status should be kept in alarm state if above the low watermark", diskMonitor.getDiskStatus().isAlarm());
	}

	@Test
	public void systemNotReadyTest() {
		DiskMonitor diskMonitor = getDiskMonitor(85, 95, false);
		diskMonitor.checkDiskUsage();

		verify(diskMonitor, never().description("Monitor should not try to calculate the status if the system is not ready yet")).calculateDiskStatus();
	}

	@Test
	public void getDiskInfoWhenSystemNotReadyTest() {
		DiskMonitor diskMonitor = getDiskMonitor(85, 95, false);

		DiskInfo result = diskMonitor.getDiskInfo();

		assertNull("getDiskInfo should return null when system is not ready", result);
	}

}
