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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.exception.content.ContentAlreadyUnlockedException;
import org.craftercms.studio.api.v2.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.clipboard.DuplicateRequest;
import org.craftercms.studio.model.rest.clipboard.PasteRequest;
import org.craftercms.studio.model.rest.content.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_COMMIT_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PREFER_CONTENT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.CONTENT;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DELETE;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DUPLICATE_ITEM;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_CHILDREN_BY_ID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_CHILDREN_BY_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_CONTENT_BY_COMMIT_ID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_DELETE_PACKAGE;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_DESCRIPTOR;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ITEM_BY_ID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ITEM_BY_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ITEM_LOCK_BY_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ITEM_UNLOCK_BY_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.LIST_QUICK_CREATE_CONTENT;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PASTE_ITEMS;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.SANDBOX_ITEMS_BY_ID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.SANDBOX_ITEMS_BY_PATH;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CHILD_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_DEPENDENT_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEM;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_XML;
import static org.craftercms.studio.model.rest.ApiResponse.CONTENT_NOT_FOUND;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(API_2 + CONTENT)
public class ContentController {

    private final ContentService contentService;
    private final SiteService siteService;
    private final DependencyService dependencyService;
    private final WorkflowService workflowService;

    //TODO: Migrate logic to new content service
    private final ClipboardService clipboardService;

    @ConstructorProperties({"contentService", "siteService", "dependencyService", "clipboardService",
            "workflowService"})
    public ContentController(ContentService contentService, SiteService siteService,
                             DependencyService dependencyService, ClipboardService clipboardService,
                             WorkflowService workflowService) {
        this.contentService = contentService;
        this.siteService = siteService;
        this.dependencyService = dependencyService;
        this.clipboardService = clipboardService;
        this.workflowService = workflowService;
    }

