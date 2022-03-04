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

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Contains the response body of an API operation.
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class ResponseBody {

    @JsonUnwrapped
    protected Result result;

    /**
     * Returns the API result.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Sets the API result.
     */
    public void setResult(Result result) {
        this.result = result;
    }

}