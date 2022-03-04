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

/**
 * Holds the data for a single range in a facet
 * @author joseross
 */
public class SearchFacetRange {

    /**
     * The count of files that match the range
     */
    protected Number count;

    /**
     * The starting limit of the range
     */
    protected Object from;

    /**
     * The ending limit of the range
     */
    protected Object to;

    public Number getCount() {
        return count;
    }

    public void setCount(final Number count) {
        this.count = count;
    }

    public Object getFrom() {
        return from;
    }

    public void setFrom(final Object from) {
        this.from = from;
    }

    public Object getTo() {
        return to;
    }

    public void setTo(final Object to) {
        this.to = to;
    }

}
