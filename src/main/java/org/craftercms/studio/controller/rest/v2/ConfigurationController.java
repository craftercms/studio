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

import java.beans.ConstructorProperties;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.content.ContentTypeService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.config.TranslationConfiguration;
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

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CONFIG;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_HISTORY;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_USAGE;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
@RequestMapping("/api/2/configuration")
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final StudioConfiguration studioConfiguration;
    private final ContentTypeService contentTypeService;

    @ConstructorProperties({"configurationService", "studioConfiguration", "contentTypeService"})
    public ConfigurationController(ConfigurationService configurationService, StudioConfiguration studioConfiguration,
                                   ContentTypeService contentTypeService) {
        this.configurationService = configurationService;
        this.studioConfiguration = studioConfiguration;
        this.contentTypeService = contentTypeService;
    }

    @GetMapping("clear_cache")
    public ResponseBody clearCache(@RequestParam String siteId) {
        configurationService.invalidateConfiguration(siteId);

        var responseBody = new ResponseBody();
        var result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping("/get_configuration")
    public ResponseBody getConfiguration(@RequestParam(name = "siteId", required = true) String siteId,
                                         @RequestParam(name = "module", required = true) String module,
                                         @RequestParam(name = "path", required = true) String path,
                                         @RequestParam(name = "environment", required = false) String environment) {
        String content = StringUtils.EMPTY;
        if (StringUtils.equals(siteId, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
            content = configurationService.getGlobalConfigurationAsString(path);
        } else {
            content = configurationService.getConfigurationAsString(siteId, module, path, environment);
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
            throws ServiceLayerException, UserNotFoundException {
        InputStream is = IOUtils.toInputStream(wcRequest.getContent(), UTF_8);
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
                                                @RequestParam(name = "environment", required = false) String environment)
            throws SiteNotFoundException, ContentNotFoundException {
        ConfigurationHistory history = configurationService.getConfigurationHistory(siteId, module, path, environment);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<ConfigurationHistory> result = new ResultOne<ConfigurationHistory>();
        result.setEntity(RESULT_KEY_HISTORY, history);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping("translation")
    public ResponseBody getConfiguration(@RequestParam String siteId) throws ServiceLayerException {
        ResultOne<TranslationConfiguration> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_CONFIG, configurationService.getTranslationConfiguration(siteId));
        result.setResponse(OK);

        ResponseBody body = new ResponseBody();
        body.setResult(result);

        return body;
    }

    @GetMapping("content-type/usage")
    public ResponseBody getContentTypeUsage(@RequestParam String siteId, @RequestParam String contentType)
            throws Exception {

        var result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_USAGE, contentTypeService.getContentTypeUsage(siteId, contentType));

        var body = new ResponseBody();
        body.setResult(result);

        return body;
    }

    @PostMapping("content-type/delete")
    public ResponseBody deleteContentType(@RequestBody @Valid DeleteContentTypeRequest request)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException {

        contentTypeService.deleteContentType(request.getSiteId(), request.getContentType(),
                request.isDeleteDependencies());
        var result = new Result();
        result.setResponse(OK);

        var body = new ResponseBody();
        body.setResult(result);

        return body;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class DeleteContentTypeRequest {

        @NotEmpty
        protected String siteId;

        @NotEmpty
        protected String contentType;

        protected boolean deleteDependencies;

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public boolean isDeleteDependencies() {
            return deleteDependencies;
        }

        public void setDeleteDependencies(boolean deleteDependencies) {
            this.deleteDependencies = deleteDependencies;
        }

    }

}
