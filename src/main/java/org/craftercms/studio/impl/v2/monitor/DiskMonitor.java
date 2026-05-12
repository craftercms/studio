/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.FileUtils;
import org.craftercms.commons.monitoring.DiskInfo;
import org.craftercms.studio.api.v2.exception.repository.RepositoryException;
import org.craftercms.studio.api.v2.notification.StudioNotificationSender;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.spring.context.SystemStatusProvider;
import org.craftercms.studio.model.rest.monitoring.DiskStatus;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.beans.ConstructorProperties;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.time.Instant.now;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Job for monitoring disk usage.
 */
public class DiskMonitor implements InitializingBean {
	private static final Logger logger = getLogger(DiskMonitor.class);
	private static final String SERVER_NAME_MODEL_KEY = "serverName";
	private static final String FORMAT_SIZE_MODEL_KEY = "byteCountToDisplaySize";

	private final SitesService siteService;
	private final StudioNotificationSender notificationSender;
	private final SystemStatusProvider systemStatusProvider;
	private final int lowWaterMark;
	private final int highWaterMark;
	private final String baseRepoPath;
	private String serverName;

	protected volatile DiskStatus diskStatus;

	@ConstructorProperties({"siteService", "notificationSender", "systemStatusProvider",
			"baseRepoPath", "lowWaterMark", "highWaterMark"})
	public DiskMonitor(SitesService siteService, StudioNotificationSender notificationSender,
					   SystemStatusProvider systemStatusProvider,
					   String baseRepoPath, int lowWaterMark, int highWaterMark) {
		this.siteService = siteService;
		this.notificationSender = notificationSender;
		this.systemStatusProvider = systemStatusProvider;
		this.baseRepoPath = baseRepoPath;
		this.lowWaterMark = lowWaterMark;
		this.highWaterMark = highWaterMark;
	}

	@Override
	public void afterPropertiesSet() {
		logger.info("Initializing DiskMonitor...");
		if (lowWaterMark < 0 || highWaterMark < 0) {
			throw new IllegalArgumentException("Watermarks must be non-negative: lowWaterMark = " + lowWaterMark + ", highWaterMark = " + highWaterMark);
		}
		if (lowWaterMark > 100 || highWaterMark > 100) {
			throw new IllegalArgumentException("Watermarks must be between 0 and 100: lowWaterMark = " + lowWaterMark + ", highWaterMark = " + highWaterMark);
		}
		if (lowWaterMark >= highWaterMark) {
			throw new IllegalArgumentException("Invalid configuration: highWaterMark (" + highWaterMark + ") must be greater than lowWaterMark (" + lowWaterMark + ")");
		}

		diskStatus = new DiskStatus(
				getDiskInfo(),
				highWaterMark,
				lowWaterMark,
				false,
				null,
				now(),
				null
		);

		logger.info("DiskMonitor initialized successfully.");
	}

	@SuppressWarnings("unused")
	public void checkDiskUsage() {
		if (!systemStatusProvider.isSystemReady()) {
			logger.info("System is not ready, skipping disk usage check.");
			return;
		}
		logger.debug("Checking disk usage...");
		calculateDiskStatus();
		logger.debug("Disk usage check completed successfully.");

		if (diskStatus.isAlarm()) {
			logger.debug("Disk usage in alarm state, triggering an alarm.");
			sendAlarm();
		}
	}

	protected String getServerName() throws UnknownHostException {
		if (serverName == null) {
			serverName = InetAddress.getLocalHost().getHostName();
		}
		return serverName;
	}

	protected void sendAlarm() {
		logger.info("Disk usage is in alarm state, sending a notification.");
		try {
			Map<String, Object> model = new HashMap<>();
			model.put(SERVER_NAME_MODEL_KEY, getServerName());
			model.put(FORMAT_SIZE_MODEL_KEY, (Function<Long, String>) FileUtils::byteCountToDisplaySize);
			notificationSender.sendMessage(diskStatus, model);
		} catch (Exception e) {
			logger.error("Failed to send disk usage alarm notification", e);
		}
	}

	/**
	 * Calculates the current disk status based on the disk usage.
	 */
	protected synchronized void calculateDiskStatus() {
		DiskStatus previousStatus = diskStatus;
		DiskInfo newDiskInfo = getDiskInfo();

		if (previousStatus != null && previousStatus.isAlarm()) {
			diskStatus = previousAlarmStatus(newDiskInfo, previousStatus, lowWaterMark, highWaterMark);
		} else {
			diskStatus = noPreviousAlarmStatus(newDiskInfo, lowWaterMark, highWaterMark);
		}
	}