    @GetMapping(LIST_QUICK_CREATE_CONTENT)
    public ResponseBody listQuickCreateContent(@RequestParam(name = "siteId") String siteId)
            throws SiteNotFoundException {

        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        List<QuickCreateItem> items = contentService.getQuickCreatableContentTypes(siteId);

        ResponseBody responseBody = new ResponseBody();
        ResultList<QuickCreateItem> result = new ResultList<QuickCreateItem>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS, items);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(GET_DELETE_PACKAGE)
    public ResponseBody getDeletePackage(@RequestBody @Valid GetDeletePackageRequestBody request) {
        List<String> childItems = contentService.getChildItems(request.getSiteId(), request.getPaths());
        List<String> dependentItems = dependencyService.getDependentItems(request.getSiteId(), request.getPaths());
        ResponseBody responseBody = new ResponseBody();
        ResultOne<Map<String, List<String>>> result = new ResultOne<Map<String, List<String>>>();
        result.setResponse(OK);
        Map<String, List<String>> items = new HashMap<String, List<String>>();
        items.put(RESULT_KEY_CHILD_ITEMS, childItems);
        items.put(RESULT_KEY_DEPENDENT_ITEMS, dependentItems);
        result.setEntity(RESULT_KEY_ITEMS, items);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = DELETE, consumes = APPLICATION_JSON_VALUE)
    public ResponseBody delete(@RequestBody DeleteRequestBody deleteRequestBody)
            throws UserNotFoundException, ServiceLayerException, DeploymentException {
        workflowService.delete(deleteRequestBody.getSiteId(), deleteRequestBody.getItems(),
                deleteRequestBody.getOptionalDependencies(), deleteRequestBody.getComment());

        var responseBody = new ResponseBody();
        var result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = GET_CHILDREN_BY_PATH, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getChildrenByPath(@RequestBody @Valid GetChildrenByPathRequestBody request)
            throws ServiceLayerException, UserNotFoundException {
        if (!siteService.exists(request.getSiteId())) {
            throw new SiteNotFoundException(request.getSiteId());
        }
        GetChildrenResult result = contentService.getChildrenByPath(
                request.getSiteId(), request.getPath(), request.getLocaleCode(), request.getKeyword(),
                request.getExcludes(), request.getSortStrategy(), request.getOrder(), request.getOffset(),
                request.getLimit());
        ResponseBody responseBody = new ResponseBody();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = GET_CHILDREN_BY_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getChildrenById(@RequestBody @Valid GetChildrenByIdRequestBody request)
            throws ServiceLayerException, UserNotFoundException {
        if (!siteService.exists(request.getSiteId())) {
            throw new SiteNotFoundException(request.getSiteId());
        }
        GetChildrenResult result =
                contentService.getChildrenById(
                        request.getSiteId(), request.getId(), request.getLocaleCode(), request.getKeyword(),
                        request.getExcludes(), request.getSortStrategy(), request.getOrder(), request.getOffset(),
                        request.getLimit());
        ResponseBody responseBody = new ResponseBody();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = GET_DESCRIPTOR, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getDescriptor(@RequestParam String siteId, @RequestParam String path,
                                      @RequestParam(required = false, defaultValue = "false") boolean flatten) throws
            ContentNotFoundException {
        var item = contentService.getItem(siteId, path, flatten);
        var descriptor = item.getDescriptorDom();
        if (descriptor == null) {
            throw new ContentNotFoundException(path, siteId, "No descriptor found for " + path + " in site " + siteId);
        }

        var result = new ResultOne<String>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_XML, descriptor.asXML());

        var response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @PostMapping(value = PASTE_ITEMS, produces = APPLICATION_JSON_VALUE)
    public ResponseBody pasteItems(@Valid @RequestBody PasteRequest request) throws Exception {
        var result = new ResultList<String>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ITEMS,
                clipboardService.pasteItems(request.getSiteId(), request.getOperation(),
                        request.getTargetPath(), request.getItem()));

        var response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @PostMapping(value = DUPLICATE_ITEM, produces = APPLICATION_JSON_VALUE)
    public ResponseBody duplicateItem(@Valid @RequestBody DuplicateRequest request) throws Exception {
        var result = new ResultOne<String>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_ITEM,
                clipboardService.duplicateItem(request.getSiteId(), request.getPath()));

        var response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @GetMapping(value = ITEM_BY_PATH, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getItemByPath(@RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
                                      @RequestParam(value = REQUEST_PARAM_PATH) String path,
                                      @RequestParam(value = REQUEST_PARAM_PREFER_CONTENT, required = false,
                                              defaultValue = "false") boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        DetailedItem detailedItem = contentService.getItemByPath(siteId, path, preferContent);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<DetailedItem> result = new ResultOne<DetailedItem>();
        result.setEntity(RESULT_KEY_ITEM, detailedItem);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = ITEM_BY_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getItemById(@RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
                                    @RequestParam(value = REQUEST_PARAM_ID) long id,
                                    @RequestParam(value = REQUEST_PARAM_PREFER_CONTENT, required = false,
                                            defaultValue = "false") boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        DetailedItem detailedItem = contentService.getItemById(siteId, id, preferContent);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<DetailedItem> result = new ResultOne<DetailedItem>();
        result.setEntity(RESULT_KEY_ITEM, detailedItem);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = SANDBOX_ITEMS_BY_PATH, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getSandboxItemsByPath(@RequestBody @Valid GetSandboxItemsByPathRequestBody request,
                                              HttpServletResponse httpServletResponse)
            throws ServiceLayerException, UserNotFoundException {
        String siteId = request.getSiteId();
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(request.getSiteId());
        }
        Collection<String> missing = Collections.emptyList();
        List<String> paths = request.getPaths();
        boolean preferContent = request.isPreferContent();
        List<SandboxItem> sandboxItems = contentService.getSandboxItemsByPath(siteId, paths, preferContent);
        ResponseBody responseBody = new ResponseBody();

        if (CollectionUtils.isEmpty(sandboxItems) || paths.size() != sandboxItems.size()) {
            List<String> found = sandboxItems.stream().map(SandboxItem::getPath).collect(Collectors.toList());
            if (preferContent) {
                found.addAll(sandboxItems.stream().map(si -> StringUtils.replace(si.getPath(),
                        FILE_SEPARATOR + INDEX_FILE, "")).collect(Collectors.toList()));
            }
            missing = CollectionUtils.subtract(paths, found);
        }

        GetSandboxItemsByPathResult result = new GetSandboxItemsByPathResult();
        result.setEntities(RESULT_KEY_ITEMS, sandboxItems);
        result.setMissingItems(missing);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = SANDBOX_ITEMS_BY_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getSandboxItemsById(@RequestBody @Valid GetSandboxItemsByIdRequestBody request,
                                            HttpServletResponse httpServletResponse)
            throws ServiceLayerException, UserNotFoundException {
        if (!siteService.exists(request.getSiteId())) {
            throw new SiteNotFoundException(request.getSiteId());
        }
        List<Long> ids = request.getIds();
        String siteId = request.getSiteId();
        boolean preferContent = request.isPreferContent();
        List<SandboxItem> sandboxItems = contentService.getSandboxItemsById(siteId, ids, preferContent);

        ResponseBody responseBody = new ResponseBody();
        if (CollectionUtils.isEmpty(sandboxItems)) {
            Result result = new Result();
            ApiResponse apiResponse = new ApiResponse(CONTENT_NOT_FOUND);
            apiResponse.setRemedialAction(
                    String.format("None of sent content ids was found. Check that they are correct and it exist in " +
                            "site '%s'", siteId));
            result.setResponse(apiResponse);
            responseBody.setResult(result);
            httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
        } else if (ids.size() != sandboxItems.size()) {
            List<Long> found = sandboxItems.stream().map(SandboxItem::getId).collect(Collectors.toList());
            if (preferContent) {
                found.addAll(sandboxItems.stream().map(SandboxItem::getParentId).collect(Collectors.toList()));
            }
            Collection<Long> missing = CollectionUtils.subtract(ids, found);
            String missingIds = missing.stream().map(String::valueOf).collect(Collectors.joining(", "));
            Result result = new Result();
            ApiResponse apiResponse = new ApiResponse(CONTENT_NOT_FOUND);
            apiResponse.setRemedialAction(
                    String.format("Following content ids [%s] were not found. Check that they are correct and it " +
                            "exist in site '%s'", missingIds, siteId));
            result.setResponse(apiResponse);
            responseBody.setResult(result);
            httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
        } else {
            ResultList<SandboxItem> result = new ResultList<SandboxItem>();
            result.setEntities(RESULT_KEY_ITEMS, sandboxItems);
            result.setResponse(OK);
            responseBody.setResult(result);
        }
        return responseBody;
    }

    @PostMapping(ITEM_LOCK_BY_PATH)
    public ResponseBody itemLockByPath(@RequestBody @Valid LockItemByPathRequest request)
            throws UserNotFoundException, ServiceLayerException {
        if (!siteService.exists(request.getSiteId())) {
            throw new SiteNotFoundException(request.getSiteId());
        }
        contentService.lockContent(request.getSiteId(), request.getPath());
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(ITEM_UNLOCK_BY_PATH)
    public ResponseBody itemUnlockByPath(@RequestBody @Valid UnlockItemByPathRequest request)
            throws ContentNotFoundException, ContentAlreadyUnlockedException, SiteNotFoundException {
        //TODO: The service should throw this exception, not the controller
        if (!siteService.exists(request.getSiteId())) {
            throw new SiteNotFoundException(request.getSiteId());
        }
        contentService.unlockContent(request.getSiteId(), request.getPath());
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(GET_CONTENT_BY_COMMIT_ID)
    public ResponseEntity<Resource> getContentByCommitId(@RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
                                             @RequestParam(value = REQUEST_PARAM_PATH) String path,
                                             @RequestParam(value = REQUEST_PARAM_COMMIT_ID) String commitId)
            throws ServiceLayerException, IOException, UserNotFoundException {

        DetailedItem item = contentService.getItemByPath(siteId, path, true);
        Resource resource = contentService.getContentByCommitId(siteId, path, commitId).orElseThrow();

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(item.getMimeType()))
                .body(resource);
    }
}
