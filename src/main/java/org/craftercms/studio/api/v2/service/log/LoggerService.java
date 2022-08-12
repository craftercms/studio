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

package org.craftercms.studio.api.v2.service.log;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.model.rest.logging.LoggerConfig;

import java.util.List;

/**
 * Provides access to loggers configuration.
 *
 * @author jmendeza
 * @since 4.0.2
 */
public interface LoggerService {

    String VALID_LEVEL_PATTERN = "(?i)(off|error|warn|info|debug|trace|all)";

    /**
     * Get all loggers and their configured priority levels
     *
     * @return list of {@link LoggerConfig}
     */
    List<LoggerConfig> getLoggerConfigs() throws ServiceLayerException;

    /**
     * Get the logger with the given name. The logger will be created in case it does not exist
     *
     * @param name logger name
     * @return a {@link LoggerConfig} object
     * @throws ServiceLayerException
     */
    default LoggerConfig getLoggerConfig(String name) throws ServiceLayerException {
        return getLoggerConfig(name, true);
    }

    /**
     * Get the logger with the given name and its configured priority level
     *
     * @param name           logger name
     * @param createIfAbsent if true, the logger will be created in case it does not exist yet
     *                       if false, a LoggerNotFoundException will be thrown if logger is not in the registry
     * @return a {@link LoggerConfig} object
     */
    LoggerConfig getLoggerConfig(String name, boolean createIfAbsent) throws ServiceLayerException;

    /**
     * Updates a logger with a given name to have a given priority level
     *
     * @param name           logger name
     * @param level          the priority level
     * @param createIfAbsent if true, the logger will be created in case it does not exist yet
     *                       if false, a LoggerNotFoundException will be thrown if logger is not in the registry
     */
    void setLoggerLevel(String name, String level, boolean createIfAbsent) throws ServiceLayerException;
}
