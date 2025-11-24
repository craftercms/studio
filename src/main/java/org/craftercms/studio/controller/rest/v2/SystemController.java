/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import jakarta.validation.Valid;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.studio.api.v2.dal.system.SystemProperty;
import org.craftercms.studio.api.v2.service.system.SystemPropertiesService;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.system.UpdateSystemPropertiesRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

/**
 * Provides access to system properties
 */
@Validated
@RequestMapping(API_2 + SYSTEM)
@RestController
public class SystemController {

	private final SystemPropertiesService systemPropertiesService;

	@ConstructorProperties({"systemPropertiesService"})
	public SystemController(final SystemPropertiesService systemPropertiesService) {
		this.systemPropertiesService = systemPropertiesService;
	}

	@GetMapping(PROPERTIES)
	public ResultOne<Collection<SystemProperty>> getSystemProperties(@RequestParam
																	 List<@EsapiValidatedParam(type = ALPHANUMERIC) String> properties) {
		ResultOne<Collection<SystemProperty>> result = new ResultOne<>();
		result.setEntity(ResultConstants.RESULT_KEY_PROPERTIES, systemPropertiesService.getSystemProperties(properties));
		result.setResponse(OK);
		return result;
	}

	@PostMapping(PROPERTIES)
	public Result setSystemProperties(@Valid @RequestBody UpdateSystemPropertiesRequest request) {
		Map<String, String> propertiesMap = new HashMap<>();
		for (UpdateSystemPropertiesRequest.SystemProperty property : request.getProperties()) {
			propertiesMap.put(property.getName(), property.getValue());
		}
		systemPropertiesService.setSystemProperties(propertiesMap);
		Result result = new Result();
		result.setResponse(OK);
		return result;
	}
}
