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

import java.util.Set;

import javolution.util.FastSet;

public class ContentTypePathTO {

	/** allowed path **/
	protected String _pathInclude;
	/** allowed content types without excluded paths **/
	protected Set<String> _allowedContentTypes;

	/**
	 * @return the pathInclude
	 */
	public String getPathInclude() {
		return _pathInclude;
	}

	/**
	 * @param pathInclude
	 *            the pathInclude to set
	 */
	public void setPathInclude(String pathInclude) {
		this._pathInclude = pathInclude;
	}

	/**
	 * @return the allowedContentTypes
	 */
	public Set<String> getAllowedContentTypes() {
		return _allowedContentTypes;
	}

	/**
	 * @param allowedContentTypes
	 *            the allowedContentTypes to set
	 */
	public void setAllowedContentTypes(Set<String> allowedContentTypes) {
		this._allowedContentTypes = allowedContentTypes;
	}

	/**
	 * add to allowed content types (no exclusion list)
	 * 
	 * @param key
	 */
	public void addToAllowedContentTypes(String key) {
		if (this._allowedContentTypes == null) {
			this._allowedContentTypes = new FastSet<String>();
		}
		this._allowedContentTypes.add(key);
	}

	/**
	 * remove the given content type key from allowed content types
	 * 
	 * @param key
	 */
	public void removeAllowedContentTypes(String key) {
		if (this._allowedContentTypes != null) {
			this._allowedContentTypes.remove(key);
		}
		
	}

}