	/**
	 * Handles the case where there was no previous alarm status.
	 * Checks the current disk usage and if above the high watermark, it triggers
	 * the git gc on all repositories.
	 *
	 * @param newDiskInfo   the new disk information
	 * @param lowWaterMark  the low watermark percentage
	 * @param highWaterMark the high watermark percentage
	 * @return a new DiskStatus object with the updated disk information and alarm state
	 */
	protected DiskStatus noPreviousAlarmStatus(DiskInfo newDiskInfo, int lowWaterMark, int highWaterMark) {
		int diskUsage = newDiskInfo.getDiskUsage();
		logger.debug("Previous disk status was NOT an alarm status, checking current disk usage: {}%", diskUsage);

		boolean aboveHigh = diskUsage >= highWaterMark;

		boolean alarm = false;
		Instant alarmDate = null;
		Instant lastCleanup = null;
		if (aboveHigh) {
			logger.debug("Running git gc on all repositories as disk usage is above high watermark.");
			try {
				siteService.garbageCollectRepositories();
			} catch (RepositoryException e) {
				logger.error("Failed to run git gc on repositories", e);
			}
			lastCleanup = now();
			newDiskInfo = getDiskInfo();
			diskUsage = newDiskInfo.getDiskUsage();
			boolean aboveLow = diskUsage >= lowWaterMark;
			if (aboveLow) {
				logger.warn("Disk usage {}% is above lowWaterMark ({}%), triggering an alarm.", diskUsage, lowWaterMark);
				alarm = true;
				alarmDate = now();
			} else {
				logger.info("Disk usage {}% is below lowWaterMark ({}%) after git gc, no alarm triggered.", diskUsage, lowWaterMark);
			}
		} else {
			logger.debug("Disk usage {}% is below highWaterMark ({}%), no action needed.", diskUsage, highWaterMark);
		}

		return new DiskStatus(
				newDiskInfo,
				highWaterMark,
				lowWaterMark,
				alarm,
				alarmDate,
				now(),
				lastCleanup
		);
	}

	/**
	 * Gets the current disk information based on the configured repository base path.
	 *
	 * @return a DiskInfo object containing the disk usage information
	 */
	protected DiskInfo getDiskInfo() {
		if (!systemStatusProvider.isSystemReady()) {
			logger.warn("System is not ready. DiskInfo will not be retrieved yet");
			return null;
		}
		File baseRepoFile = new File(baseRepoPath);
		return new DiskInfo(baseRepoFile);
	}

	/**
	 * Handles the case where the previous disk status was an alarm.
	 * Checks the current disk usage and if still above the low watermark,
	 * it keeps the alarm state.
	 *
	 * @param newDiskInfo    the new disk information
	 * @param previousStatus the previous disk status
	 * @param lowWaterMark   the low watermark percentage
	 * @param highWaterMark  the high watermark percentage
	 * @return a new DiskStatus object with the updated disk information and alarm state
	 */
	protected DiskStatus previousAlarmStatus(DiskInfo newDiskInfo, DiskStatus previousStatus, int lowWaterMark, int highWaterMark) {
		boolean alarm;
		Instant alarmDate;
		logger.debug("Previous disk status was an alarm, checking if it should be kept or cleared...");
		int diskUsage = newDiskInfo.getDiskUsage();
		boolean aboveLow = diskUsage >= lowWaterMark;
		// Keep the alarm state if still above the low watermark
		if (aboveLow) {
			logger.debug("Disk usage is {}%, which is still above low watermark, keeping the alarm.", diskUsage);
			alarm = true;
			alarmDate = previousStatus.getAlarmDate();
		} else {
			logger.debug("Disk usage is {}%, which is below low watermark, clearing the alarm.", diskUsage);
			alarm = false;
			alarmDate = null;
		}
		return new DiskStatus(
				newDiskInfo,
				highWaterMark,
				lowWaterMark,
				alarm,
				alarmDate,
				now(),
				previousStatus.getLastCleanup()
		);
	}

	public DiskStatus getDiskStatus() {
		return diskStatus;
	}
}
