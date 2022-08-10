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
import org.craftercms.studio.api.v2.service.log.LoggerService;
import org.craftercms.studio.model.rest.logging.LoggerConfiguredLevel;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Log4j implementation for {@link LoggerService}.
 *
 * @author jmendeza
 * @since 4.0.2
 */
public class Log4jLoggerServiceImpl implements LoggerService {

    @Override
    public List<LoggerConfiguredLevel> getLoggerLevels() {
        LoggerContext context = LoggerContext.getContext(false);
        return context.getLoggers().stream()
                .map(l -> new LoggerConfiguredLevel(l.getName(), l.getLevel().toString()))
                .collect(Collectors.toList());
    }

    @Override
    public LoggerConfiguredLevel getLoggerLevel(final String name) {
        LoggerContext context = LoggerContext.getContext(false);
        Logger logger = context.getLogger(name);
        return new LoggerConfiguredLevel(logger.getName(), logger.getLevel().toString());
    }

    @Override
    public void setLoggerLevel(final String name, final String level) {
        Configurator.setLevel(name, Level.valueOf(level));
    }
}
