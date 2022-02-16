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