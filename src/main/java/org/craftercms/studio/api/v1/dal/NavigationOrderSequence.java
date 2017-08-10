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

public class NavigationOrderSequence implements Serializable {

    private static final long serialVersionUID = 3646263089226872560L;

    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public double getMaxCount() { return maxCount; }
    public void setMaxCount(double maxCount) { this.maxCount = maxCount; }

    protected String folderId;
    protected String site;
    protected String path;
    protected double maxCount;
}
