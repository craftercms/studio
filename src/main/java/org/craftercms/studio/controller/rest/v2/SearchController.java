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

import java.io.IOException;

import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v2.service.search.SearchService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_RESULT;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller to access the search service
 * @author joseross
 */
@RestController
@RequestMapping("/api/2/search")
public class SearchController {

    /**
     * The search service
     */
    protected SearchService searchService;

    @Required
    public void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    @RequestMapping(value = "/search", method = { GET, POST })
    public ResponseBody search(@RequestParam String siteId, @RequestBody SearchParams params)
        throws AuthenticationException, IOException {

        SearchResult searchResult = searchService.search(siteId, params);

        ResultOne<SearchResult> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_RESULT, searchResult);

        ResponseBody body = new ResponseBody();
        body.setResult(result);
        return body;
    }

}
