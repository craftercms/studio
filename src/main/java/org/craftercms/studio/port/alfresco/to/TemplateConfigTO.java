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
import java.util.List;

import javolution.util.FastList;

public class TemplateConfigTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2842379986301454785L;

	/**
	 * a list of xpaths to exclude from copying on changing content type
	 */
	private List<String> _excludedPathsOnConvert = null;
	
	/**
	 * a list of xpaths that contain multiple values to copy over
	 */
	private List<String> _multiValuedPathsOnConvert = null;

	/**
	 * @return the excludedPathsOnConvert
	 */
	public List<String> getExcludedPathsOnConvert() {
		if (_excludedPathsOnConvert == null) {
			_excludedPathsOnConvert = new FastList<String>(0); 
		}
		return _excludedPathsOnConvert;
	}

	/**
	 * @param excludedPathsOnConvert the excludedPathsOnConvert to set
	 */
	public void setExcludedPathsOnConvert(List<String> excludedPathsOnConvert) {
		this._excludedPathsOnConvert = excludedPathsOnConvert;
	}

	/**
	 * @return the multiValuedPathsOnConvert
	 */
	public List<String> getMultiValuedPathsOnConvert() {
		if (_multiValuedPathsOnConvert == null) {
			_multiValuedPathsOnConvert = new FastList<String>(0); 
		}
		return _multiValuedPathsOnConvert;
	}

	/**
	 * @param multiValuedPathsOnConvert the multiValuedPathsOnConvert to set
	 */
	public void setMultiValuedPathsOnConvert(List<String> multiValuedPathsOnConvert) {
		this._multiValuedPathsOnConvert = multiValuedPathsOnConvert;
	}
	
}
