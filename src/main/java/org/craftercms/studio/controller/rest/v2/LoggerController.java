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

package org.craftercms.studio.controller.rest.v2;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.log.LoggerService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.logging.LoggerConfiguredLevel;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.beans.ConstructorProperties;

import static org.craftercms.studio.controller.rest.v2.LoggerController.ROOT_URL;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_RESULT;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_RESULTS;

/**
 * REST controller that provides access to Logger related operations.
 *
 * @author jmendeza
 * @since 4.0.2
 */
@RestController
@RequestMapping("/api/2" + ROOT_URL)
public class LoggerController {

    public final static String ROOT_URL = "/loggers";
    public final static String LOGGER_LEVEL = "/logger_level";


    private LoggerService loggerService;

    @ConstructorProperties({"logService"})
    public LoggerController(final LoggerService logService) {
        this.loggerService = logService;
    }

    @GetMapping
    public ResultList<LoggerConfiguredLevel> getLoggers() throws ServiceLayerException {
        ResultList<LoggerConfiguredLevel> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_RESULTS, loggerService.getLoggerLevels());
        return result;
    }

    @PostMapping(value = LOGGER_LEVEL)
    public ResultOne<LoggerConfiguredLevel> setLoggerLevel(@Valid @RequestBody LoggerConfiguredLevel loggerLevel) throws ServiceLayerException {
        ResultOne<LoggerConfiguredLevel> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        loggerService.setLoggerLevel(loggerLevel.getName(), loggerLevel.getLevel());
        result.setEntity(RESULT_KEY_RESULT, loggerService.getLoggerLevel(loggerLevel.getName()));
        return result;
    }
}
