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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.workflow.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.*;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;

@Validated
@RestController
@RequestMapping(API_2 + WORKFLOW)
public class WorkflowController {

    private final WorkflowService workflowService;
    private final PublishService publishService;

    @ConstructorProperties({"workflowService", "publishService"})
    public WorkflowController(final WorkflowService workflowService, final PublishService publishService) {
        this.workflowService = workflowService;
        this.publishService = publishService;
    }

    @GetMapping(value = ITEM_STATES, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<SandboxItem> getItemStates(@NotBlank @ValidSiteId @RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
                                                          @RequestParam(name = REQUEST_PARAM_PATH, required = false) String path,
                                                          @RequestParam(name = REQUEST_PARAM_STATES, required = false) Long states,
                                                          @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0")
                                                          int offset,
                                                          @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10")
                                                          int limit) throws SiteNotFoundException, InvalidParametersException {
        if (!isPathRegexValid(path)) {
            throw new InvalidParametersException("Parameter 'path' is not valid regular expression.");
        }
        int total = workflowService.getItemStatesTotal(siteId, path, states);
        List<SandboxItem> items = new ArrayList<>();

        if (total > offset) {
            items = workflowService.getItemStates(siteId, path, states, offset, limit);
        }

        PaginatedResultList<SandboxItem> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(isEmpty(items) ? 0 : items.size());
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS, items);
        return result;
    }

    private boolean isPathRegexValid(String pathRegex) {
        boolean toRet = true;
        try {
            Pattern.compile(pathRegex);
        } catch (Exception e) {
            toRet = false;
        }
        return toRet;
    }

    @PostMapping(value = ITEM_STATES, produces = APPLICATION_JSON_VALUE)
    public Result updateItemStates(@Valid @RequestBody ItemStatesPostRequestBody requestBody)
            throws SiteNotFoundException {
        ItemStatesUpdate update = requestBody.getUpdate();
        workflowService.updateItemStates(requestBody.getSiteId(), requestBody.getItems(),
                update.isClearSystemProcessing(), update.isClearUserLocked(), update.getLive(),
                update.getStaged(), update.getNew(), update.getModified());

        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = UPDATE_ITEM_STATES_BY_QUERY, produces = APPLICATION_JSON_VALUE)
    public Result updateItemStatesByQuery(@Valid @RequestBody UpdateItemStatesByQueryRequestBody requestBody)
            throws SiteNotFoundException {
        UpdateItemStatesByQueryRequestBody.Query query = requestBody.getQuery();
        ItemStatesUpdate update = requestBody.getUpdate();
        workflowService.updateItemStatesByQuery(query.getSiteId(), query.getPath(),
                query.getStates(), update.isClearSystemProcessing(),
                update.isClearUserLocked(), update.getLive(),
                update.getStaged(), update.getNew(), update.getModified());

        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @GetMapping(value = AFFECTED_PATHS, produces = APPLICATION_JSON_VALUE)
    public ResultList<SandboxItem> getWorkflowAffectedPaths(@ValidSiteId @RequestParam(REQUEST_PARAM_SITEID) String siteId,
                                                            @ValidExistingContentPath @RequestParam(REQUEST_PARAM_PATH) String path)
            throws ServiceLayerException, UserNotFoundException {
        List<SandboxItem> sandboxItems = workflowService.getWorkflowAffectedPaths(siteId, path);
        ResultList<SandboxItem> result = new ResultList<>();
        result.setEntities(RESULT_KEY_ITEMS, sandboxItems);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = REQUEST_PUBLISH, consumes = APPLICATION_JSON_VALUE)
    public Result requestPublish(@RequestBody @Valid RequestPublishRequestBody requestPublishRequestBody)
            throws ServiceLayerException, AuthenticationException {
        List<String> paths = new ArrayList<>(requestPublishRequestBody.getItems());
        if (!isEmpty(requestPublishRequestBody.getOptionalDependencies())) {
            paths.addAll(requestPublishRequestBody.getOptionalDependencies());
        }
        List<PublishService.PublishRequestPath> requestPaths =
                paths.stream()
                        .map(item -> new PublishService.PublishRequestPath(item, false, false))
                        .toList();
        Instant schedule = requestPublishRequestBody.getSchedule() != null ?
                requestPublishRequestBody.getSchedule().toInstant() : null;
        long packageId = publishService.requestPublish(requestPublishRequestBody.getSiteId(), requestPublishRequestBody.getPublishingTarget(),
                requestPaths, emptyList(), schedule, requestPublishRequestBody.getComment(), false);
        ResultOne<Long> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_PACKAGE_ID, packageId);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = PUBLISH, consumes = APPLICATION_JSON_VALUE)
    public Result publish(@Valid @RequestBody PublishRequestBody publishRequestBody)
            throws UserNotFoundException, ServiceLayerException, AuthenticationException {
        List<String> paths = new ArrayList<>(publishRequestBody.getItems());
        if (!isEmpty(publishRequestBody.getOptionalDependencies())) {
            paths.addAll(publishRequestBody.getOptionalDependencies());
        }
        List<PublishService.PublishRequestPath> requestPaths =
                paths
                        .stream()
                        .map(item -> new PublishService.PublishRequestPath(item, false, false))
                        .toList();
        Instant schedule = publishRequestBody.getSchedule() != null ?
                publishRequestBody.getSchedule().toInstant() : null;
        long packageId = publishService.publish(publishRequestBody.getSiteId(), publishRequestBody.getPublishingTarget(),
                requestPaths, emptyList(),
                schedule, publishRequestBody.getComment(), false);
        ResultOne<Long> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_PACKAGE_ID, packageId);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = PATH_PARAM_SITE + PACKAGE + PATH_PARAM_PACKAGE + APPROVE, consumes = APPLICATION_JSON_VALUE)
    public Result approve(@Valid @PathVariable @ValidSiteId String site, @Valid @PathVariable @Positive long packageId,
                          @Valid @RequestBody ApproveRequestBody request)
            throws UserNotFoundException, ServiceLayerException, AuthenticationException {
        workflowService.approvePackage(site, packageId,
                request.getSchedule(), request.getComment());

        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = PATH_PARAM_SITE + PACKAGE + PATH_PARAM_PACKAGE + REJECT, consumes = APPLICATION_JSON_VALUE)
    public Result reject(@Valid @PathVariable @ValidSiteId String site, @Valid @PathVariable @Positive long packageId,
                         @Valid @RequestBody ReviewPackageRequestBody rejectRequestBody)
            throws ServiceLayerException, AuthenticationException {
        workflowService.rejectPackage(site, packageId,
                rejectRequestBody.getComment());
        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @PostMapping(PATH_PARAM_SITE + PACKAGE + PATH_PARAM_PACKAGE + CANCEL)
    public Result cancel(@Valid @PathVariable @ValidSiteId String site, @Valid @PathVariable @Positive long packageId,
                         @Valid @RequestBody ReviewPackageRequestBody cancelPackageRequest)
            throws ServiceLayerException, AuthenticationException {
        workflowService.cancelPackage(site, packageId, cancelPackageRequest.getComment());
        Result result = new Result();
        result.setResponse(OK);
        return result;
    }
}
