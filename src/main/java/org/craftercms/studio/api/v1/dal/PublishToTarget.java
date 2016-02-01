/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

public class PublishToTarget implements Serializable {

    private static final long serialVersionUID = -8105378162779709176L;

    public class Action {
        public final static String NEW = "NEW";
        public final static String UPDATE = "UPDATE";
        public final static String MOVE = "MOVE";
        public final static String DELETE = "DELETE";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getOldPath() { return oldPath; }
    public void setOldPath(String oldPath) { this.oldPath = oldPath; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getContentTypeClass() { return contentTypeClass; }
    public void setContentTypeClass(String contentTypeClass) { this.contentTypeClass = contentTypeClass; }

    protected long id;
    protected String site;
    protected String environment;
    protected String path;
    protected String oldPath;
    protected String username;
    protected long version;
    protected String action;
    protected String contentTypeClass;
}
