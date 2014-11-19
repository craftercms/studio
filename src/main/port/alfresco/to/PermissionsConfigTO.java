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

import java.util.Date;
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
public class PermissionsConfigTO implements TimeStamped {

	/** site-filename key **/
	protected String _key = null;
	/** mappings Document object containing either permissions or role mapping details **/
	protected Document _mapping = null;
	/** complete messages used for displaying complete pop-ups **/

	/** configuration time stamp **/
	protected Date _lastUpdated = null;
	
	protected Map<String, List<String>> _roles = null;
	protected Map<String, Map<String, List<Node>>> _permissions = null;
	
	@Override
	public void setLastUpdated(Date lastUpdated) {
		_lastUpdated = lastUpdated;
	}

	@Override
	public Date getLastUpdated() {
		return _lastUpdated;
	}

	public String getKey() {
		return _key;
	}

	public void setKey(String key) {
		this._key = key;
	}

	public Document getMapping() {
		return _mapping;
	}
	
	public Map<String, List<String>> getRoles() {
		return _roles;
	}

	public void setMapping(Document mapping) {
		this._mapping = mapping;
	}
	
	public void setRoles(Map<String, List<String>> roles) {
		this._roles = roles;
	}

	public Map<String, Map<String, List<Node>>> getPermissions() {
		return _permissions;
	}

	public void setPermissions(Map<String, Map<String, List<Node>>> permissions) {
		this._permissions = permissions;
	}
}
