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
package org.craftercms.cstudio.alfresco.to;

import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;

public class ObjectStateTO {

    private String objectId;
    public String getObjectId() {
        return objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    private String site;
    public String getSite() {
        return site;
    }
    public void setSite(String site) {
        this.site = site;
    }

    private String path;
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    private ObjectStateService.State state;
    public ObjectStateService.State getState() {
        return state;
    }
    public void setState(ObjectStateService.State state) {
        this.state = state;
    }

    private boolean systemProcessing;
    public boolean isSystemProcessing() {
        return systemProcessing;
    }
    public void setSystemProcessing(boolean systemProcessing) {
        this.systemProcessing = systemProcessing;
    }
}
