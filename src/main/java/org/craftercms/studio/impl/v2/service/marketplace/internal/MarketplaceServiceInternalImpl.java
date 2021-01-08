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

package org.craftercms.studio.impl.v2.service.marketplace.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.monitoring.VersionInfo;
import org.craftercms.commons.plugin.model.Plugin;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.plugin.model.Version;
import org.craftercms.commons.rest.RestTemplate;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.BlueprintNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceNotInitializedException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceUnreachableException;
import org.craftercms.studio.api.v2.service.marketplace.Constants;
import org.craftercms.studio.api.v2.service.marketplace.Paths;
import org.craftercms.studio.api.v2.service.marketplace.internal.MarketplaceServiceInternal;
import org.craftercms.studio.api.v2.service.site.internal.SitesServiceInternal;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.marketplace.CreateSiteRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.craftercms.studio.api.v2.service.marketplace.Constants.PLUGIN_REF;
import static org.craftercms.studio.api.v2.service.marketplace.Constants.PLUGIN_URL;

/**
 * Default implementation of {@link MarketplaceServiceInternal} that proxies all request to the configured Marketplace
 *
 * @author joseross
 * @since 3.1.2
 */
public class MarketplaceServiceInternalImpl implements MarketplaceServiceInternal, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MarketplaceServiceInternalImpl.class);

    public MarketplaceServiceInternalImpl(final InstanceService instanceService, final SiteService siteService,
                                          final SitesServiceInternal sitesServiceInternal,
                                          final StudioConfiguration studioConfiguration) {
        this.instanceService = instanceService;
        this.siteService = siteService;
        this.sitesServiceInternal = sitesServiceInternal;
        this.studioConfiguration = studioConfiguration;
    }

    protected InstanceService instanceService;

    protected SiteService siteService;

    protected SitesServiceInternal sitesServiceInternal;

    protected StudioConfiguration studioConfiguration;

    protected RestTemplate restTemplate = new RestTemplate();

    protected ObjectMapper mapper = new ObjectMapper();

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

    /**
     * Indicates if the search should include plugins pending of approval
     */
    protected boolean showPending = false;

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setShowPending(final boolean showPending) {
        this.showPending = showPending;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        VersionInfo versionInfo = VersionInfo.getVersion(MarketplaceServiceInternalImpl.class);
        if (versionInfo == null) {
            logger.warn("Marketplace service could not be initialized");
            return;
        }
        String versionStr = versionInfo.getPackageVersion();

        // init version
        version = Version.getVersion(versionStr);
        edition = Version.getEdition(versionStr);

        // init headers
        httpHeaders = new HttpHeaders();
        httpHeaders.set(HEADER_STUDIO_ID, instanceService.getInstanceId());

        httpHeaders.set(HEADER_STUDIO_BUILD, versionInfo.getPackageBuild());
        httpHeaders.set(HEADER_STUDIO_VERSION, versionInfo.getPackageVersion());
        httpHeaders.set(HEADER_JAVA_VERSION, versionInfo.getJavaVersion());

        httpHeaders.set(HEADER_OS_NAME, versionInfo.getOsName());
        httpHeaders.set(HEADER_OS_VERSION, versionInfo.getOsVersion());
        httpHeaders.set(HEADER_OS_ARCH, versionInfo.getOsArch());
    }

    @Override
    public Map<String, Object> searchPlugins(final String type, final String keywords, final boolean showIncompatible,
                                             final long offset, final long limit)
        throws MarketplaceException {

        validate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .path(Paths.PLUGIN_SEARCH)
            .queryParam(Constants.PARAM_VERSION, version)
            .queryParam(Constants.PARAM_EDITION, edition)
            .queryParam(Constants.PARAM_SHOW_PENDING, showPending)
            .queryParam(Constants.PARAM_SHOW_INCOMPATIBLE, showIncompatible)
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
                restTemplate.exchange(builder.build().toString(), HttpMethod.GET, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new MarketplaceUnreachableException(url, e);
        }
    }

    protected void validate() throws MarketplaceException {
        if (StringUtils.isEmpty(version)) {
            throw new MarketplaceNotInitializedException();
        }
    }

    protected Map<String, Object> getDescriptor(String id, Version version) throws MarketplaceException,
        BlueprintNotFoundException {

        validate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .path(Paths.GET_PLUGIN)
            .pathSegment(id, String.format("%s.%s.%s", version.getMajor(), version.getMinor(), version.getPatch()))
            .queryParam(Constants.PARAM_SHOW_PENDING, showPending);

        HttpEntity<Void> request = new HttpEntity<>(null, httpHeaders);

        try {
            ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(builder.build().toString(), HttpMethod.GET, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new MarketplaceUnreachableException(url, e);
        } catch (IllegalArgumentException e) {
            throw new BlueprintNotFoundException(String.format("Blueprint not found in the Marketplace: %s %s.%s.%s",
             id, version.getMajor(), version.getMinor(), version.getPatch()));
        }
    }

    @Override
    public void createSite(CreateSiteRequest request) throws RemoteRepositoryNotFoundException,
        InvalidRemoteRepositoryException, RemoteRepositoryNotBareException, InvalidRemoteUrlException,
        ServiceLayerException, InvalidRemoteRepositoryCredentialsException {

        if (StringUtils.isEmpty(request.getSandboxBranch())) {
            request.setSandboxBranch(studioConfiguration.getProperty(StudioConfiguration.REPO_SANDBOX_BRANCH));
        }

        if (StringUtils.isEmpty(request.getRemoteName())) {
            request.setRemoteName(studioConfiguration.getProperty(StudioConfiguration.REPO_DEFAULT_REMOTE_NAME));
        }

        Map<String, Object> result = getDescriptor(request.getBlueprintId(), request.getBlueprintVersion());
        Plugin plugin = mapper.convertValue(result, Plugin.class);

        sitesServiceInternal.validateBlueprintParameters(PluginDescriptor.of(plugin), request.getSiteParams());

        siteService.createSiteWithRemoteOption(request.getSiteId(), request.getSandboxBranch(),
            request.getDescription(), request.getBlueprintId(), request.getRemoteName(),
            result.get(PLUGIN_URL).toString(), result.get(PLUGIN_REF).toString(), false,
            RemoteRepository.AuthenticationType.NONE, null, null, null,
            null, StudioConstants.REMOTE_REPOSITORY_CREATE_OPTION_CLONE, request.getSiteParams(),
            true);
    }
}
