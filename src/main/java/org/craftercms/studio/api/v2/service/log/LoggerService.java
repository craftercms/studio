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
import org.craftercms.studio.model.rest.logging.LoggerConfiguredLevel;

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
     * Get all loggers and its configured priority levels
     *
     * @return list of {@link LoggerConfiguredLevel}
     */
    List<LoggerConfiguredLevel> getLoggerLevels() throws ServiceLayerException;

    /**
     * Get the logger with the given name and its configured priority level
     *
     * @param name logger name
     * @return list of {@link LoggerConfiguredLevel}
     */
    LoggerConfiguredLevel getLoggerLevel(String name) throws ServiceLayerException;

    /**
     * Updates a logger with a given name to have a given priority level
     *
     * @param name  logger name
     * @param level the priority level
     */
    void setLoggerLevel(String name, String level) throws ServiceLayerException;
}
