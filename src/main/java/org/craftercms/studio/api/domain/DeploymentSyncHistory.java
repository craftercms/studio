/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.studio.api.domain;

import java.util.Date;

public class DeploymentSyncHistory {

    public String getId() { return _id; }
    public void setId(String id) { this._id = id; }

    public Date getSyncDate() { return _syncDate; }
    public void setSyncDate(Date syncDate) { this._syncDate = syncDate; }

    public String getSite() { return _site; }
    public void setSite(String site) { this._site = site; }

    public String getEnvironment() { return _environment; }
    public void setEnvironment(String environment) { this._environment = environment; }

    public String getPath() { return _path; }
    public void setPath(String path) { this._path = path; }

    public String getTarget() { return _target; }
    public void setTarget(String target) { this._target = target; }

    public String getUser() { return _user; }
    public void setUser(String user) { this._user = user; }

    public String getContentTypeClass() { return _contentTypeClass; }
    public void setContentTypeClass(String contentTypeClass) { this._contentTypeClass = contentTypeClass; }

    protected String _id;
    protected Date _syncDate;
    protected String _site;
    protected String _environment;
    protected String _path;
    protected String _target;
    protected String _user;
    protected String _contentTypeClass;
}
