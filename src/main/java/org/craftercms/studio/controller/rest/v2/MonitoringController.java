/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import org.craftercms.commons.monitoring.MemoryInfo;
import org.craftercms.commons.monitoring.StatusInfo;
import org.craftercms.commons.monitoring.VersionInfo;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_MEMORY;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_STAUS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_VERSION;

/**
 * Rest controller to provide monitoring information
 * @author joseross
 */
@RestController
@RequestMapping("/api/2" + ROOT_URL)
public class MonitoringController {

    @GetMapping(MEMORY_URL)
    public ResultOne<MemoryInfo> getCurrentMemory() {
        ResultOne<MemoryInfo> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_MEMORY, MemoryInfo.getCurrentMemory());
        return result;
    }

    @GetMapping(STATUS_URL)
    public ResultOne<StatusInfo> getCurrentStatus() {
        ResultOne<StatusInfo> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_STAUS, StatusInfo.getCurrentStatus());
        return result;
    }

    @GetMapping(VERSION_URL)
    public ResultOne<VersionInfo> getCurrentVersion() throws Exception {
        ResultOne<VersionInfo> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_VERSION, VersionInfo.getVersion(getClass()));
        return result;
    }

}
