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
package org.craftercms.studio.impl.v1.log.l4j;

import org.craftercms.studio.api.v1.log.AbstractLogger;
import org.craftercms.studio.api.v1.log.Logger;

/**
 * Logger object, log4j implementation
 * @author russdanner
 */
public class LoggerImpl extends AbstractLogger {

	/**
	 * return the name for the logger
	 */
	public String getName() {
		return _logger.getName();
	}
	
	/**
	 * return the level for the logger
	 */
	public String getLevel() {
		String retLevel = Logger.LEVEL_OFF;
		
//		if(_logger.isFatalEnabeled()) retLevel = Logger.LEVEL_FATAL;
		if(_logger.isErrorEnabled()) retLevel = Logger.LEVEL_ERROR;
		if(_logger.isWarnEnabled()) retLevel = Logger.LEVEL_WARN;
		if(_logger.isInfoEnabled()) retLevel = Logger.LEVEL_INFO;
		if(_logger.isDebugEnabled()) retLevel = Logger.LEVEL_DEBUG;
		if(_logger.isTraceEnabled()) retLevel = Logger.LEVEL_TRACE;
		
		return retLevel;
	}

	/**
	 * log debug message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void debug(String msg, Object ... args) {
		if(_logger.isDebugEnabled()) {
			String message = expandMessage(msg, args);
			_logger.debug(message);
		}		
	}

	/**
	 * log debug message
	 * @param msg the message or message format to log
	 * @param t the error to include
	 * @param args argument for the log
	 */
	@Override
	public void debug(String msg, Throwable t, Object... args) {
		if(_logger.isDebugEnabled()) {
			String message = expandMessage(msg, args);
			_logger.debug(message, t);
		}
	}

	/**
	 * log warn message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void warn(String msg, Object ... args) {
		if(_logger.isWarnEnabled()) {
			String message = expandMessage(msg, args);
			_logger.warn(message);
		}		
	}

	/**
	 * log info message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void info(String msg, Object ... args) {
		if(_logger.isInfoEnabled()) {
			String message = expandMessage(msg, args);
			_logger.info(message);
		}
	}

	/**
	 * log error message
	 * @param msg the message or message format to log
	 * @param args argument for the log
	 */
	public void error(String msg, Object ... args) {
		if(_logger.isErrorEnabled()) {
			String message = expandMessage(msg, args);
			_logger.error(message);
		}		
	}

	/**
	 * log error message
	 * @param msg the message or message format to log
	 * @param err with error
	 * @param args argument for the log
	 */
	public void error(String msg, Exception err, Object ... args) {
		if(_logger.isErrorEnabled()) {
			String message = expandMessage(msg, args);
			_logger.error(message, err);
		}		
	}

	@Override
	public boolean isDebugEnabled() {
		return _logger.isDebugEnabled();
	}

	/**
	 * package scope constructor 
	 */
	LoggerImpl(org.slf4j.Logger logger) {
		_logger = logger;
	}

	protected org.slf4j.Logger _logger;
}
