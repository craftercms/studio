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

import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.commons.monitoring.MemoryInfo;
import org.craftercms.commons.monitoring.StatusInfo;
import org.craftercms.commons.monitoring.VersionInfo;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.monitor.MonitorService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.Map;

import static org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase.MEMORY_URL;
import static org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase.ROOT_URL;
import static org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase.STATUS_URL;
import static org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase.VERSION_URL;
import static org.craftercms.engine.controller.rest.MonitoringController.LOG_URL;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_EVENTS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_MEMORY;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_STATUS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_VERSION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Rest controller to provide monitoring information
 * @author joseross
 */
@RestController
@RequestMapping("/api/2")
public class MonitoringController extends ManagementTokenAware {

    protected final MonitorService monitorService;

    @ConstructorProperties({"studioConfiguration", "securityService", "monitorService"})
    public MonitoringController(StudioConfiguration studioConfiguration, SecurityService securityService, MonitorService monitorService) {
        super(studioConfiguration, securityService);
        this.monitorService = monitorService;
    }

    @GetMapping(value = ROOT_URL + MEMORY_URL)
    public ResultOne<MemoryInfo> getCurrentMemory(@RequestParam(name = "token", required = false) String token)
        throws InvalidManagementTokenException, InvalidParametersException {
        validateToken(token);
        ResultOne<MemoryInfo> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_MEMORY, MemoryInfo.getCurrentMemory());
        return result;
    }

    @GetMapping(value = ROOT_URL + STATUS_URL)
    public ResultOne<StatusInfo> getCurrentStatus(@RequestParam(name = "token", required = false) String token)
        throws InvalidManagementTokenException, InvalidParametersException {
        validateToken(token);
        ResultOne<StatusInfo> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_STATUS, StatusInfo.getCurrentStatus());
        return result;
    }

    @GetMapping(value = ROOT_URL + VERSION_URL)
    public ResultOne<VersionInfo> getCurrentVersion(@RequestParam(name = "token", required = false) String token)
        throws InvalidManagementTokenException, IOException, InvalidParametersException {
        validateToken(token);
        ResultOne<VersionInfo> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_VERSION, VersionInfo.getVersion(getClass()));
        return result;
    }

    @GetMapping(value = ROOT_URL + LOG_URL, produces = APPLICATION_JSON_VALUE)
    public ResultList<Map<String,Object>> getLogEvents(@RequestParam long since,
                                                       @RequestParam(name = "token", required = false) String token)
        throws InvalidManagementTokenException, InvalidParametersException {
        validateToken(token);
        ResultList<Map<String, Object>> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_EVENTS, monitorService.getLogEvents("craftercms", since));
        return result;
    }

}
