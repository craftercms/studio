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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v2.service.system.SystemPropertiesService;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.system.UpdateSystemPropertiesRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.service.system.SystemPropertiesService.PROPERTY_NAME_ALLOWED_PATTERN;
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
	public ResultOne<Map<String, String>> getSystemProperties(@RequestParam @NotEmpty
															  List<@Size(max = 50) @ValidateNoTagsParam @ValidateStringParam(whitelistedPatterns = PROPERTY_NAME_ALLOWED_PATTERN) String> properties) {
		ResultOne<Map<String, String>> result = new ResultOne<>();
		result.setEntity(ResultConstants.RESULT_KEY_PROPERTIES, systemPropertiesService.getSystemProperties(properties));
		result.setResponse(OK);
		return result;
	}

	@PostMapping(PROPERTIES)
	public Result setSystemProperties(@Valid @RequestBody UpdateSystemPropertiesRequest request) {
		systemPropertiesService.setSystemProperties(request.getProperties());
		Result result = new Result();
		result.setResponse(OK);
		return result;
	}
}
