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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.craftercms.studio.api.v1.log.LogProvider;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;


/**
 * Logger factory encapsulates a log providers and allows us to augment a provider with
 * additional features.
 * - no need to use if statements around log messages
 * - auto expansion of log formats
 * @author russdanner
 */
public class L4jLogProvider implements LogProvider {

	public void init() {
		LoggerFactory.setProvider(new L4jLogProvider());
	}

	/** 
	 * return a list of active loggers
	 */
    @SuppressWarnings("unchecked")
	public Map<String, Logger> getLoggers() {
        LoggerContext context = LoggerContext.getContext(false);
        return context.getLoggers().stream().collect(Collectors.toMap(
        	org.apache.logging.log4j.core.Logger::getName,
			logger -> new LoggerImpl(org.slf4j.LoggerFactory.getLogger(logger.getName()))
		));
	}

	/**
	 * set a logger's level
	 * @param name the name of the logger
	 * @param level the level to set
	 */
	public void setLoggerLevel(String name, String level) {
		Configurator.setLevel(name, Level.valueOf(level));
	}

	
	/**
	 * return a logger implementation
	 * @param targetClass ther target class for the logger
	 */
	public Logger getLogger(Class targetClass) {
		Logger retLogger = null;
		org.slf4j.Logger l4jLogger = org.slf4j.LoggerFactory.getLogger(targetClass);
		retLogger = new LoggerImpl(l4jLogger);
		
		return retLogger;
	}
}
