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

public class PublishingChannelTO implements Serializable {
    private static final long serialVersionUID = -4492271613702957391L;

    protected String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    protected boolean publish;
    public boolean isPublish() {
        return publish;
    }
    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    protected boolean updateStatus;
    public boolean isUpdateStatus() {
        return updateStatus;
    }
    public void setUpdateStatus(boolean updateStatus) {
        this.updateStatus = updateStatus;
    }

    protected int order = Integer.MAX_VALUE;
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PublishingChannelTO)) {
            return false;
        }
        PublishingChannelTO item = (PublishingChannelTO) object;
        // it is the same item if the default webapp and the URI
        // are the same
        return name.equals(item.getName());
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (this.name != null) {
            result = 31 * result + this.name.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
