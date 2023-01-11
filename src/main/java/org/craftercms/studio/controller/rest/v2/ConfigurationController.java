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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateObjectParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.content.ContentTypeService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.config.TranslationConfiguration;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.beans.ConstructorProperties;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.DELETED;
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
    @ValidateParams
    public ResponseBody clearCache(@EsapiValidatedParam(type = SITE_ID) @RequestParam String siteId) throws SiteNotFoundException {
        configurationService.invalidateConfiguration(siteId);

        var responseBody = new ResponseBody();
        var result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @ValidateParams
    @GetMapping("/get_configuration")
    public ResponseBody getConfiguration(@EsapiValidatedParam(type = SITE_ID) @RequestParam(name = "siteId", required = true) String siteId,
                                         @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(name = "module", required = true) String module,
                                         @EsapiValidatedParam(type = HTTPURI) @RequestParam(name = "path", required = true) String path,
                                         @EsapiValidatedParam(type = ALPHANUMERIC, notNull = false, notEmpty = false, notBlank = false) @RequestParam(name = "environment", required = false) String environment)
            throws ContentNotFoundException {
        final String content;
        if (StringUtils.equals(siteId, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
            content = configurationService.getGlobalConfigurationAsString(path);
        } else {
            content = configurationService.getConfigurationAsString(siteId, module, path, environment);
        }

        ResponseBody responseBody = new ResponseBody();
        ResultOne<String> result = new ResultOne<>();
        result.setEntity("content", content);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/write_configuration")
    @ValidateParams
    public ResponseBody writeConfiguration(@ValidateObjectParam @RequestBody WriteConfigurationRequest wcRequest)
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

    @ValidateParams
    @GetMapping("/get_configuration_history")
    public ResponseBody getConfigurationHistory(@EsapiValidatedParam(type = SITE_ID) @RequestParam(name = "siteId", required = true) String siteId,
                                                @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(name = "module", required = true) String module,
                                                @EsapiValidatedParam(type = HTTPURI) @RequestParam(name = "path", required = true) String path,
                                                @EsapiValidatedParam(type = ALPHANUMERIC, notNull = false, notEmpty = false, notBlank = false) @RequestParam(name = "environment", required = false) String environment)
            throws SiteNotFoundException, ContentNotFoundException {
        ConfigurationHistory history = configurationService.getConfigurationHistory(siteId, module, path, environment);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<ConfigurationHistory> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_HISTORY, history);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping("translation")
    public ResponseBody getTranslationConfiguration(@EsapiValidatedParam(type = SITE_ID) @RequestParam String siteId) throws ServiceLayerException {
        ResultOne<TranslationConfiguration> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_CONFIG, configurationService.getTranslationConfiguration(siteId));
        result.setResponse(OK);

        ResponseBody body = new ResponseBody();
        body.setResult(result);

        return body;
    }

    @ValidateParams
    @GetMapping("content-type/usage")
    public ResponseBody getContentTypeUsage(@EsapiValidatedParam(type = SITE_ID) @RequestParam String siteId,
                                            @EsapiValidatedParam(type = HTTPURI) @RequestParam String contentType)
            throws Exception {
        var result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_USAGE, contentTypeService.getContentTypeUsage(siteId, contentType));

        var body = new ResponseBody();
        body.setResult(result);

        return body;
    }

    @ValidateParams
    @GetMapping("content-type/preview_image")
    public ResponseEntity<Resource> getContentTypePreviewImage(@EsapiValidatedParam(type = SITE_ID) @RequestParam String siteId,
                                                               @EsapiValidatedParam(type = HTTPURI) @RequestParam String contentTypeId)
            throws ServiceLayerException {

        ImmutablePair<String, Resource> resource = contentTypeService.getContentTypePreviewImage(siteId, contentTypeId);
        String mimeType = StudioUtils.getMimeType(resource.getKey());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .body(resource.getValue());
    }

    @ValidateParams
    @PostMapping("content-type/delete")
    public ResponseBody deleteContentType(@ValidateObjectParam @RequestBody @Valid DeleteContentTypeRequest request)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException {

        contentTypeService.deleteContentType(request.getSiteId(), request.getContentType(),
                request.isDeleteDependencies());
        var result = new Result();
        result.setResponse(DELETED);

        var body = new ResponseBody();
        body.setResult(result);

        return body;
    }

    @JsonIgnoreProperties
    protected static class DeleteContentTypeRequest {

        @EsapiValidatedParam(type = SITE_ID)
        protected String siteId;

        @EsapiValidatedParam(type = HTTPURI)
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
