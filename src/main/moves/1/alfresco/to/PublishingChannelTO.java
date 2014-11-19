/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.to;

import java.io.Serializable;

public class PublishingChannelTO implements Serializable {
    private static final long serialVersionUID = -4492271613702957391L;

    protected String _name;
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        this._name = name;
    }

    protected boolean _publish;
    public boolean isPublish() {
        return _publish;
    }
    public void setPublish(boolean publish) {
        this._publish = publish;
    }

    protected boolean _updateStatus;
    public boolean isUpdateStatus() {
        return _updateStatus;
    }
    public void setUpdateStatus(boolean updateStatus) {
        this._updateStatus = updateStatus;
    }

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
        return _name.equals(item.getName());
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (this._name != null) {
            result = 31 * result + this._name.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
