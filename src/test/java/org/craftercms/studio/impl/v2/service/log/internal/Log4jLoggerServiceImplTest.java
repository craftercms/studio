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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.exception.logger.LoggerNotFoundException;
import org.craftercms.studio.model.rest.logging.LoggerConfig;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class Log4jLoggerServiceImplTest {

    private static final String LOGGER_1 = "LOGGER_1";
    private static final String LOGGER_2 = "LOGGER_2";
    private static final String LOGGER_3 = "LOGGER_3";
    private static final String LOGGER_4 = "LOGGER_4";
    private static final String[] LOGGER_NAMES = {LOGGER_1, LOGGER_2, LOGGER_3, LOGGER_4};

    @Autowired
    private Log4jLoggerServiceImpl log4jLoggerService;

    @Before
    public void setup() {
        for (String loggerName : LOGGER_NAMES) {
            LoggerFactory.getLogger(loggerName);
        }
        log4jLoggerService = new Log4jLoggerServiceImpl();
    }

    @Test
    public void getLoggersTest() {
        List<LoggerConfig> loggerConfigs = log4jLoggerService.getLoggerConfigs();
        assertNotNull(loggerConfigs);

        assertTrue(loggerConfigs.stream()
                .map(LoggerConfig::getName)
                .collect(Collectors.toList())
                .containsAll(List.of(LOGGER_NAMES)), "Not all logger names were found in the list returned by the service");
    }

    @Test
    public void setLoggerLevelTest() throws ServiceLayerException {
        String warn = Level.WARN.toString().toLowerCase();
        log4jLoggerService.setLoggerLevel(LOGGER_1, warn, true);
        LoggerConfig loggerConfig = log4jLoggerService.getLoggerConfig(LOGGER_1);
        assertEquals(warn, loggerConfig.getLevel(), "Configured logger level does not match after setLoggerLevel");
    }

    @Test(expected = LoggerNotFoundException.class)
    public void setAbsentLoggerLevelWithCreateIfAbsentFalseTest() throws ServiceLayerException {
        String level = Level.WARN.toString().toLowerCase();
        String loggerName = DateTime.now().toString();
        log4jLoggerService.setLoggerLevel(loggerName, level, false);
    }

    @Test
    public void setAbsentLoggerLevelWithCreateIfAbsentTrueTest() throws ServiceLayerException {
        String level = Level.WARN.toString().toLowerCase();
        String loggerName = DateTime.now().toString();
        log4jLoggerService.setLoggerLevel(loggerName, level, true);
        LoggerConfig loggerConfig = log4jLoggerService.getLoggerConfig(loggerName);
        assertEquals(level, loggerConfig.getLevel(), "Configured logger level does not match after setLoggerLevel");
    }

    @Test
    public void getAbsentLoggerLevelWithCreateIfAbsentTrueTest() throws ServiceLayerException {
        String loggerName = DateTime.now().toString();
        LoggerConfig loggerConfig = log4jLoggerService.getLoggerConfig(loggerName, true);
        assertNotNull(loggerConfig);
        assertEquals(loggerName, loggerConfig.getName(), "Configured logger name does not match after getLoggerLevel");
    }

    @Test(expected = LoggerNotFoundException.class)
    public void getAbsentLoggerLevelWithCreateIfAbsentFalseTest() throws ServiceLayerException {
        String loggerName = UUID.randomUUID().toString();
        LoggerConfig loggerConfig = log4jLoggerService.getLoggerConfig(loggerName, false);
    }

    @Test
    public void getExistingLoggerLevelWithCreateIfAbsentFalseTest() throws ServiceLayerException {
        LoggerConfig loggerConfig = log4jLoggerService.getLoggerConfig(LOGGER_1, false);
        assertNotNull(loggerConfig);
        assertEquals(LOGGER_1, loggerConfig.getName(), "Configured logger name does not match after getLoggerLevel");
    }

}
