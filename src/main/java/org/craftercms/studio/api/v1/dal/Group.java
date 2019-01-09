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

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

public class Group implements Serializable, GrantedAuthority {

    private static final long serialVersionUID = 4723035066512137838L;

    private long id;
    private String name;
    private String description;
    private long siteId;
    private int externallyManaged;
    private String site;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getSiteId() { return siteId; }
    public void setSiteId(long siteId) { this.siteId = siteId; }

    public int getExternallyManaged() { return externallyManaged; }
    public void setExternallyManaged(int externallyManaged) { this.externallyManaged = externallyManaged; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    @Override
    public String getAuthority() {
        return name;
    }
}
