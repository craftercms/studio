/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.model.rest.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.model.rest.ApiResponse.DEPRECATED;

@RestController
public class CmisController {

    @GetMapping("/api/2/cmis/list")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result list() {
        return getDeprecatedResponse();
    }

    @GetMapping("/api/2/cmis/search")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result search() {
        return getDeprecatedResponse();
    }

    @PostMapping("/api/2/cmis/clone")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result cloneContent() {
        return getDeprecatedResponse();
    }

    @PostMapping(value = "/api/2/cmis/upload")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result uploadContent() {
        return getDeprecatedResponse();
    }

    private Result getDeprecatedResponse() {
        Result result = new Result();
        result.setResponse(DEPRECATED);
        return result;
    }
}
