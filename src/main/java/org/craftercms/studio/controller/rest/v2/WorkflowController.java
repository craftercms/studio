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

import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.workflow.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.HTTPURI;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;
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

    @ConstructorProperties({"workflowService"})
    public WorkflowController(final WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping(value = ITEM_STATES, produces = APPLICATION_JSON_VALUE)
    public PaginatedResultList<SandboxItem> getItemStates(@NotBlank @EsapiValidatedParam(type = SITE_ID) @RequestParam(name = REQUEST_PARAM_SITEID) String siteId,
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
    public ResultList<SandboxItem> getWorkflowAffectedPaths(@EsapiValidatedParam(type = SITE_ID) @RequestParam(REQUEST_PARAM_SITEID) String siteId,
                                                            @ValidateSecurePathParam @EsapiValidatedParam(type = HTTPURI) @RequestParam(REQUEST_PARAM_PATH) String path)
            throws ServiceLayerException, UserNotFoundException {
        List<SandboxItem> sandboxItems = workflowService.getWorkflowAffectedPaths(siteId, path);
        ResultList<SandboxItem> result = new ResultList<>();
        result.setEntities(RESULT_KEY_ITEMS, sandboxItems);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = REQUEST_PUBLISH, consumes = APPLICATION_JSON_VALUE)
    public Result requestPublish(@RequestBody @Valid RequestPublishRequestBody requestPublishRequestBody)
            throws ServiceLayerException, UserNotFoundException, DeploymentException {
        workflowService.requestPublish(requestPublishRequestBody.getSiteId(), requestPublishRequestBody.getItems(),
                requestPublishRequestBody.getOptionalDependencies(), requestPublishRequestBody.getPublishingTarget(),
                requestPublishRequestBody.getSchedule(), requestPublishRequestBody.getComment(),
                requestPublishRequestBody.isSendEmailNotifications());

        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = PUBLISH, consumes = APPLICATION_JSON_VALUE)
    public Result publish(@Valid @RequestBody PublishRequestBody publishRequestBody)
            throws UserNotFoundException, ServiceLayerException, DeploymentException {
        workflowService.publish(publishRequestBody.getSiteId(), publishRequestBody.getItems(),
                publishRequestBody.getOptionalDependencies(), publishRequestBody.getPublishingTarget(),
                publishRequestBody.getSchedule(), publishRequestBody.getComment());

        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = APPROVE, consumes = APPLICATION_JSON_VALUE)
    public Result approve(@Valid @RequestBody ApproveRequestBody approveRequestBody)
            throws UserNotFoundException, ServiceLayerException, DeploymentException {
        workflowService.approve(approveRequestBody.getSiteId(), approveRequestBody.getItems(),
                approveRequestBody.getOptionalDependencies(), approveRequestBody.getPublishingTarget(),
                approveRequestBody.getSchedule(), approveRequestBody.getComment());

        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @PostMapping(value = REJECT, consumes = APPLICATION_JSON_VALUE)
    public Result reject(@Valid @RequestBody RejectRequestBody rejectRequestBody)
            throws ServiceLayerException, DeploymentException, UserNotFoundException {
        workflowService.reject(rejectRequestBody.getSiteId(), rejectRequestBody.getItems(),
                rejectRequestBody.getComment());
        Result result = new Result();
        result.setResponse(OK);
        return result;
    }
}
