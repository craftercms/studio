/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
 * Logger object, log4j implementation
 * @author russdanner
 */
public class ConsoleLoggerImpl extends AbstractLogger {

	/**
	 * return the name for the logger
	 */
	public String getName() {
		return "Console Logger";
	}
	
	/**
	 * return the level for the logger
	 */
	public String getLevel() {
		String retLevel = Logger.LEVEL_INFO;

		return retLevel;
	}

	/**
	 * log debug message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void debug(String msg, Object ... args) {
		String message = expandMessage(msg, args);
		System.out.println("DEBUG: " +  message);
	}

	/**
	 * log warn message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void warn(String msg, Object ... args) {
		String message = expandMessage(msg, args);
		System.out.println("WARN: " +  message);
	}

	/**
	 * log info message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void info(String msg, Object ... args) {
		String message = expandMessage(msg, args);
		System.out.println("INFO: " +  message);
	}

	/**
	 * log error message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void error(String msg, Object ... args) {
		String message = expandMessage(msg, args);
		System.out.println("ERROR: " +  message);
	}

	/**
	 * log error message
	 * @param msg the message or message format to log
	 * @param err with error
	 * @param args argument for the log
	 */
	public void error(String msg, Exception err, Object ... args) {
		String message = expandMessage(msg, args);
		System.out.println("ERROR: " +  message);
	}
	
	/**
	 * package scope constructor 
	 */
	public ConsoleLoggerImpl() {
	}
}
