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
package org.craftercms.cstudio.alfresco.util;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A util class for converting string to various types for metadata extraction
 * 
 * @author hyanghee
 *
 */
public class ValueConverter implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -6194104691768087456L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverter.class);
	
	/**
	 * convert the given string to date
	 * 
	 * @param date
	 * @return date
	 */
	public static Date convertToDate(String formatString, String date) {
		if (!StringUtils.isEmpty(date)) {
			try {
				SimpleDateFormat format = new SimpleDateFormat(formatString);
				if (format != null) {
					return format.parse(date);
				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(formatString + " is not a valid datetime format.");
					}
				}
			} catch (ParseException e) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(date + " is not valid value for " + formatString);
				}
			}
		}
		return null;
	}

	/**
	 * convert the given date string from form (yyyy-MM-dd'T'HH:mm:ssZ with Z being (-|+)HH:mm to java timezone format (-|+)HHmm) to date
	 * 
	 * @param date
	 * @return date
	 */
	public static Date convertFromFormDate(String formatString, String date) {
		if (!StringUtils.isEmpty(date)) {
			String convertedValue = ContentFormatUtils.convertFromFormDate(date);
			if (convertedValue != null) {
				return convertToDate(formatString, convertedValue);
			}
		}
		return null;
	}

}
