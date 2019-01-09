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
package org.craftercms.studio.api.v1.to;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PublishingChannelGroupConfigTO implements Serializable {

    private static final long serialVersionUID = 2775150585198082982L;
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private boolean liveEnvironment = false;
    public boolean isLiveEnvironment() {
        return liveEnvironment;
    }
    public void setLiveEnvironment(boolean liveEnvironment) {
        this.liveEnvironment = liveEnvironment;
    }

    private List<PublishingChannelConfigTO> channels = new ArrayList<PublishingChannelConfigTO>();
    public List<PublishingChannelConfigTO> getChannels() {
        return channels;
    }
    public void setChannels(List<PublishingChannelConfigTO> channels) {
        this.channels = channels;
    }

    private int order = Integer.MAX_VALUE;
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    private Set<String> roles = new HashSet<String>();
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
