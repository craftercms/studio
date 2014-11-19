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

import java.util.Date;

import javolution.util.FastComparator;

/**
 * content comparater base class
 * 
 * @author hyanghee
 *
 */
public abstract class ContentComparatorBase<T> extends FastComparator<T> {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 2704130586801127603L;
	protected String _sort;
	protected boolean _ascending;

	/**
	 * constructor
	 * 
	 * @param sort
	 * @param ascending
	 */
	public ContentComparatorBase(final String sort, final boolean ascending) {
		this._ascending = ascending;
		this._sort = sort;
	}

	/**
	 * compare dates
	 * 
	 * @param value1
	 * @param value2
	 * @param ascending
	 * @return sorting result
	 */
	protected int compareDates(Date value1, Date value2, boolean ascending) {
		if (value1 == null && value2 == null) {
			return 0;
		} else if (value1 == null) {
			return (_ascending) ? -1 : 1;
		} else if (value2 == null) {
			return (_ascending) ? 1 : -1;
		} else if (_ascending) {
			return value1.compareTo(value2);
		} else {
			return value1.compareTo(value2) * -1;
		}
	}

	/**
	 * compare dates
	 * 
	 * @param value1
	 * @param value2
	 * @param ascending
	 * @return sorting result
	 */
	protected int compareStrings(String value1, String value2, boolean ascending) {
		if (value1 == null && value2 == null) {
			return 0;
		} else if (value1 == null) {
			return (ascending) ? -1 : 1;
		} else if (value2 == null) {
			return (ascending) ? 1 : -1;
		} else if (ascending) {
			return value1.compareTo(value2);
		} else {
			return value1.compareTo(value2) * -1;
		}
	}

}
