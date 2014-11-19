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
package org.craftercms.cstudio.api.log;

/**
 * Logger object
 * @author russdanner
 */
public interface Logger {

	static final String LEVEL_OFF = "off";
	static final String LEVEL_DEBUG = "debug";
	static final String LEVEL_WARN = "warn";
	static final String LEVEL_ERROR = "error";
	static final String LEVEL_INFO = "info";
	
	static final String LEVEL_FATAL = "fatal";
	static final String LEVEL_TRACE = "trace";

	/**
	 * return the name for the logger
	 */
	String getName();
	
	/**
	 * return the level for the logger
	 */
	String getLevel();

	/**
	 * log debug message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	void debug(String msg, Object ... args);

	/**
	 * log info message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	void info(String msg, Object ... args);

	/**
	 * log warn message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	void warn(String msg, Object ... args);

	/**
	 * log warn message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	void error(String msg, Object ... args);

	/**
	 * log warn message
	 * @param msg the message or message format to log
	 * @param exception with error
	 * @param args argument for the log
	 */
	void error(String msg, Exception err, Object ... args);

}
