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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A {@link Result} where the actual result is a list of entities.
 *
 * @param <T> the entity type
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class ResultList<T> extends Result {

    @JsonIgnore
    private Map<String, List<T>> entities;


    /**
     * Returns the result entities.
     */
    @JsonAnyGetter
    public Map<String, List<T>> getEntities() {
        return entities;
    }

    /**
     * Sets the result entities.
     */
    @JsonAnySetter
    public void setEntities(String name, List<T> entities) {
        this.entities = Collections.singletonMap(name, entities);
    }

}
