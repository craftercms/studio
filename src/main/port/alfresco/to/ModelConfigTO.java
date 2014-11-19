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

/**
 * Class for storing model configuration 
 * 
 * @author hyanghee
 *
 */
public class ModelConfigTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -6941494257051632798L;
	/** model location **/
	protected String _path;
	/** model hierarchy depth **/
	protected int _depth;
	/** model display name **/
	protected String _displayName;
	protected String _namespace;

	public ModelConfigTO() {}
	
	public ModelConfigTO(String path, int depth, String displayName, String namespace) {
		this._path = path;
		this._depth = depth;
		this._displayName = displayName;
		this._namespace = namespace;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return _path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this._path = path;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return _depth;
	}

	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this._depth = depth;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return _displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this._displayName = displayName;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return _namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this._namespace = namespace;
	}
	


}
