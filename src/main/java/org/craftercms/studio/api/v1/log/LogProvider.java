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

import java.util.Map;

/**
 * Logger factory encapsulates a log providers and allows us to augment a provider with
 * additional features.
 * - no need to use if statements around log messages
 * - auto expansion of log formats
 * @author russdanner
 */
public interface LogProvider {

	/**
	 * return a logger implementation
	 * @param target ther target class for the logger
	 */
	Logger getLogger(Class target);

	/** 
	 * return a list of active loggers
	 */
	public Map<String, Logger> getLoggers();

	/**
	 * set a logger's level
	 * @param name the name of the logger
	 * @param level the level to set
	 */
	public void setLoggerLevel(String name, String level);

}
