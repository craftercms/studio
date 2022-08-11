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

package org.craftercms.studio.impl.v2.service.log.internal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.spi.LoggerRegistry;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.exception.logger.LoggerNotFoundException;
import org.craftercms.studio.api.v2.service.log.LoggerService;
import org.craftercms.studio.model.rest.logging.LoggerConfig;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Log4j implementation for {@link LoggerService}.
 *
 * @author jmendeza
 * @since 4.0.2
 */
public class Log4jLoggerServiceImpl implements LoggerService {

    private LoggerConfig createLoggerLevel(Logger logger) {
        return new LoggerConfig(logger.getName(), logger.getLevel().toString().toLowerCase());
    }

    @Override
    public List<LoggerConfig> getLoggerConfigs() {
        LoggerContext context = LoggerContext.getContext(false);
        return context.getLoggers().stream()
                .map(this::createLoggerLevel)
                .collect(Collectors.toList());
    }

    @Override
    public LoggerConfig getLoggerConfig(final String name, final boolean createIfAbsent) throws ServiceLayerException {
        LoggerContext context = LoggerContext.getContext(false);
        LoggerRegistry<Logger> loggerRegistry = context.getLoggerRegistry();
        if (!createIfAbsent && !loggerRegistry.hasLogger(name)) {
            throw new LoggerNotFoundException(name);
        }
        Logger logger = context.getLogger(name);
        return createLoggerLevel(logger);
    }

    @Override
    public void setLoggerLevel(final String name, final String level, final boolean createIfAbsent) throws ServiceLayerException {
        LoggerRegistry<Logger> loggerRegistry = LoggerContext.getContext(false).getLoggerRegistry();
        if (!createIfAbsent && !loggerRegistry.hasLogger(name)) {
            throw new LoggerNotFoundException(name);
        }
        Configurator.setLevel(name, Level.valueOf(level));
    }
}
