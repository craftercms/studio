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
package org.craftercms.cstudio.alfresco.util;

import org.craftercms.cstudio.alfresco.to.ContentItemTO;

/**
 * compares content items based on the key set in the current instance
 * 
 * @author hyanghee
 *
 */
public class SearchResultItemComparator extends ContentComparatorBase<ContentItemTO> {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 4244864334551790818L;

	/**
	 * constructor
	 * 
	 * @param sort
	 * @param ascending
	 */
	public SearchResultItemComparator(String sort, boolean ascending) {
		super(sort, ascending);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public int compare(ContentItemTO item1, ContentItemTO item2) {
		Comparable value1 = (Comparable) item1.getProperties().get(_sort);
		Comparable value2 = (Comparable) item2.getProperties().get(_sort);
		
		if (value1 == null && value2 == null) {
			return 0;
		} else if (value1 == null) {
			return (_ascending) ? 1 : -1;
		} else if (value2 == null) {
			return (_ascending) ? -1 : 1;
		} else if (_ascending) {
			if ( (value1 instanceof String) && (value2 instanceof String) ) {
				String str1 = ((String) value1).toLowerCase();
				String str2 = ((String) value2).toLowerCase();
				return str1.compareTo(str2);
			} 	
			return value1.compareTo(value2);
		} else {
			if ( (value1 instanceof String) && (value2 instanceof String) ) {
				String str1 = ((String) value1).toLowerCase();
				String str2 = ((String) value2).toLowerCase();
				return str1.compareTo(str2) * -1;
			} 	
			return value1.compareTo(value2) * -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javolution.util.FastComparator#areEqual(java.lang.Object, java.lang.Object)
	 */
	public boolean areEqual(ContentItemTO item1, ContentItemTO item2) {
		if (item1 == null && item2 == null) {
			return true;
		} else if (item1 == null || item2 == null) {
			return false;
		} else {
			return item1.equals(item2);
		}
	}

	public int hashCodeOf(ContentItemTO item) {
		if (item != null) {
			return item.hashCode();
		}
		return 0;
	}

}
