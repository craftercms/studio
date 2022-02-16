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

package org.craftercms.studio.api.v2.dal;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class DeploymentHistoryItem implements Serializable {

    private static final long serialVersionUID = 8029357585496491162L;

    protected String id;
    protected ZonedDateTime deploymentDate;
    protected String site;
    protected String environment;
    protected String path;
    protected String target;
    protected String username;
    protected String contentTypeClass;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getDeploymentDate() {
        return deploymentDate;
    }

    public void setDeploymentDate(ZonedDateTime deploymentDate) {
        this.deploymentDate = deploymentDate;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getUser() {
        return username;
    }

    public void setUser(String username) {
        this.username = username;
    }

    public String getContentTypeClass() {
        return contentTypeClass;
    }

    public void setContentTypeClass(String contentTypeClass) {
        this.contentTypeClass = contentTypeClass;
    }
}
