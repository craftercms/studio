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
package org.craftercms.studio.api.v1.service.deployment;

import java.io.Serializable;

//implementation detail
public class PublishingSyncItem implements Serializable {

    private static final long serialVersionUID = 6640606779759849591L;

    public enum Action {
        NEW,
        UPDATE,
        MOVE,
        DELETE
    }

    public String getId() { return _id; }
    public void setId(String id) { this._id = id; }

    public String getSite() { return _site; }
    public void setSite(String site) { this._site = site; }

    public String getEnvironment() { return _environment; }
    public void setEnvironment(String environment) { this._environment = environment; }

    public String getPath() { return _path; }
    public void setPath(String path) { this._path = path; }

    public String getUser() { return _user; }
    public void setUser(String user) { this._user = user; }

    public long getTimestampVersion() { return _timestampVersion; }
    public void setTimestampVersion(long timestampVersion) { this._timestampVersion = timestampVersion; }

    public Action getAction() { return _action; }
    public void setAction(Action action) { this._action = action; }

    public String getOldPath() { return _oldPath; }
    public void setOldPath(String oldPath) { this._oldPath = oldPath; }

    public String getContentTypeClass() { return _contentTypeClass; }
    public void setContentTypeClass(String contentTypeClass) { this._contentTypeClass = contentTypeClass; }

    protected String _id;
    protected String _site;
    protected String _environment;
    protected String _path;
    protected String _user;
    protected long _timestampVersion;
    protected Action _action;
    protected String _oldPath;
    protected String _contentTypeClass;
}
