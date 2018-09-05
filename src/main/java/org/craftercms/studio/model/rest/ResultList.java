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
 * A {@link Result} where the actual result is a list of entities.
 *
 * @param <T> the entity type
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class ResultList<T> extends Result {

    /**
     * List of entities.
     */
    private List<T> entities;

    /**
     * Total number of entities found.
     */
    private int total;

    /**
     * Offset used to retrieve the entities list.
     */
    private int offset;

    /**
     * Limit used to retrieve the entities list.
     */
    private int limit;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }
}
