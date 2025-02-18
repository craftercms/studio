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
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.model.publish.CalculatedPublishItem;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.content.DependencyItem;
import org.craftercms.studio.model.rest.dependency.GetDependentsRequestBody;
import org.craftercms.studio.model.rest.dependency.GetSoftDependenciesRequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@Validated
@RestController
@RequestMapping(API_2 + DEPENDENCY)
public class DependencyController {

	private final DependencyService dependencyService;

	@ConstructorProperties({"dependencyService"})
	public DependencyController(final DependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}

	@PostMapping(DEPENDENCIES)
	public ResultOne<Map<String, Collection<String>>> getDependencies(@RequestBody @Valid GetSoftDependenciesRequestBody request) {
		Collection<String> softDeps = dependencyService.getSoftDependencies(request.getSiteId(), request.getPaths());
		Collection<String> hardDeps = dependencyService.getHardDependencies(request.getSiteId(), request.getPaths()).stream().map(CalculatedPublishItem::getPath).toList();

		softDeps.removeAll(hardDeps);

		ResultOne<Map<String, Collection<String>>> result = new ResultOne<>();
		result.setResponse(OK);
		Map<String, Collection<String>> items = new HashMap<>();
		items.put(RESULT_KEY_HARD_DEPENDENCIES, hardDeps);
		items.put(RESULT_KEY_SOFT_DEPENDENCIES, softDeps);
		result.setEntity(RESULT_KEY_ITEMS, items);
		return result;
	}

	@PostMapping(PATH_PARAM_SITE + DEPENDENT_ITEMS)
	public ResultOne<List<DependencyItem>> getDependentItems(@PathVariable @ValidSiteId String site,
															 @RequestBody @Valid GetDependentsRequestBody request)
		throws ServiceLayerException {
		List<DependencyItem> items = dependencyService.getDependentItems(site, request.getPath());
		var result = new ResultOne<List<DependencyItem>>();
		result.setResponse(OK);
		result.setEntity(RESULT_KEY_ITEMS, items);
		return result;
	}
}
