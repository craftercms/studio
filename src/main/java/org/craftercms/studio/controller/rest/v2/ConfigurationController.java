/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidConfigurationPath;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.config.TranslationConfiguration;
import org.craftercms.studio.model.rest.ConfigurationHistory;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.WriteConfigurationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.Strings.CS;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CONFIG;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_HISTORY;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@Validated
@RestController
@RequestMapping(API_2 + CONFIGURATION)
public class ConfigurationController {

	private final ConfigurationService configurationService;
	private final StudioConfiguration studioConfiguration;
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	@ConstructorProperties({"configurationService", "studioConfiguration"})
	public ConfigurationController(ConfigurationService configurationService, StudioConfiguration studioConfiguration) {
		this.configurationService = configurationService;
		this.studioConfiguration = studioConfiguration;
	}

	@GetMapping(CLEAR_CACHE)
	public Result clearCache(@ValidSiteId @RequestParam String siteId) {
		configurationService.invalidateConfiguration(siteId);
		var result = new Result();
		result.setResponse(OK);
		return result;
	}

	@GetMapping(GET_CONFIGURATION)
	@LogExecutionTime
	public ResultOne<String> getConfiguration(@ValidSiteId @RequestParam(name = "siteId", required = true) String siteId,
						  @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(name = "module", required = true) String module,
						  @ValidConfigurationPath @RequestParam(name = "path", required = true) String path,
						  @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(name = "environment", required = false) String environment)
			throws ServiceLayerException {
		final String content;
		if (CS.equals(siteId, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
			content = configurationService.getGlobalConfigurationAsString(path);
		} else {
			content = configurationService.getConfigurationAsString(siteId, module, path, environment);
		}

		ResultOne<String> result = new ResultOne<>();
		result.setEntity("content", content);
		result.setResponse(OK);
		return result;
	}

	@PostMapping(WRITE_CONFIGURATION)
	public Result writeConfiguration(@Validated @RequestBody WriteConfigurationRequest wcRequest)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		InputStream is = IOUtils.toInputStream(wcRequest.getContent(), UTF_8);
		String siteId = wcRequest.getSiteId();
		if (CS.equals(siteId, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
			configurationService.writeGlobalConfiguration(wcRequest.getPath(), is);
		} else {
			configurationService.writeConfiguration(siteId, wcRequest.getModule(), wcRequest.getPath(),
				wcRequest.getEnvironment(), is);
		}
		Result result = new Result();
		result.setResponse(OK);
		return result;
	}

	@GetMapping(GET_CONFIGURATION_HISTORY)
	public ResultOne<ConfigurationHistory> getConfigurationHistory(@ValidSiteId @RequestParam(name = "siteId", required = true) String siteId,
								       @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(name = "module", required = true) String module,
								       @ValidConfigurationPath @RequestParam(name = "path", required = true) String path,
								       @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(name = "environment", required = false) String environment)
		throws ServiceLayerException, UserNotFoundException {
		ConfigurationHistory history = configurationService.getConfigurationHistory(siteId, module, path, environment);

		ResultOne<ConfigurationHistory> result = new ResultOne<>();
		result.setEntity(RESULT_KEY_HISTORY, history);
		result.setResponse(OK);
		return result;
	}

	@GetMapping(TRANSLATION)
	public ResultOne<TranslationConfiguration> getTranslationConfiguration(@ValidSiteId @RequestParam String siteId) throws ServiceLayerException {
		ResultOne<TranslationConfiguration> result = new ResultOne<>();
		result.setEntity(RESULT_KEY_CONFIG, configurationService.getTranslationConfiguration(siteId));
		result.setResponse(OK);
		return result;
	}

}
