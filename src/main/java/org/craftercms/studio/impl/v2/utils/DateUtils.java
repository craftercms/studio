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
package org.craftercms.studio.impl.v2.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.ZoneOffset.UTC;

/**
 * Utility methods for handling dates
 *
 * @author joseross
 * @since 4.0.0
 */
public abstract class DateUtils {

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter
                                                            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                                                            .withZone(UTC);

    /**
     * Returns the current time in UTC
     */
    public static ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(UTC);
    }

    /**
     * Applies the given formatter to the current time in UTC
     */
    public static String formatCurrentTime(DateTimeFormatter formatter) {
        return formatDate(getCurrentTime(), formatter);
    }

    /**
     * Returns the current time in UTC formatted using the ISO standard
     */
    public static String getCurrentTimeIso() {
        return formatCurrentTime(ISO_FORMATTER);
    }

    /**
     * Applies the given pattern to format the current time in UTC
     */
    public static String formatCurrentTime(String pattern) {
        return formatCurrentTime(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats a date using any pattern
     */
    public static String formatDate(ZonedDateTime dateTime, String patter) {
        return formatDate(dateTime, DateTimeFormatter.ofPattern(patter));
    }

    public static String formatDateIso(ZonedDateTime dateTime) {
        return formatDate(dateTime, ISO_FORMATTER);
    }

    /**
     * Formats a date using a formatter
     */
    public static String formatDate(ZonedDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    /**
     * Parses a date using a formatter
     */
    public static ZonedDateTime parseDate(String value, DateTimeFormatter formatter) {
        return ZonedDateTime.parse(value,formatter);
    }

    /**
     * Parses a date using the UTC formatter
     */
    public static ZonedDateTime parseDateIso(String value) {
        return parseDate(value, ISO_FORMATTER);
    }

}
