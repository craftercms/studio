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

/**
 * role configuration
 * 
 */
public class RoleConfigTO {

	protected String _name;
	protected String _role;
	protected String _label;
	protected int _weight;

	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this._label = label;
	}

	/**
	 * @return the weight
	 */
	public int getWeight() {
		return _weight;
	}

	/**
	 * @param weight
	 *            the weight to set
	 */
	public void setWeight(int weight) {
		this._weight = weight;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this._role = role;
	}

	/**
	 * @return the role
	 */
	public String getRole() {
		return _role;
	}

}
