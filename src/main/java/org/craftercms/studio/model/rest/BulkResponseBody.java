/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.model.rest;

import java.util.List;

/**
 * Contains the response body of a bulk API operation.
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class BulkResponseBody {

    protected List<Result> results;

    /**
     * Returns the API results.
     */
    public List<Result> getResults() {
        return results;
    }

    /**
     * Sets the API results.
     */
    public void setResults(List<Result> results) {
        this.results = results;
    }

}