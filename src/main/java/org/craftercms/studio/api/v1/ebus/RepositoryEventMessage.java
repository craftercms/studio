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

package org.craftercms.studio.api.v1.ebus;


import org.craftercms.commons.http.RequestContext;

import java.io.Serializable;

/**
 * Repository event message.
 *
 * @author Dejan Brkic
 */
public class RepositoryEventMessage implements Serializable {

    private String site;
    private String path;
    private String oldPath;
    private String contentType;
    private RepositoryEventContext repositoryEventContext;

    public String getSite() { return site; }
    public void setSite(final String site) { this.site = site; }

    public String getPath() { return path; }
    public void setPath(final String path) { this.path = path; }

    public String getOldPath() { return oldPath; }
    public void setOldPath(String oldPath) { this.oldPath = oldPath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public RepositoryEventContext getRepositoryEventContext() { return repositoryEventContext; }
    public void setRepositoryEventContext(RepositoryEventContext repositoryEventContext) { this.repositoryEventContext = repositoryEventContext; }
}
