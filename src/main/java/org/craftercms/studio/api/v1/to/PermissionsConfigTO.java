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
package org.craftercms.studio.api.v1.to;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Transfer object for containing the Document object for the file in question. 
 * 
 * @author Sandra O'Keeffe
 * @author Sweta Chalasani
 */
public class PermissionsConfigTO implements TimeStamped, Serializable {

    private static final long serialVersionUID = -8150776631439025097L;
    /** site-filename key **/
	protected String key = null;
	/** mappings Document object containing either permissions or role mapping details **/
	protected Document mapping = null;
	/** complete messages used for displaying complete pop-ups **/

	/** configuration time stamp **/
	protected ZonedDateTime lastUpdated = null;
	
	protected Map<String, List<String>> roles = null;
	protected Map<String, Map<String, List<Node>>> permissions = null;
	
	@Override
	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Document getMapping() {
		return mapping;
	}
	
	public Map<String, List<String>> getRoles() {
		return roles;
	}

	public void setMapping(Document mapping) {
		this.mapping = mapping;
	}
	
	public void setRoles(Map<String, List<String>> roles) {
		this.roles = roles;
	}

	public Map<String, Map<String, List<Node>>> getPermissions() {
		return permissions;
	}

	public void setPermissions(Map<String, Map<String, List<Node>>> permissions) {
		this.permissions = permissions;
	}
}
