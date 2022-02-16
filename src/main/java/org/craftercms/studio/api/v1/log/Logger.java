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
package org.craftercms.studio.api.v1.log;

/**
 * Logger object
 * @author russdanner
 */
public interface Logger {

	String LEVEL_OFF = "off";
	String LEVEL_DEBUG = "debug";
	String LEVEL_WARN = "warn";
	String LEVEL_ERROR = "error";
	String LEVEL_INFO = "info";
	
	String LEVEL_FATAL = "fatal";
	String LEVEL_TRACE = "trace";

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
	 * log debug message
	 * @param msg the message or message format to log
	 * @param t  the error to include
	 * @param args argument for the log
	 */
	void debug(String msg, Throwable t, Object... args);

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
	 * @param err with error
	 * @param args argument for the log
	 */
	void error(String msg, Exception err, Object ... args);

	/**
	 * Check if debug level is enabled
	 * @return true if debug is enabled otherwise false
	 */
	boolean isDebugEnabled();
}
