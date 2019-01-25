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

package org.craftercms.studio.impl.v2.service.search;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.search.elasticsearch.ElasticSearchWrapper;
import org.craftercms.search.elasticsearch.impl.AbstractElasticSearchWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link ElasticSearchWrapper} specific for authoring indexes
 * @author joseross
 */
public class PermissionAwareSearchService extends AbstractElasticSearchWrapper {

    /**
     * The suffix to append to the site name
     */
    protected String indexSuffix;

    @Required
    public void setIndexSuffix(final String indexSuffix) {
        this.indexSuffix = indexSuffix;
    }

    public SearchResponse search(String siteId, List<String> allowedPaths, SearchRequest request) throws IOException {
        return search(siteId, allowedPaths, request, RequestOptions.DEFAULT);
    }

    public SearchResponse search(String siteId, List<String> allowedPaths, SearchRequest request,
                                 RequestOptions options)
        throws IOException {

        request.indices(siteId + indexSuffix);

        if(CollectionUtils.isNotEmpty(allowedPaths)) {
            // TODO: Add filters to request
        }

        return client.search(request, options);
    }

    @Override
    protected void updateIndex(final SearchRequest request) {
        // do nothing, this method will not be used
    }

    @Override
    public SearchResponse search(final SearchRequest request, final RequestOptions options) {
        // Prevent execution of request without permission filters
        throw new UnsupportedOperationException();
    }

}
