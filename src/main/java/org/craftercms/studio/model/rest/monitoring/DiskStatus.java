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
 * @param diskInfo      the disk usage information
 * @param highWaterMark the high water mark percentage for disk usage
 * @param lowWaterMark  the low water mark percentage for disk usage
 * @param alarm         indicates if the alarm is active
 * @param alarmDate     the date when the alarm was triggered
 * @param lastCheck     the date when the disk was last checked
 * @param lastCleanup   the date when the disk was last cleaned up
 */
public record DiskStatus(@JsonUnwrapped DiskInfo diskInfo,
						 int highWaterMark,
						 int lowWaterMark,
						 boolean alarm,
						 Instant alarmDate,
						 Instant lastCheck,
						 Instant lastCleanup) {
}
