/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.marketplace;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.monitoring.VersionInfo;
import org.craftercms.commons.plugin.model.Version;
import org.craftercms.commons.rest.RestTemplate;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.marketplace.MarketplaceService;
import org.craftercms.studio.api.v2.service.marketplace.Constants;
import org.craftercms.studio.api.v2.service.marketplace.Paths;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Default implementation of {@link MarketplaceService} that proxies all request to the configured Marketplace
 *
 * @author joseross
 * @since 3.1.2
 */
public class MarketplaceServiceImpl implements MarketplaceService, InitializingBean {

    public MarketplaceServiceImpl(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    protected InstanceService instanceService;

    protected RestTemplate restTemplate = new RestTemplate();

    /**
     * The custom HTTP headers to sent with all requests
     */
    protected HttpHeaders httpHeaders;

    /**
     * The current Crafter CMS version, sent with all requests
     */
    protected String version;

    /**
     * The current Crafter CMS edition, sent with all requests
     */
    protected String edition;

    /**
     * The Marketplace URL to use
     */
    protected String url;

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        VersionInfo versionInfo = VersionInfo.getVersion(MarketplaceServiceImpl.class);
        String versionStr = versionInfo.getPackageVersion();

        // init version
        version = Version.getVersion(versionStr);
        edition = Version.getEdition(versionStr);

        // init headers
        httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER_STUDIO_ID, instanceService.getInstanceId());

        httpHeaders.set(HEADER_STUDIO_BUILD, versionInfo.getPackageBuild());
        httpHeaders.set(HEADER_STUDIO_VERSION, versionInfo.getPackageVersion());
        httpHeaders.set(HEADER_JAVA_VERSION, versionStr);

        httpHeaders.set(HEADER_OS_NAME, versionInfo.getOsName());
        httpHeaders.set(HEADER_OS_VERSION, versionInfo.getOsVersion());
        httpHeaders.set(HEADER_OS_ARCH, versionInfo.getOsArch());
    }

    @Override
    public Map<String, Object> searchPlugins(final String type, final String keywords, final long offset,
                                             final long limit)
        throws ServiceLayerException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .path(Paths.PLUGIN_SEARCH)
            .queryParam(Constants.PARAM_VERSION, version)
            .queryParam(Constants.PARAM_EDITION, edition)
            .queryParam(Constants.PARAM_OFFSET, offset)
            .queryParam(Constants.PARAM_LIMIT, limit);

        if (StringUtils.isNotEmpty(type)) {
            builder.queryParam(Constants.PARAM_TYPE, type);
        }

        if (StringUtils.isNotEmpty(keywords)) {
            builder.queryParam(Constants.PARAM_KEYWORDS, keywords);
        }

        HttpEntity<Void> request = new HttpEntity<>(null, httpHeaders);

        try {
            ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new ServiceLayerException("Marketplace is not available at " + url, e);
        }
    }

}
