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

public class PublishingChannelConfigTO {

    /** publishing channel ID **/
    protected String _id;
    public String getId() {
        return _id;
    }
    public void setId(String id) {
        this._id = id;
    }

    /** publishing channel name **/
    protected String _name;
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        this._name = name;
    }

    /** publishing channel type **/
    protected String _type;
    public String getType() {
        return _type;
    }
    public void setType(String type) {
        this._type = type;
    }
	@Override
	public String toString() {
		return _name;
	}

}
