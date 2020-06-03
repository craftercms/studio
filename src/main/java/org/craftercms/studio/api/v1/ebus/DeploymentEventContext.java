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

import java.io.Serializable;
import java.util.List;

public class DeploymentEventContext implements Serializable {

    private static final long serialVersionUID = 6332448878646609100L;

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public List<DeploymentItem> getItems() { return items; }
    public void setItems(List<DeploymentItem> items) { this.items = items; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    protected String site;
    protected List<DeploymentItem> items;
    protected String environment;
    protected String author;
    protected String comment;
}
