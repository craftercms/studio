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
package org.craftercms.studio.api.v1.dal;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class DeploymentSyncHistory implements Serializable {

    private static final long serialVersionUID = 1546577631929363169L;

    protected String id;
    protected ZonedDateTime syncDate;
    protected String site;
    protected String environment;
    protected String path;
    protected String target;
    protected String username;
    protected String contentTypeClass;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ZonedDateTime getSyncDate() { return syncDate; }
    public void setSyncDate(ZonedDateTime syncDate) { this.syncDate = syncDate; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getUser() { return username; }
    public void setUser(String username) { this.username = username; }

    public String getContentTypeClass() { return contentTypeClass; }
    public void setContentTypeClass(String contentTypeClass) { this.contentTypeClass = contentTypeClass; }
}
