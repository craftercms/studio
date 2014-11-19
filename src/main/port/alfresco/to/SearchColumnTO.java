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
 * Search Column bean
 * 
 * @author tanveer
 *
 */
public class SearchColumnTO {
	
	protected String _name = null;
	protected String _title = null;
	protected boolean _useWildCard = false;
	protected boolean _searchable = false;
	
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
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this._title = title;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * @param searchable the searchable to set
	 */
	public void setSearchable(boolean searchable) {
		this._searchable = searchable;
	}

	/**
	 * @return the searchable
	 */
	public boolean isSearchable() {
		return _searchable;
	}

	/**
	 * @param useWildCard the useWildCard to set
	 */
	public void setUseWildCard(boolean useWildCard) {
		this._useWildCard = useWildCard;
	}

	/**
	 * @return the useWildCard
	 */
	public boolean isUseWildCard() {
		return _useWildCard;
	}
}
