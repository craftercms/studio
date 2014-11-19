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
 * name value pair object.
 * (defined new class not to have any dependency on any other library)
 * 
 * @author hyanghee
 *
 */
public class PairTO {

	protected String _name;
	protected String _value;

	/**
	 * constructor
	 */
	public PairTO() {}
	
	/**
	 * constructor 
	 * 
	 * @param name
	 * @param value
	 */
	public PairTO(String name, String value) {
		this._name = name;
		this._value = value;
	}
	
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this._value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return _value;
	}
	
	public String toString() {
		return "[" + _name + ", " + _value + "]";
	}

}
