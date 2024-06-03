/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import jakarta.validation.constraints.PositiveOrZero;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryGroup;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.PublishingPackageNotFoundException;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.publish.PublishService.PublishDependenciesResult;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.*;
import org.craftercms.studio.model.rest.publish.AvailablePublishingTargets;
import org.craftercms.studio.model.rest.publish.GetPublishDependenciesRequest;
import org.craftercms.studio.model.rest.publish.PublishPackageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.RESULT_KEY_PACKAGE;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.*;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(API_2 + PUBLISH)
public class PublishController {

    private final PublishService publishService;
    private final SitesService sitesService;

    @ConstructorProperties({"publishService", "sitesService"})
    public PublishController(final PublishService publishService, final SitesService sitesService) {
        this.publishService = publishService;
        this.sitesService = sitesService;
    }

    @GetMapping(PACKAGES)
    public PaginatedResultList<PublishingPackage> getPublishingPackages(@ValidSiteId
                                                                        @RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
                                                                        @EsapiValidatedParam(type = ALPHANUMERIC)
                                                                        @RequestParam(name = REQUEST_PARAM_ENVIRONMENT, required = false)
                                                                        String environment,
                                                                        @ValidExistingContentPath
                                                                        @RequestParam(name = REQUEST_PARAM_PATH, required = false) String path,
                                                                        @RequestParam(name = REQUEST_PARAM_STATES, required = false)
                                                                        List<@ValidateNoTagsParam String> states,
                                                                        @RequestParam(name = REQUEST_PARAM_OFFSET, required = false,
                                                                                defaultValue = "0") @PositiveOrZero int offset,
                                                                        @RequestParam(name = REQUEST_PARAM_LIMIT, required = false,
                                                                                defaultValue = "10") @PositiveOrZero int limit) throws SiteNotFoundException {
        int total = publishService.getPublishingPackagesTotal(siteId, environment, path, states);
        List<PublishingPackage> packages = new ArrayList<>();
        if (total > 0) {
            packages = publishService.getPublishingPackages(siteId, environment, path, states, offset, limit);
        }

        PaginatedResultList<PublishingPackage> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(isEmpty(packages) ? 0 : packages.size());
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PACKAGES, packages);
        return result;
    }

    @GetMapping(PACKAGE)
    public ResponseBody getPublishingPackageDetails(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
                                                    @RequestParam(name = REQUEST_PARAM_PACKAGE_ID) UUID packageId)
            throws SiteNotFoundException, PublishingPackageNotFoundException {
        PublishingPackageDetails publishingPackageDetails =
                publishService.getPublishingPackageDetails(siteId, packageId.toString());
        ResponseBody responseBody = new ResponseBody();
        ResultOne<PublishingPackageDetails> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_PACKAGE, publishingPackageDetails);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(CANCEL)
    public ResponseBody cancelPublishingPackages(
            @Valid @RequestBody CancelPublishingPackagesRequest cancelPublishingPackagesRequest)
            throws ServiceLayerException, UserNotFoundException {
        String siteId = cancelPublishingPackagesRequest.getSiteId();
        publishService.cancelPublishingPackages(siteId, cancelPublishingPackagesRequest.getPackageIds());
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(STATUS)
    public ResponseBody getPublishingStatus(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        PublishStatus status = sitesService.getPublishingStatus(siteId);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<PublishStatus> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_PUBLISH_STATUS, status);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = HISTORY, produces = APPLICATION_JSON_VALUE)
    public ResultList<DeploymentHistoryGroup> getPublishingHistory(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
                                                                   @PositiveOrZero @RequestParam(name = REQUEST_PARAM_DAYS) int daysFromToday,
                                                                   @PositiveOrZero @RequestParam(name = REQUEST_PARAM_NUM) int numberOfItems,
                                                                   @EsapiValidatedParam(type = ALPHANUMERIC) @RequestParam(name = REQUEST_PARAM_FILTER_TYPE, required = false,
                                                                           defaultValue = "page") String filterType)
            throws ServiceLayerException, UserNotFoundException {
        List<DeploymentHistoryGroup> history =
                publishService.getDeploymentHistory(siteId, daysFromToday, numberOfItems, filterType);
        PaginatedResultList<DeploymentHistoryGroup> result = new PaginatedResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PUBLISH_HISTORY, history);
        result.setOffset(0);
        result.setLimit(history.size());
        result.setTotal(history.size());
        return result;
    }

    @GetMapping(value = AVAILABLE_TARGETS, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getAvailablePublishingTargets(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        var availableTargets = publishService.getAvailablePublishingTargets(siteId);
        var published = publishService.isSitePublished(siteId);
        AvailablePublishingTargets availablePublishingTargets = new AvailablePublishingTargets();
        availablePublishingTargets.setPublishingTargets(availableTargets);
        availablePublishingTargets.setPublished(published);

        ResponseBody responseBody = new ResponseBody();
        availablePublishingTargets.setResponse(OK);
        responseBody.setResult(availablePublishingTargets);
        return responseBody;
    }

    @Valid
    @GetMapping(value = HAS_INITIAL_PUBLISH, produces = APPLICATION_JSON_VALUE)
    public ResponseBody hasInitialPublish(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        var published = publishService.isSitePublished(siteId);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<Boolean> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_HAS_INITIAL_PUBLISH, published);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/all")
    @Deprecated
    public ResultOne<Long> publishAll(@Validated @RequestBody PublishAllRequest request)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        long packageId = publishService.publish(request.getSiteId(), request.getPublishingTarget(),
                null, null, null, request.getSubmissionComment(), true);

        ResultOne<Long> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PACKAGE_ID, packageId);
        return result;
    }

    @PostMapping(DEPENDENCIES)
    public ResultOne<PublishDependenciesResult> getPublishDependencies(@Validated @RequestBody GetPublishDependenciesRequest request)
            throws ServiceLayerException, IOException {
        PublishDependenciesResult dependenciesPackage = publishService.getPublishDependencies(request.getSiteId(),
                request.getPublishingTarget(), request.getPaths(), request.getCommitIds());

        ResultOne<PublishDependenciesResult> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PACKAGE, dependenciesPackage);
        return result;
    }

    @PostMapping
    public ResultOne<Long> publish(@Validated @RequestBody PublishPackageRequest request)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        long packageId = submitPublishPackage(request);

        ResultOne<Long> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PACKAGE_ID, packageId);
        return result;
    }

    /**
     * Submit a publish package request
     *
     * @param request the request
     * @return the package id
     * @throws ServiceLayerException
     * @throws AuthenticationException
     */
    private long submitPublishPackage(PublishPackageRequest request)
            throws ServiceLayerException, AuthenticationException {
        if (request.isRequestApproval()) {
            return publishService.requestPublish(request.getSiteId(), request.getPublishingTarget(),
                    request.getPaths(), request.getCommitIds(), request.getSchedule(),
                    request.getComment(), true);
        }
        return publishService.publish(request.getSiteId(), request.getPublishingTarget(), request.getPaths(),
                request.getCommitIds(), request.getSchedule(),
                request.getComment(), true);
    }

}
