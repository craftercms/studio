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
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.dependency.GetSoftDependenciesRequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DEPENDENCIES;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DEPENDENCY;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_HARD_DEPENDENCIES;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_SOFT_DEPENDENCIES;

@RestController
@RequestMapping(API_2 + DEPENDENCY)
public class DependencyController {

    private DependencyService dependencyService;

    @PostMapping(DEPENDENCIES)
    public ResponseBody getSoftDependencies(@RequestBody @Valid GetSoftDependenciesRequestBody request)
            throws ServiceLayerException {
        List<String> softDeps = dependencyService.getSoftDependencies(request.getSiteId(), request.getPaths());
        List<String> hardDeps = dependencyService.getHardDependencies(request.getSiteId(), request.getPaths());

        List<String> filteredSoftDeps =
                softDeps.stream().filter(sd -> !hardDeps.contains(sd)).collect(Collectors.toList());

        ResponseBody responseBody = new ResponseBody();
        ResultOne<Map<String, List<String>>> result = new ResultOne<Map<String, List<String>>>();
        result.setResponse(ApiResponse.OK);
        Map<String, List<String>> items = new HashMap<String, List<String>>();
        items.put(RESULT_KEY_HARD_DEPENDENCIES, hardDeps);
        items.put(RESULT_KEY_SOFT_DEPENDENCIES, filteredSoftDeps);
        result.setEntity(RESULT_KEY_ITEMS, items);
        responseBody.setResult(result);
        return responseBody;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }
}
