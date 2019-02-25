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
    protected Number from;

    /**
     * The ending limit of the range
     */
    protected Number to;

    public Number getCount() {
        return count;
    }

    public void setCount(final Number count) {
        this.count = count;
    }

    public Number getFrom() {
        return from;
    }

    public void setFrom(final Number from) {
        this.from = from;
    }

    public Number getTo() {
        return to;
    }

    public void setTo(final Number to) {
        this.to = to;
    }

}
