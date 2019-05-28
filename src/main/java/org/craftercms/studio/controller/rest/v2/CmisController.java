/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.CmisPathNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisTimeoutException;
import org.craftercms.studio.api.v1.exception.CmisUnavailableException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.StudioPathNotFoundException;
import org.craftercms.studio.api.v2.dal.CmisContentItem;
import org.craftercms.studio.api.v2.service.cmis.CmisService;
import org.craftercms.studio.impl.v2.utils.PaginationUtils;
import org.craftercms.studio.model.rest.CmisCloneRequest;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_CMIS_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_CMIS_REPO_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
public class CmisController {

    protected CmisService cmisService;

    @GetMapping("/api/2/cmis/list")
    public ResponseBody list(@RequestParam(value = "siteId", required = true) String siteId,
                             @RequestParam(value = "cmisRepoId", required = true) String cmisRepoId,
                             @RequestParam(value = "path", required = false, defaultValue = StringUtils.EMPTY) String path,
                             @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                             @RequestParam(value = "limit", required = false, defaultValue = "10") int limit)
            throws CmisRepositoryNotFoundException, CmisTimeoutException, CmisUnavailableException {

        List<CmisContentItem> cmisContentItems = cmisService.list(siteId, cmisRepoId, path);
        List<CmisContentItem> paginatedItems =
                PaginationUtils.paginate(cmisContentItems, offset, limit, StringUtils.EMPTY);

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<CmisContentItem> result = new PaginatedResultList<>();
        result.setTotal(cmisContentItems.size());
        result.setOffset(offset);
        result.setLimit(paginatedItems.size());
        result.setResponse(OK);
        responseBody.setResult(result);
        result.setEntities(RESULT_KEY_ITEMS, paginatedItems);
        return responseBody;
    }

    @GetMapping("/api/2/cmis/search")
    public ResponseBody search(@RequestParam(value = "siteId", required = true) String siteId,
                               @RequestParam(value = "cmisRepoId", required = true) String cmisRepoId,
                               @RequestParam(value = "searchTerm", required = true) String searchTerm,
                               @RequestParam(value = "path", required = false, defaultValue = StringUtils.EMPTY) String path,
                               @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                               @RequestParam(value = "limit", required = false, defaultValue = "10") int limit)
            throws CmisRepositoryNotFoundException, CmisTimeoutException, CmisUnavailableException {

        List<CmisContentItem> cmisContentItems = cmisService.search(siteId, cmisRepoId, searchTerm, path);
        List<CmisContentItem> paginatedItems =
                PaginationUtils.paginate(cmisContentItems, offset, limit, StringUtils.EMPTY);

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<CmisContentItem> result = new PaginatedResultList<>();
        result.setTotal(cmisContentItems.size());
        result.setOffset(offset);
        result.setLimit(paginatedItems.size());
        result.setResponse(OK);
        responseBody.setResult(result);
        result.setEntities(RESULT_KEY_ITEMS, paginatedItems);
        return responseBody;
    }

    @PostMapping("/api/2/cmis/clone")
    public ResponseBody cloneContent(@RequestBody CmisCloneRequest cmisCloneRequest)
            throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException,
            StudioPathNotFoundException, ServiceLayerException, CmisPathNotFoundException {
        cmisService.cloneContent(cmisCloneRequest.getSiteId(), cmisCloneRequest.getCmisRepoId(),
                cmisCloneRequest.getCmisPath(), cmisCloneRequest.getStudioPath());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = "/api/2/cmis/upload")
    public ResponseBody uploadContent(HttpServletRequest httpServletRequest)
            throws IOException, CmisUnavailableException, CmisPathNotFoundException, CmisTimeoutException,
            CmisRepositoryNotFoundException, FileUploadException {

        ServletFileUpload servletFileUpload = new ServletFileUpload();
        FileItemIterator itemIterator = servletFileUpload.getItemIterator(httpServletRequest);
        String filename = StringUtils.EMPTY;
        String siteId = StringUtils.EMPTY;
        String cmisRepoId = StringUtils.EMPTY;
        String cmisPath = StringUtils.EMPTY;
        while (itemIterator.hasNext()) {
            FileItemStream item = itemIterator.next();
            String name = item.getFieldName();
            try (InputStream stream = item.openStream()) {
                if (item.isFormField()) {
                    switch (name) {
                        case REQUEST_PARAM_SITEID:
                            siteId = Streams.asString(stream);
                            break;
                        case REQUEST_PARAM_CMIS_REPO_ID:
                            cmisRepoId = Streams.asString(stream);
                            break;
                        case REQUEST_PARAM_CMIS_PATH:
                            cmisPath = Streams.asString(stream);
                            break;
                        default:
                            // Unknown parameter, just skip it...
                            break;
                    }
                } else {
                    filename = item.getName();
                    cmisService.uploadContent(siteId, cmisRepoId, cmisPath, filename, stream);
                }
            }
        }
        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    public CmisService getCmisService() {
        return cmisService;
    }

    public void setCmisService(CmisService cmisService) {
        this.cmisService = cmisService;
    }
}
