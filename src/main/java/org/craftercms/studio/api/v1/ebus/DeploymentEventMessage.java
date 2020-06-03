/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.ebus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class DeploymentEventMessage implements Serializable {

    private static final long serialVersionUID = 3564349406862734404L;

    private String site;
    private String endpoint;
    private List<DeploymentEventItem> items;
    private RepositoryEventContext repositoryEventContext;

    @JsonCreator
    public DeploymentEventMessage(@JsonProperty String site, @JsonProperty String endpoint, @JsonProperty List<DeploymentEventItem> items, @JsonProperty RepositoryEventContext repositoryEventContext) {
        super();
        this.site = site;
        this.endpoint = endpoint;
        this.items = items;
        this.repositoryEventContext = repositoryEventContext;
    }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public List<DeploymentEventItem> getItems() { return items; }
    public void setItems(List<DeploymentEventItem> items) { this.items = items; }

    public RepositoryEventContext getRepositoryEventContext() { return repositoryEventContext; }
    public void setRepositoryEventContext(RepositoryEventContext repositoryEventContext) { this.repositoryEventContext = repositoryEventContext; }
}
