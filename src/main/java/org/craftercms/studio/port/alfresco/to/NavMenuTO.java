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

import java.util.ArrayList;
import java.util.Map;

/**
 * contains contextual navigation menu items
 *  
 * @author hyanghee
 *
 */
public class NavMenuTO {

	/** contextual navigation menu items **/	
	protected Map<String, ArrayList<NavigationMenuListTO>> _menuItems;

	/**
	 * @param menuItems the menuItems to set
	 */
	public void setMenuItems(Map<String, ArrayList<NavigationMenuListTO>> menuItems) {
		this._menuItems = menuItems;
	}

	/**
	 * @return the menuItems
	 */
	public Map<String, ArrayList<NavigationMenuListTO>> getMenuItems() {
		return _menuItems;
	}
	 
}
