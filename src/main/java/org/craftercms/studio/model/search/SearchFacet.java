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

import java.util.Map;

/**
 * Holds the data for a single facet
 * @author joseross
 */
public class SearchFacet {

    /**
     * The label of the facet
     */
    protected String name;

    /**
     * Indicates if the facet values are ranges
     */
    protected boolean range;

    /**
     * Indicates if the facet values are dates
     */
    protected boolean date;

    /**
     * Indicates if the facet supports multiple values
     */
    protected boolean multiple;

    /**
     * The values and counts of the facet
     */
    protected Map<Object, Object> values;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isRange() {
        return range;
    }

    public void setRange(final boolean range) {
        this.range = range;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(final boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isDate() {
        return date;
    }

    public void setDate(final boolean date) {
        this.date = date;
    }

    public Map<Object, Object> getValues() {
        return values;
    }

    public void setValues(final Map<Object, Object> values) {
        this.values = values;
    }

}
