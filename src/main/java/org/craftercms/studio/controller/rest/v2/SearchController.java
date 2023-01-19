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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v2.service.search.SearchService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.beans.ConstructorProperties;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_RESULT;

/**
 * Controller to access the search service
 *
 * @author joseross
 */
@Validated
@RestController
@RequestMapping("/api/2/search")
public class SearchController {

    /**
     * The search service
     */
    protected final SearchService searchService;

    @ConstructorProperties({"searchService"})
    public SearchController(final SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping(value = "/search")
    public ResultOne<SearchResult> search(@EsapiValidatedParam(type = SITE_ID) @RequestParam String siteId, @Valid @RequestBody SearchParams params)
            throws AuthenticationException, ServiceLayerException {
        SearchResult searchResult = searchService.search(siteId, params);

        ResultOne<SearchResult> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_RESULT, searchResult);
        return result;
    }

}
