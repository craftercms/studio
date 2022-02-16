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

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A {@link Result} where the actual result is a single entity.
 *
 * @param <T> the entity type
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class ResultOne<T> extends Result {

    @JsonIgnore
    protected Map<String, T> entity;

    /**
     * Returns the result entity.
     */
    @JsonAnyGetter
    public Map<String, T> getEntity() {
        return entity;
    }

    /**
     * Sets the result entity.
     */
    @JsonAnySetter
    public void setEntity(String name, T entity) {
        this.entity = Collections.singletonMap(name, entity);
    }
}
