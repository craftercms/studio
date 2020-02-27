/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.to;

import java.io.Serializable;

public class PublishingChannelConfigTO implements Serializable {

    private static final long serialVersionUID = -3800738241727318928L;
    /** publishing channel ID **/
    protected String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /** publishing channel name **/
    protected String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** publishing channel type **/
    protected String type;
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    /** publishing channel order **/
    protected int order = Integer.MAX_VALUE;
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    @Override
	public String toString() {
		return name;
	}

}
