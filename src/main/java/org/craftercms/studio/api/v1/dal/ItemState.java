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

/**
 * @author Dejan Brkic
 */
public class ItemState implements Serializable {
    private static final long serialVersionUID = -4858304001684851333L;

    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getSystemProcessing() { return systemProcessing; }
    public void setSystemProcessing(int systemProcessing) { this.systemProcessing = systemProcessing; }

    protected String objectId;
    protected String site;
    protected String path;
    protected String state;
    protected int systemProcessing;
}
