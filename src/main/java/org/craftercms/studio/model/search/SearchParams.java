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

package org.craftercms.studio.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Holds the data needed to perform a search operation
 * @author joseross
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchParams {

    /**
     * Keywords to search in the files.
     */
    protected String keywords;

    /**
     * Lucene query to execute.
     */
    protected String query;

    /**
     * Regular expression to filter the paths
     */
    protected String path;

    /**
     * The offset to paginate the results
     */
    protected int offset = 0;

    /**
     * The limit to paginate the results
     */
    protected int limit = 20;

    /**
     * The field to sort the results
     */
    protected String sortBy = "_score";

    /**
     * The order to sort the results
     */
    protected String sortOrder = "DESC";

    /**
     * The filters to search the files
     */
    protected Map<String, Object> filters;

    /**
     * Indicates if OR should be used instead of AND
     */
    protected boolean orOperator;

    /**
     * List of additional fields to include for each item
     */
    protected List<String> additionalFields = emptyList();

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(final String keywords) {
        this.keywords = keywords;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(final Map<String, Object> filters) {
        this.filters = filters;
    }

    public boolean isOrOperator() {
        return orOperator;
    }

    public void setOrOperator(boolean orOperator) {
        this.orOperator = orOperator;
    }

    public List<String> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(List<String> additionalFields) {
        this.additionalFields = additionalFields;
    }

}
