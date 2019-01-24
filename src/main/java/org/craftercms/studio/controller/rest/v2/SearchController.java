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
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.search.SearchItem;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.craftercms.studio.model.search.Sort;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;

/**
 * @author joseross
 */
@RestController
@RequestMapping("/api/2/search")
public class SearchController {

    protected SearchService searchService;

    @Required
    public void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseBody search(@RequestParam String siteId, @RequestParam String query,
                               @RequestParam(required = false, defaultValue = "0") int offset,
                               @RequestParam(required = false, defaultValue = "20") int limit,
                               @RequestParam(required = false, defaultValue = "score") String sortBy)
//                               @RequestParam(required = false, defaultValue = "DESC") Sort.Order sortOrder)
        throws AuthenticationException, IOException {

        Sort sort = new Sort();
        sort.setField(sortBy);
//        sort.setOrder(sortOrder);

        SearchParams request = new SearchParams();
        request.setQuery(query);
        request.setOffset(offset);
        request.setLimit(limit);
        request.setSort(sort);
        SearchResult searchResult = searchService.search(siteId, request);

        ResultOne<SearchResult> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity("result", searchResult);

        ResponseBody body = new ResponseBody();
        body.setResult(result);
        return body;
    }

}
