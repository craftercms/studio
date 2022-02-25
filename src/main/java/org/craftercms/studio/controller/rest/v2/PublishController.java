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

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.DeploymentHistoryGroup;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.dal.PublishingPackage;
import org.craftercms.studio.api.v2.dal.PublishingPackageDetails;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.model.rest.CancelPublishingPackagesRequest;
import org.craftercms.studio.model.rest.ClearPublishingLockRequest;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.publish.AvailablePublishingTargets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_DAYS;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ENVIRONMENT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_FILTER_TYPE;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LIMIT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_NUM;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OFFSET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PACKAGE_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_STATES;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.AVAILABLE_TARGETS;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.CANCEL;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.CLEAR_LOCK;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.HISTORY;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PACKAGE;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PACKAGES;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PUBLISH;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.STATUS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PACKAGE;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PACKAGES;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PUBLISH_HISTORY;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PUBLISH_STATUS;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(API_2 + PUBLISH)
public class PublishController {

    private PublishService publishService;
    private SiteService siteService;
    private SitesService sitesService;

    @GetMapping(PACKAGES)
    public ResponseBody getPublishingPackages(@RequestParam(name = REQUEST_PARAM_SITEID, required = true) String siteId,
                                              @RequestParam(name = REQUEST_PARAM_ENVIRONMENT, required = false)
                                                      String environment,
                                              @RequestParam(name = REQUEST_PARAM_PATH, required = false) String path,
                                              @RequestParam(name = REQUEST_PARAM_STATES, required = false)
                                                          List<String> states,
                                              @RequestParam(name = REQUEST_PARAM_OFFSET, required = false,
                                                      defaultValue = "0") int offset,
                                              @RequestParam(name = REQUEST_PARAM_LIMIT, required = false,
                                                      defaultValue = "10") int limit) throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        int total = publishService.getPublishingPackagesTotal(siteId, environment, path, states);
        List<PublishingPackage> packages = new ArrayList<PublishingPackage>();
        if (total > 0) {
            packages = publishService.getPublishingPackages(siteId, environment, path, states, offset, limit);
        }

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<PublishingPackage> result = new PaginatedResultList<PublishingPackage>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isEmpty(packages) ? 0 : packages.size());
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PACKAGES, packages);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(PACKAGE)
    public ResponseBody getPublishingPackageDetails(@RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
                                                    @RequestParam(name = REQUEST_PARAM_PACKAGE_ID) String packageId)
            throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        PublishingPackageDetails publishingPackageDetails =
                publishService.getPublishingPackageDetails(siteId, packageId);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<PublishingPackageDetails> result = new ResultOne<PublishingPackageDetails>();
        result.setEntity(RESULT_KEY_PACKAGE, publishingPackageDetails);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(CANCEL)
    public ResponseBody cancelPublishingPackages(
            @RequestBody CancelPublishingPackagesRequest cancelPublishingPackagesRequest)
            throws ServiceLayerException, UserNotFoundException {
        String siteId = cancelPublishingPackagesRequest.getSiteId();
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        publishService.cancelPublishingPackages(siteId, cancelPublishingPackagesRequest.getPackageIds());
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(STATUS)
    public ResponseBody getPublishingStatus(@RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        PublishStatus status = sitesService.getPublishingStatus(siteId);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<PublishStatus> result = new ResultOne<PublishStatus>();
        result.setEntity(RESULT_KEY_PUBLISH_STATUS, status);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = CLEAR_LOCK, consumes = APPLICATION_JSON_VALUE)
    public ResponseBody clearPublishingLock(@RequestBody ClearPublishingLockRequest clearPublishingLockRequest)
            throws SiteNotFoundException {
        String siteId = clearPublishingLockRequest.getSiteId();
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        sitesService.clearPublishingLock(siteId);
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = HISTORY, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getPublishingHistory(@RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
                                             @RequestParam(name = REQUEST_PARAM_DAYS) int daysFromToday,
                                             @RequestParam(name = REQUEST_PARAM_NUM) int numberOfItems,
                                             @RequestParam(name = REQUEST_PARAM_FILTER_TYPE, required = false,
                                                     defaultValue = "page") String filterType)
            throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        List<DeploymentHistoryGroup> history =
                publishService.getDeploymentHistory(siteId, daysFromToday, numberOfItems, filterType);
        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<DeploymentHistoryGroup> result = new PaginatedResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PUBLISH_HISTORY, history);
        result.setOffset(0);
        result.setLimit(history.size());
        result.setTotal(history.size());
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = AVAILABLE_TARGETS, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getAvailablePublishingTargets(@RequestParam(name = REQUEST_PARAM_SITEID) String siteId)
            throws SiteNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
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

    public PublishService getPublishService() {
        return publishService;
    }

    public void setPublishService(PublishService publishService) {
        this.publishService = publishService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public SitesService getSitesService() {
        return sitesService;
    }

    public void setSitesService(SitesService sitesService) {
        this.sitesService = sitesService;
    }
}
