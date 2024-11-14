/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.SqlSort;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.publish.PublishPackageNotFoundException;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.publish.PublishService.PublishDependenciesResult;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.publish.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.USERNAME;
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

    @GetMapping(PATH_PARAM_SITE + PACKAGES)
    public PaginatedResultList<PublishPackage> getPublishingPackages(@ValidSiteId @PathVariable String site,
                                                                     @EsapiValidatedParam(type = ALPHANUMERIC) @Size(max = 20)
                                                                     @RequestParam(name = REQUEST_PARAM_TARGET, required = false)
                                                                     String target,
                                                                     @RequestParam(name = REQUEST_PARAM_STATES, required = false) Long states,
                                                                     @RequestParam(name = REQUEST_PARAM_APPROVAL_STATES, required = false)
                                                                     List<PublishPackage.ApprovalState> approvalStates,
                                                                     @RequestParam(name = REQUEST_PARAM_SUBMITTER, required = false)
                                                                     @EsapiValidatedParam(type = USERNAME) String submitter,
                                                                     @RequestParam(name = REQUEST_PARAM_REVIEWER, required = false)
                                                                     @EsapiValidatedParam(type = USERNAME) String reviewer,
                                                                     @RequestParam(name = REQUEST_PARAM_IS_SCHEDULED, required = false)
                                                                     Boolean isScheduled,
                                                                     @RequestParam(name = REQUEST_PARAM_SORT, required = false)
                                                                     List<@SqlSort(columns = PUBLISH_PACKAGES_SORT_FIELDS) SortField> sort,
                                                                     @RequestParam(name = REQUEST_PARAM_OFFSET, required = false,
                                                                             defaultValue = "0") @PositiveOrZero int offset,
                                                                     @RequestParam(name = REQUEST_PARAM_LIMIT, required = false,
                                                                             defaultValue = "10") @PositiveOrZero int limit)
            throws SiteNotFoundException {
        long total = publishService.getPublishingPackagesCount(site, target, states, approvalStates, submitter, reviewer, isScheduled);
        Collection<PublishPackage> packages = new ArrayList<>();
        if (total > 0) {
            packages = publishService.getPublishingPackages(site, target, states, approvalStates, submitter, reviewer, isScheduled, sort, offset, limit);
        }

        PaginatedResultList<PublishPackage> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(isEmpty(packages) ? 0 : packages.size());
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PACKAGES, packages);
        return result;
    }

    @GetMapping(PATH_PARAM_SITE + PACKAGE + PATH_PARAM_PACKAGE)
    public ResultOne<PublishPackageDetails> getPublishingPackage(@PathVariable @ValidSiteId String site,
                                                                 @PathVariable @Positive long packageId)
            throws SiteNotFoundException, PublishPackageNotFoundException {
        PublishPackageDetails publishingPackageDetails =
                publishService.getPublishingPackageDetails(site, packageId);
        ResultOne<PublishPackageDetails> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_PACKAGE, publishingPackageDetails);
        result.setResponse(OK);
        return result;
    }

    @GetMapping(PATH_PARAM_SITE + PACKAGE + PATH_PARAM_PACKAGE + STATUS)
    public ResultOne<PublishPackage> getPublishPackageStatus(@PathVariable @ValidSiteId String site, @PathVariable @Positive long packageId)
            throws PublishPackageNotFoundException, SiteNotFoundException {
        // TODO: add the package status (progress) to the response
        PublishPackage publishPackage = publishService.getPackage(site, packageId);
        ResultOne<PublishPackage> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_PACKAGE, publishPackage);
        result.setResponse(OK);
        return result;
    }

    @GetMapping(STATUS)
    public ResultOne<PublishStatus> getPublishingStatus(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        PublishStatus status = sitesService.getPublishingStatus(siteId);
        ResultOne<PublishStatus> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_PUBLISH_STATUS, status);
        result.setResponse(OK);
        return result;
    }

    @GetMapping(value = AVAILABLE_TARGETS, produces = APPLICATION_JSON_VALUE)
    public AvailablePublishingTargets getAvailablePublishingTargets(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        var availableTargets = publishService.getAvailablePublishingTargets(siteId);
        var published = publishService.isSitePublished(siteId);
        AvailablePublishingTargets availablePublishingTargets = new AvailablePublishingTargets();
        availablePublishingTargets.setPublishingTargets(availableTargets);
        availablePublishingTargets.setPublished(published);
        availablePublishingTargets.setResponse(OK);
        return availablePublishingTargets;
    }

    @Valid
    @GetMapping(value = HAS_INITIAL_PUBLISH, produces = APPLICATION_JSON_VALUE)
    public ResultOne<Boolean> hasInitialPublish(@ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        var published = publishService.isSitePublished(siteId);
        ResultOne<Boolean> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_HAS_INITIAL_PUBLISH, published);
        return result;
    }

    @PostMapping(PATH_PARAM_SITE + DEPENDENCIES)
    public ResultOne<PublishDependenciesResult> getPublishDependencies(@PathVariable @NotEmpty @ValidSiteId String site,
                                                                       @Validated @RequestBody GetPublishDependenciesRequest request)
            throws ServiceLayerException, IOException {
        PublishDependenciesResult dependenciesPackage = publishService.getPublishDependencies(site,
                request.getPublishingTarget(), request.getPaths(), request.getCommitIds());

        ResultOne<PublishDependenciesResult> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PACKAGE, dependenciesPackage);
        return result;
    }

    @PostMapping(ENABLE_PUBLISHER)
    public Result enablePublisher(@Validated @RequestBody EnablePublisherRequest request) {
        sitesService.enablePublishing(request.getSiteId(), request.isEnable());
        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @PostMapping(PATH_PARAM_SITE)
    public ResultOne<Long> publish(@PathVariable @NotEmpty @ValidSiteId String site,
                                   @Validated @RequestBody PublishPackageRequest request)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        long packageId = submitPublishPackage(site, request);

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
     */
    private long submitPublishPackage(String siteId, PublishPackageRequest request)
            throws ServiceLayerException, AuthenticationException {
        if (request.isRequestApproval()) {
            return publishService.requestPublish(siteId, request.getPublishingTarget(),
                    request.getPaths(), request.getCommitIds(), request.getSchedule(),
                    request.getComment(), request.isPublishAll());
        }
        return publishService.publish(siteId, request.getPublishingTarget(), request.getPaths(),
                request.getCommitIds(), request.getSchedule(),
                request.getComment(), request.isPublishAll());
    }

}
