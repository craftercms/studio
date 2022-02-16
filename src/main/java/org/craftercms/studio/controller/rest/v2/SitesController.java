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

import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.service.marketplace.MarketplaceService;
import org.craftercms.studio.api.v2.service.policy.PolicyService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.model.policy.ValidationResult;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.marketplace.CreateSiteRequest;
import org.craftercms.studio.model.rest.sites.UpdateSiteRequest;
import org.craftercms.studio.model.rest.sites.ValidatePolicyRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_BLUEPRINTS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_RESULTS;

@RestController
@RequestMapping("/api/2/sites")
public class SitesController {

    private SitesService sitesService;

    private MarketplaceService marketplaceService;

    private PolicyService policyService;

    @ConstructorProperties({"sitesService", "marketplaceService", "policyService"})
    public SitesController(SitesService sitesService, MarketplaceService marketplaceService,
                           PolicyService policyService) {
        this.sitesService = sitesService;
        this.marketplaceService = marketplaceService;
        this.policyService = policyService;
    }

    @GetMapping("/available_blueprints")
    public ResponseBody getAvailableBlueprints() throws ServiceLayerException {
        List<PluginDescriptor> blueprintDescriptors = null;
        try {
            blueprintDescriptors = sitesService.getAvailableBlueprints();
        } catch (Exception e) {
            throw new ServiceLayerException(e);
        }
        ResponseBody responseBody = new ResponseBody();
        ResultList<PluginDescriptor> result = new ResultList<>();
        result.setEntities(RESULT_KEY_BLUEPRINTS, blueprintDescriptors);
        result.setResponse(ApiResponse.OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/create_site_from_marketplace")
    public ResponseBody createSite(@Valid @RequestBody CreateSiteRequest request)
        throws RemoteRepositoryNotFoundException, InvalidRemoteRepositoryException, ServiceLayerException,
        InvalidRemoteRepositoryCredentialsException, InvalidRemoteUrlException, RemoteRepositoryNotBareException {

        marketplaceService.createSite(request);

        Result result = new Result();
        result.setResponse(ApiResponse.CREATED);

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @PostMapping("/{siteId}")
    public ResponseBody updateSite(@PathVariable String siteId, @Valid @RequestBody UpdateSiteRequest request)
            throws SiteNotFoundException, SiteAlreadyExistsException, InvalidParametersException {
        if (isEmpty(request.getName()) && isEmpty(request.getDescription())) {
            throw new InvalidParametersException("The request needs to include a name or a description");
        }

        sitesService.updateSite(siteId, request.getName(), request.getDescription());

        var result = new Result();
        result.setResponse(ApiResponse.OK);

        var response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @PostMapping("/{siteId}/policy/validate")
    public ResponseBody validatePolicy(@PathVariable String siteId, @Valid @RequestBody ValidatePolicyRequest request)
            throws ConfigurationException, IOException, ContentNotFoundException {
        List<ValidationResult> results = policyService.validate(siteId, request.getActions());

        var result = new ResultList<ValidationResult>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_RESULTS, results);

        var response = new ResponseBody();
        response.setResult(result);

        return response;
    }
    
}
