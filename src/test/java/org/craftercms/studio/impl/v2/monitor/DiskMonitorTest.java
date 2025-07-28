package org.craftercms.studio.impl.v2.monitor;

import org.craftercms.commons.monitoring.DiskInfo;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.repository.job.RepositoryCleanupJob;
import org.craftercms.studio.model.rest.monitoring.DiskStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static java.time.Instant.now;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DISK_MONITOR_HIGH_WATER_MARK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DISK_MONITOR_LOW_WATER_MARK;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DiskMonitorTest {

	@Mock
	protected StudioConfiguration studioConfiguration;
	@Mock
	protected RepositoryCleanupJob gitGCJob;

	@Spy
	@InjectMocks
	protected DiskMonitor diskMonitor;

	@Test
	public void noPreviousAlarmHighUsageTest() {
		doReturn(95).when(studioConfiguration).getProperty(DISK_MONITOR_HIGH_WATER_MARK, Integer.class);
		doReturn(85).when(studioConfiguration).getProperty(DISK_MONITOR_LOW_WATER_MARK, Integer.class);

		DiskInfo mockInfo = mock(DiskInfo.class);
		doReturn(96).when(mockInfo).getDiskUsage();
		doReturn(mockInfo).when(diskMonitor).getDiskInfo();

		diskMonitor.checkDiskUsage();

		verify(gitGCJob).cleanupAllRepositories();

		verify(diskMonitor, times(1).description("Notification should be sent when disk usage is in alarm state"))
				.sendAlarm();

		assertTrue(diskMonitor.getDiskStatus().isAlarm(), "Disk status should be in alarm state due to high usage");
	}

	@Test
	public void noPreviousAlarmMiddleUsageTest() {
		doReturn(95).when(studioConfiguration).getProperty(DISK_MONITOR_HIGH_WATER_MARK, Integer.class);
		doReturn(85).when(studioConfiguration).getProperty(DISK_MONITOR_LOW_WATER_MARK, Integer.class);

		DiskInfo mockInfo = mock(DiskInfo.class);
		doReturn(90).when(mockInfo).getDiskUsage();
		doReturn(mockInfo).when(diskMonitor).getDiskInfo();

		diskMonitor.checkDiskUsage();

		verify(gitGCJob, never().description("Repositories should only be gc'ed the first time alarm is raised"))
				.cleanupAllRepositories();

		verify(diskMonitor, never().description("Notification should not be sent when disk usage is not in alarm state"))
				.sendAlarm();

		assertFalse(diskMonitor.getDiskStatus().isAlarm(), "Disk status should only be in alarm state after reaching high water mark");
	}

	@Test
	public void previousAlarmLowUsageTest() {
		doReturn(95).when(studioConfiguration).getProperty(DISK_MONITOR_HIGH_WATER_MARK, Integer.class);
		doReturn(85).when(studioConfiguration).getProperty(DISK_MONITOR_LOW_WATER_MARK, Integer.class);

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

		assertFalse(diskMonitor.getDiskStatus().isAlarm(), "Disk status should not be in alarm state due to low usage");
	}

	@Test
	public void previousAlarmMiddleUsageTest() {
		doReturn(95).when(studioConfiguration).getProperty(DISK_MONITOR_HIGH_WATER_MARK, Integer.class);
		doReturn(85).when(studioConfiguration).getProperty(DISK_MONITOR_LOW_WATER_MARK, Integer.class);

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

		assertTrue(diskMonitor.getDiskStatus().isAlarm(), "Disk status should be kept in alarm state if above the low watermark");
	}

	@Test
	public void previousAlarmHighUsageTest() {
		doReturn(95).when(studioConfiguration).getProperty(DISK_MONITOR_HIGH_WATER_MARK, Integer.class);
		doReturn(85).when(studioConfiguration).getProperty(DISK_MONITOR_LOW_WATER_MARK, Integer.class);

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

		assertTrue(diskMonitor.getDiskStatus().isAlarm(), "Disk status should be kept in alarm state if above the low watermark");
	}
}
