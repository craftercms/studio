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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_MANAGEMENT_PREVIEW_AUTHORIZATION_TOKEN;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_MANAGEMENT_PREVIEW_PROTECTED_URLS;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ALL_SUB_URLS;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PROXY_ENGINE;

/**
 * Controller to proxy request to preview & add the management token if needed
 */
@RestController
public class ProxyController {

    protected final StudioConfiguration studioConfiguration;

    protected final ServicesConfig servicesConfig;

    protected final RestTemplate restTemplate = new RestTemplate();

    @ConstructorProperties({"studioConfiguration", "servicesConfig"})
    public ProxyController(StudioConfiguration studioConfiguration, ServicesConfig servicesConfig) {
        this.studioConfiguration = studioConfiguration;
        this.servicesConfig = servicesConfig;
    }

    @RequestMapping(PROXY_ENGINE + ALL_SUB_URLS)
    public ResponseEntity<Object> proxyEngine(@RequestBody(required = false) String body,
                                              @RequestParam("crafterSite") String siteId,
                                              HttpMethod method, HttpServletRequest request)
            throws URISyntaxException {
        String requestUrl = request.getRequestURI();
        String proxiedUrl = StringUtils.replace(requestUrl, request.getContextPath(), StringUtils.EMPTY);
        proxiedUrl = StringUtils.replace(proxiedUrl, PROXY_ENGINE, StringUtils.EMPTY);
        List<String> engineProtectedUrls = getEngineProtectedUrls();
        boolean managementTokenRequired = engineProtectedUrls.contains(proxiedUrl);

        // Prepare URL to execute proxied request
        URI uri = new URI(getAuthoringUrl(siteId));
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(uri)
                .path(proxiedUrl)
                .query(request.getQueryString());
        if (managementTokenRequired) {
            uriComponentsBuilder = uriComponentsBuilder.queryParam("token", getEngineManagementTokenValue());
        }
        uri = uriComponentsBuilder.build(true).toUri();

        // Copy all headers
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));}

        // Execute proxied request and return response
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        try {
            return restTemplate.exchange(uri, method, httpEntity, Object.class);
        } catch(HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        }
    }

    /**
     * Returns the full authoring url used for preview
     */
    protected String getAuthoringUrl(String siteId) {
        return servicesConfig.getAuthoringUrl(siteId);
    }

    /**
     * Returns the management token for preview
     */
    protected String getEngineManagementTokenValue() {
        return studioConfiguration.getProperty(CONFIGURATION_MANAGEMENT_PREVIEW_AUTHORIZATION_TOKEN);
    }

    /**
     * Returns the list of preview URLs that require the management token
     */
    protected List<String> getEngineProtectedUrls() {
        return Arrays.asList(
                studioConfiguration.getProperty(CONFIGURATION_MANAGEMENT_PREVIEW_PROTECTED_URLS).split(","));
    }

}
