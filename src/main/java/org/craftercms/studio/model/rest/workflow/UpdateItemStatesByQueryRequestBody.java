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

package org.craftercms.studio.model.rest.workflow;

import javax.validation.constraints.NotEmpty;

public class UpdateItemStatesByQueryRequestBody {

    @NotEmpty
    private Query query;
    @NotEmpty
    private ItemStatesPostRequestBody update;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public ItemStatesPostRequestBody getUpdate() {
        return update;
    }

    public void setUpdate(ItemStatesPostRequestBody update) {
        this.update = update;
    }

    public class Query {
        @NotEmpty
        private String siteId;
        private String path;
        private Long states;

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Long getStates() {
            return states;
        }

        public void setStates(Long states) {
            this.states = states;
        }
    }
}
