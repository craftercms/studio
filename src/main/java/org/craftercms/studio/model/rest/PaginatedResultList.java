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

/**
 * A paginated {@link ResultList}.
 *
 * @param <T> the entity type
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class PaginatedResultList<T> extends ResultList<T> {

    protected long total;
    protected int offset;
    protected int limit;

    /**
     * Returns the total of results.
     */
    public long getTotal() {
        return total;
    }

    /**
     * Sets the total of results.
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * Returns the offset in the total of results this result list starts.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the offset in the total of results this result list starts.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the number of items in the result list.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the number of items in the result list.
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

}
