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
 * Clipboard Config: node association
 * 
 * @author tanveer
 *
 */
public class ClipboardNodeAssocTO {
	
	protected String _name = null;
	protected boolean _multiValued = false;
	protected String _assetFolder = null;
	
	/**
	 * @param name the name to set
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
	 * @param multiValued the multiValued to set
	 */
	public void setMultiValued(boolean multiValued) {
		this._multiValued = multiValued;
	}
	/**
	 * @return the multiValued
	 */
	public boolean isMultiValued() {
		return _multiValued;
	}
	/**
	 * @param assetFolder the assetFolder to set
	 */
	public void setAssetFolder(String assetFolder) {
		this._assetFolder = assetFolder;
	}
	/**
	 * @return the assetFolder
	 */
	public String getAssetFolder() {
		return _assetFolder;
	}
}
