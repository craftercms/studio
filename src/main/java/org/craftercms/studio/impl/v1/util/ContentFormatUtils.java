/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
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
	
	public final static Pattern PATTERN_ISO_DATE_MODEL = Pattern.compile("([0-9]{4}\\-[0-9]{2}\\-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2})((\\+|\\-)[0-9]{2}:[0-9]{2})");
	public final static Pattern PATTERN_ISO_DATE_WO_TIME_MODEL = Pattern.compile("([0-9]{4}\\-[0-9]{2}\\-[0-9]{2})((\\+|\\-)[0-9]{2}:[0-9]{2})");
	public final static Pattern PATTERN_DATE_MODEL = Pattern.compile("([0-9]{4}\\-[0-9]{2}\\-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2})((\\+|\\-)[0-9]{2})([0-9]{2})");

	/** model date format constants **/
	public static final String DATE_PATTERN_MODEL = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATE_PATTERN_TIMEZONE_GMT = "GMT";
	/** approved scheduled items category date format constants **/
	public static final String DATE_PATTERN_SCHEDULED = "MM/dd KK:mma";
	public static final SimpleDateFormat DATE_FORMAT_SCHEDULED = new SimpleDateFormat("MM/dd KK:mma");

	protected static SimpleDateFormat DATE_FORMAT_MODEL = null; 
	
	/**
	 * parse the given date using the model date format and the timezone
	 * 
	 * @param format
	 * @param dateStr
	 * @return timeZone
	 */
	public static Date parseDate(SimpleDateFormat format, String dateStr, String timeZone) {
		Date retDate = null;

		if(format != null && dateStr != null) {
			if (StringUtils.isEmpty(timeZone)) {
				format.setTimeZone(TimeZone.getTimeZone(DATE_PATTERN_TIMEZONE_GMT));
			} else {
				format.setTimeZone(TimeZone.getTimeZone(timeZone));
			}

			try {
				retDate = format.parse(dateStr);
			} catch (ParseException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Failed to parse date: " + dateStr + " using format: " + DATE_FORMAT_MODEL); //would be null.toPattern());
				}
			}
		}
		else {
			LOGGER.error("Requested date format with null args dateStr: " + dateStr + " using format: " + format);
		}

		return retDate;
	}
	
	/**
	 * parse the given date using the model date format
	 * 
	 * @param format
	 * @param dateStr
	 * @return date
	 */
	public static Date parseDate(SimpleDateFormat format, String dateStr) {
		return parseDate(format, dateStr, DATE_PATTERN_TIMEZONE_GMT);
	}
	
	/**
	 * format the given date and timezone 
	 * 
	 * @param format
	 * @param date
	 * @param timeZone
	 * @return formatted date
	 */
	public static String formatDate(SimpleDateFormat format, Date date, String timeZone) {
		if (StringUtils.isEmpty(timeZone)) {
			format.setTimeZone(TimeZone.getTimeZone(DATE_PATTERN_TIMEZONE_GMT));
		} else {
			format.setTimeZone(TimeZone.getTimeZone(timeZone));
		}
		return format.format(date);
	}
	
	/**
	 * format the given date 
	 * 
	 * @param format
	 * @param date
	 * @return date string
	 */
	public static String formatDate(SimpleDateFormat format, Date date) {
		return format.format(date);
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
	 * get a long value from the given string
	 * 
	 * @param str
	 * @return long
	 */
	public static long getLongValue(String str) {
		try {
			return Long.parseLong(str);
		} catch (NumberFormatException e) {
			LOGGER.error("failed to get a long value from " + str, e);
			return -1L;
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

	/**
	 * convert string to the property data type
	 * 
	 * @param namespaceService
	 * @param propType
	 * @param className
	 * @param value
	 * @param dateFormat
	 * @return converted value
	 */
	/*public static Serializable convertType(NamespaceService namespaceService, QName propType, String className, String value, SimpleDateFormat dateFormat) {
		try {
			if (!(Class.forName(className).equals(String.class)) && StringUtils.isEmpty(value) ) {
				return null;
			}
			if (Class.forName(className).equals(String.class)) {
				return value;
			} else if (Class.forName(className).equals(Long.class)) {
				return Long.parseLong(value);
			} else if (Class.forName(className).equals(Integer.class)) {
				return Integer.parseInt(value);
			} else if (Class.forName(className).equals(Float.class)) {
				return Float.parseFloat(value);
			} else if (Class.forName(className).equals(Double.class)) {
				return Double.parseDouble(value);
			} else if (Class.forName(className).equals(Boolean.class)) {
				return Boolean.parseBoolean(value);
			} else if (Class.forName(className).equals(QName.class)) {
				return namespaceService.createQName(value);
			} else if (Class.forName(className).equals(NodeRef.class)) {
				return new NodeRef(value);
			} else if (Class.forName(className).equals(Date.class)) {
				String convertedValue = convertFromFormDate(value);
				if (convertedValue != null) {
					return ContentFormatUtils.parseDate(dateFormat, convertedValue);
				}
			} 
		} catch (NumberFormatException e) {
			LOGGER.error("Failed to convert the value for " + propType + " class: " + className + " value: " + value, e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Failed to convert the value for " + propType + " class: " + className + " value: " + value, e);
		} 
		return null;
	}*/
	
	/**
	 * convert ISO date format yyyy-MM-dd'T'HH:mm:ssZ with Z being (-|+)HH:mm to java timezone format (-|+)HHmm
	 *  
	 * @param date
	 * @return converted date
	 */
	public static String convertFromFormDate(String date) {
		if (!StringUtils.isEmpty(date)) {
			Matcher matcher = PATTERN_ISO_DATE_MODEL.matcher(date);
			Matcher matcher2 = PATTERN_ISO_DATE_WO_TIME_MODEL.matcher(date);
			if (matcher.matches()) {
				String convertedDate = matcher.group(1) + matcher.group(2).replace(":", "");
				return convertedDate;
			} else if (matcher2.matches()) {
				String convertedDate = matcher2.group(1) + "T00:00:01" + matcher2.group(2).replace(":", "");
				return convertedDate;
			} else {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Failed to convert from ISO date. " + date + " does not match " + PATTERN_ISO_DATE_MODEL.toString() + ", Or " + PATTERN_ISO_DATE_WO_TIME_MODEL.toString());
				}
			}
		}
		return null;
	}
	
	/**
	 * convert from timezone format (-|+)HHmm to ISO date format yyyy-MM-dd'T'HH:mm:ssZ with Z being (-|+)HH:mm
	 *  
	 * @param date
	 * @return converted date
	 */
	public static String convertToFormDate(String date) {
		if (!StringUtils.isEmpty(date)) {
			Matcher matcher = PATTERN_DATE_MODEL.matcher(date);
			if (matcher.matches()) {
				String convertedDate = matcher.group(1) + matcher.group(2) + ":" + matcher.group(4);
				return convertedDate;
			} else {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Failed to convert to ISO date. " + date + " does not match " + PATTERN_DATE_MODEL.toString());
				}
			}
		}
		return null;
	}
	
}
