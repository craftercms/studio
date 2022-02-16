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

import java.util.List;

/**
 * Holds all the data for a search operation
 * @author joseross
 */
public class SearchResult {

    /**
     * The total files that matched the search
     */
    protected long total;

    /**
     * The list of files
     */
    protected List<SearchResultItem> items;

    /**
     * The facets of the matched files
     */
    protected List<SearchFacet> facets;

    public long getTotal() {
        return total;
    }

    public void setTotal(final long total) {
        this.total = total;
    }

    public List<SearchResultItem> getItems() {
        return items;
    }

    public void setItems(final List<SearchResultItem> items) {
        this.items = items;
    }

    public List<SearchFacet> getFacets() {
        return facets;
    }

    public void setFacets(final List<SearchFacet> facets) {
        this.facets = facets;
    }

}
