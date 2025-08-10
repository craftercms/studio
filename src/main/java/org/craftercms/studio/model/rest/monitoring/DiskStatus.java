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
package org.craftercms.studio.model.rest.monitoring;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.craftercms.commons.monitoring.DiskInfo;

import java.time.Instant;

/**
 * Holds the status of the disk usage alarm.
 *
 * <ul>
 *   <li>diskInfo - the disk usage information</li>
 *   <li>highWaterMark - the high water mark percentage for disk usage</li>
 *   <li>lowWaterMark - the low water mark percentage for disk usage</li>
 *   <li>alarm - indicates if the alarm is active</li>
 *   <li>alarmDate - the date when the alarm was triggered</li>
 *   <li>lastCheck - the date when the disk was last checked</li>
 *   <li>lastCleanup - the date when the disk was last cleaned up</li>
 * </ul>
 */
public class DiskStatus {

	protected final DiskInfo diskInfo;
	protected final int highWaterMark;
	protected final int lowWaterMark;
	protected final boolean alarm;
	protected final Instant alarmDate;
	protected final Instant lastCheck;
	protected final Instant lastCleanup;

	public DiskStatus(DiskInfo diskInfo, int highWaterMark,
					  int lowWaterMark, boolean alarm,
					  Instant alarmDate, Instant lastCheck,
					  Instant lastCleanup) {
		this.alarm = alarm;
		this.diskInfo = diskInfo;
		this.highWaterMark = highWaterMark;
		this.lowWaterMark = lowWaterMark;
		this.alarmDate = alarmDate;
		this.lastCheck = lastCheck;
		this.lastCleanup = lastCleanup;
	}

	public boolean isAlarm() {
		return alarm;
	}

	public Instant getAlarmDate() {
		return alarmDate;
	}

	@JsonUnwrapped
	public DiskInfo getDiskInfo() {
		return diskInfo;
	}

	public int getHighWaterMark() {
		return highWaterMark;
	}

	public Instant getLastCheck() {
		return lastCheck;
	}

	public Instant getLastCleanup() {
		return lastCleanup;
	}

	public int getLowWaterMark() {
		return lowWaterMark;
	}
}
