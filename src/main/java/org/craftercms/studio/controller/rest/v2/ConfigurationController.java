/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.ConfigurationHistory;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.WriteConfigurationRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_HISTORY;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
@RequestMapping("/api/2/configuration")
public class ConfigurationController {

    private ConfigurationService configurationService;
    private StudioConfiguration studioConfiguration;

    @GetMapping("/get_configuration")
    public ResponseBody getConfiguration(
            @RequestParam(name = "siteId", required = true) String siteId,
            @RequestParam(name = "module", required = true) String module,
            @RequestParam(name = "path", required = true) String path,
            @RequestParam(name = "environment", required = false) String environment
    ) {
        String content = StringUtils.EMPTY;
        if (StringUtils.equals(siteId, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
            content = configurationService.getGlobalConfiguration(path);
        } else {
            boolean hasExtension = path.endsWith(".xml") || path.endsWith(".json");
            if (hasExtension) {
                content = configurationService.getConfigurationAsString(siteId, module, path, environment);
            } else {
                // Start with XML as this is what's most common (to date)
                // In a few releases we could switch around if JSON picks up nicely.
                String finalPath = String.format("%s.xml", path);
                content = configurationService.getConfigurationAsString(siteId, module, finalPath, environment);
                if (content == null) {
                    finalPath = String.format("%s.json", path);
                    content = configurationService.getConfigurationAsString(siteId, module, finalPath, environment);
                }
            }
        }
        ResponseBody responseBody = new ResponseBody();
        ResultOne<String> result = new ResultOne<String>();
        result.setEntity("content", content);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/write_configuration")
    public ResponseBody writeConfiguration(@RequestBody WriteConfigurationRequest wcRequest)
            throws ServiceLayerException {
        InputStream is = IOUtils.toInputStream(wcRequest.getContent());
        String siteId = wcRequest.getSiteId();
        if (StringUtils.equals(siteId, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
            configurationService.writeGlobalConfiguration(wcRequest.getPath(), is);
        } else {
            configurationService.writeConfiguration(siteId, wcRequest.getModule(), wcRequest.getPath(),
                    wcRequest.getEnvironment(), is);
        }
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping("/get_configuration_history")
    public ResponseBody getConfigurationHistory(@RequestParam(name = "siteId", required = true) String siteId,
                                                @RequestParam(name = "module", required = true) String module,
                                                @RequestParam(name = "path", required = true) String path,
                                                @RequestParam(name = "environment", required = false) String environment) {
        ConfigurationHistory history = configurationService.getConfigurationHistory(siteId, module, path, environment);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<ConfigurationHistory> result = new ResultOne<ConfigurationHistory>();
        result.setEntity(RESULT_KEY_HISTORY, history);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
