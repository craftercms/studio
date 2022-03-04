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
package org.craftercms.studio.impl.v1.util;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * util methods for parsing/converting data 
 * 
 * @author hyanghee
 *
 */
public class ContentFormatUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentFormatUtils.class);

	/** model date format constants **/
	public static final String DATE_PATTERN_TIMEZONE_GMT = "GMT";
	
	/**
	 * parse the given date using the model date format and the timezone
	 * 
	 * @param format
	 * @param dateStr
	 * @return timeZone
	 */
	public static ZonedDateTime parseDate(SimpleDateFormat format, String dateStr, String timeZone) {
		ZonedDateTime retDate = null;

		if(format != null && dateStr != null) {
			if (StringUtils.isEmpty(timeZone)) {
				format.setTimeZone(TimeZone.getTimeZone(DATE_PATTERN_TIMEZONE_GMT));
			} else {
				format.setTimeZone(TimeZone.getTimeZone(timeZone));
			}

            retDate = ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
		}
		else {
			LOGGER.error("Requested date format with null args dateStr: " + dateStr + " using format: " + format);
		}

		return retDate;
	}
    
	/**
	 * get a boolean value given a string
	 * 
	 * @param str
	 * @return boolean value
	 */
	public static boolean getBooleanValue(String str) {
		if (!StringUtils.isEmpty(str) && str.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * get integer value
	 * 
	 * @param str
	 * @return int value
	 */
	public static int getIntValue(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			LOGGER.error("failed to get a number from " + str, e);
			return -1;
		}
	}
}
