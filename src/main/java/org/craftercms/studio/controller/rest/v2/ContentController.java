/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.SiteAwareBulkRequest;
import org.craftercms.studio.model.rest.clipboard.DuplicateRequest;
import org.craftercms.studio.model.rest.clipboard.PasteRequest;
import org.craftercms.studio.model.rest.content.DeleteContentRequest;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.GetChildrenByIdRequest;
import org.craftercms.studio.model.rest.content.GetChildrenByPathRequest;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.GetSandboxItemsByIdRequest;
import org.craftercms.studio.model.rest.content.GetSandboxItemsByPathRequest;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.model.rest.content.UnlockItemByIdRequest;
import org.craftercms.studio.model.rest.content.UnlockItemByPathRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.ITEM_UNLOCK_BY_ID;
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
import static org.craftercms.studio.model.rest.ApiResponse.DELETED;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(API_2 + CONTENT)
public class ContentController {

    private final ContentService contentService;
    private final SiteService siteService;
    private final DependencyService dependencyService;

    //TODO: Migrate logic to new content service
    private final ClipboardService clipboardService;

    @ConstructorProperties({"contentService", "siteService", "dependencyService", "clipboardService"})
    public ContentController(ContentService contentService, SiteService siteService,
                             DependencyService dependencyService, ClipboardService clipboardService) {
        this.contentService = contentService;
        this.siteService = siteService;
        this.dependencyService = dependencyService;
        this.clipboardService = clipboardService;
    }

    @GetMapping(LIST_QUICK_CREATE_CONTENT)
    public ResponseBody listQuickCreateContent(@RequestParam(name = "siteId", required = true) String siteId)
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
    public ResponseBody getDeletePackage(@RequestBody @Valid GetDeletePackageRequest request) {
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

    @PostMapping(DELETE)
    public ResponseBody delete(@RequestBody @Valid DeleteContentRequest request)
            throws DeploymentException, AuthenticationException, ServiceLayerException {
        contentService.deleteContent(request.getSiteId(), request.getPaths(), request.getSubmissionComment());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(DELETED);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = GET_CHILDREN_BY_PATH, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getChildrenByPath(@RequestBody @Valid GetChildrenByPathRequest request)
            throws ServiceLayerException, UserNotFoundException {
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
    public ResponseBody getChildrenById(@RequestBody @Valid GetChildrenByIdRequest request)
            throws ServiceLayerException, UserNotFoundException {
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
    public ResponseBody getItemByPath(@RequestParam(value = REQUEST_PARAM_SITEID, required = true) String siteId,
                                      @RequestParam(value = REQUEST_PARAM_PATH, required = true) String path,
                                      @RequestParam(value = REQUEST_PARAM_PREFER_CONTENT, required = false,
                                              defaultValue = "false") boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        DetailedItem detailedItem = contentService.getItemByPath(siteId, path, preferContent);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<DetailedItem> result = new ResultOne<DetailedItem>();
        result.setEntity(RESULT_KEY_ITEM, detailedItem);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = ITEM_BY_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getItemById(@RequestParam(value = REQUEST_PARAM_SITEID, required = true) String siteId,
                                    @RequestParam(value = REQUEST_PARAM_ID, required = true) long id,
                                    @RequestParam(value = REQUEST_PARAM_PREFER_CONTENT, required = false,
                                            defaultValue = "false") boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        DetailedItem detailedItem = contentService.getItemById(siteId, id, preferContent);
        ResponseBody responseBody = new ResponseBody();
        ResultOne<DetailedItem> result = new ResultOne<DetailedItem>();
        result.setEntity(RESULT_KEY_ITEM, detailedItem);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = SANDBOX_ITEMS_BY_PATH, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getSandboxItemsByPath(@RequestBody @Valid GetSandboxItemsByPathRequest request)
            throws ServiceLayerException, UserNotFoundException {
        List<SandboxItem> sandboxItems = contentService.getSandboxItemsByPath(
                request.getSiteId(), request.getPaths(), request.isPreferContent());
        ResponseBody responseBody = new ResponseBody();
        ResultList<SandboxItem> result = new ResultList<SandboxItem>();
        result.setEntities(RESULT_KEY_ITEMS, sandboxItems);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = SANDBOX_ITEMS_BY_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getSandboxItemsById(@RequestBody @Valid GetSandboxItemsByIdRequest request)
            throws ServiceLayerException, UserNotFoundException {
        List<SandboxItem> sandboxItems = contentService.getSandboxItemsById(
                request.getSiteId(), request.getIds(), request.isPreferContent());
        ResponseBody responseBody = new ResponseBody();
        ResultList<SandboxItem> result = new ResultList<SandboxItem>();
        result.setEntities(RESULT_KEY_ITEMS, sandboxItems);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(ITEM_UNLOCK_BY_PATH)
    public ResponseBody itemUnlockByPath(@RequestBody @Valid UnlockItemByPathRequest request) {
        contentService.itemUnlockByPath(request.getSiteId(), request.getPath());
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(ITEM_UNLOCK_BY_ID)
    public ResponseBody itemUnlockById(@RequestBody @Valid UnlockItemByIdRequest request) {
        contentService.itemUnlockById(request.getSiteId(), request.getItemId());
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(GET_CONTENT_BY_COMMIT_ID)
    public ResponseEntity<InputStreamResource> getContentByCommitId(@RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
                                             @RequestParam(value = REQUEST_PARAM_PATH) String path,
                                             @RequestParam(value = REQUEST_PARAM_COMMIT_ID) String commitId)
            throws ServiceLayerException, IOException, UserNotFoundException {

        DetailedItem item = contentService.getItemByPath(siteId, path, true);
        InputStream content = contentService.getContentByCommitId(siteId, path, commitId);

        InputStreamResource responseBody = new InputStreamResource(content);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(item.getMimeType()));
        httpHeaders.setContentLength(item.getSandbox().getSizeInBytes());
        return new ResponseEntity<InputStreamResource>(responseBody, httpHeaders, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GetDeletePackageRequest extends SiteAwareBulkRequest {

    }

}
