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

package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LIMIT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LOCALE_CODE;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OFFSET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ORDER;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PATHS;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SORT_STRATEGY;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.CONTENT;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.DELETE;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_CHILDREN_BY_ID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_CHILDREN_BY_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GET_DELETE_PACKAGE;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.LIST_QUICK_CREATE_CONTENT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SUBMISSION_COMMENT;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CHILD_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_DEPENDENT_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.model.rest.ApiResponse.DELETED;
import static org.craftercms.studio.model.rest.ApiResponse.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(API_2 + CONTENT)
public class ContentController {

    private ContentService contentService;
    private SiteService siteService;
    private DependencyService dependencyService;

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

    @GetMapping(GET_DELETE_PACKAGE)
    public ResponseBody getDeletePackage(
            @RequestParam(value = REQUEST_PARAM_SITEID, required = true) String siteId,
            @RequestParam(value = REQUEST_PARAM_PATHS, required = true)List<String> paths) {
        List<String> childItems = new ArrayList<String>();
        List<String> dependentItems = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(paths)) {
            childItems = contentService.getChildItems(siteId, paths);
            dependentItems = dependencyService.getDependentItems(siteId, paths);
        }
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

    @DeleteMapping(DELETE)
    public ResponseBody delete(
            @RequestParam(value = REQUEST_PARAM_SITEID, required = true) String siteId,
            @RequestParam(value = REQUEST_PARAM_PATHS, required = true)List<String> paths,
            @RequestParam(value = REQUEST_PARAM_SUBMISSION_COMMENT, required = false) String submissionComment)
            throws DeploymentException, AuthenticationException, ServiceLayerException {
        contentService.deleteContent(siteId, paths, submissionComment);

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(DELETED);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = GET_CHILDREN_BY_PATH, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getChildrenByPath(@RequestParam(value = REQUEST_PARAM_SITEID, required = true) String siteId,
                                          @RequestParam(value = REQUEST_PARAM_PATH, required = true) String path,
                                          @RequestParam(value = REQUEST_PARAM_LOCALE_CODE, required = false) String localeCode,
                                          @RequestParam(value = REQUEST_PARAM_SORT_STRATEGY, required = false)
                                                      String sortStrategy,
                                          @RequestParam(value = REQUEST_PARAM_ORDER, required = false) String order,
                                          @RequestParam(value = REQUEST_PARAM_OFFSET, required = false,
                                                  defaultValue = "0") int offset,
                                          @RequestParam(value = REQUEST_PARAM_LIMIT, required = false,
                                                  defaultValue = "10") int limit) {
        GetChildrenResult result =
                contentService.getChildrenByPath(siteId, path, localeCode, sortStrategy, order, offset, limit);
        ResponseBody responseBody = new ResponseBody();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping(value = GET_CHILDREN_BY_ID, produces = APPLICATION_JSON_VALUE)
    public ResponseBody getChildrenById(@RequestParam(value = REQUEST_PARAM_SITEID, required = true) String siteId,
                                          @RequestParam(value = REQUEST_PARAM_ID, required = true) String id,
                                          @RequestParam(value = REQUEST_PARAM_LOCALE_CODE, required = false) String localeCode,
                                          @RequestParam(value = REQUEST_PARAM_SORT_STRATEGY, required = false)
                                                  String sortStrategy,
                                          @RequestParam(value = REQUEST_PARAM_ORDER, required = false) String order,
                                          @RequestParam(value = REQUEST_PARAM_OFFSET, required = false,
                                                  defaultValue = "0") int offset,
                                          @RequestParam(value = REQUEST_PARAM_LIMIT, required = false,
                                                  defaultValue = "10") int limit) {
        GetChildrenResult result =
                contentService.getChildrenById(siteId, id, localeCode, sortStrategy, order, offset, limit);
        ResponseBody responseBody = new ResponseBody();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }
}
