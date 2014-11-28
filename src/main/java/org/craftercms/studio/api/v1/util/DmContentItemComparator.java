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
package org.craftercms.studio.api.v1.util;

import org.craftercms.studio.api.v1.to.DmContentItemTO;
import org.craftercms.studio.impl.v1.util.ContentComparatorBase;

public class DmContentItemComparator extends ContentComparatorBase<DmContentItemTO> {
	
	public static final String SORT_EVENT_DATE = "eventDate";
	public static final String SORT_INTERNAL_NAME = "internalName";
	public static final String SORT_BROWSER_URI = "browserUri";
	public static final String SORT_PATH = "path";
	public static final String SORT_USER_LAST_NAME = "userLastName";
	
	/** if this is set to true, floating items will come last in the child list **/
	protected boolean _listFloatingPagesLast;
	/** if this is set to true, level descriptors will come first in the child list **/
	protected boolean _listLevelDescriptorsFirst;

    protected String _secondLevelSortBy;

    protected boolean _isSecondLevelCompareRequired;

    protected boolean _secondLevelAscending;
    
    /**
	 * constructor that sets the sort key and the sort type
	 * 
	 * @param sort
	 * @param ascending
	 * @param listFloatingPagesLast
	 * 			if this is set to true, floating items will come last in the child list
	 * @param listLevelDescriptorsFirst
	 * 			if this is set to true, level descriptors will come first in the child list
	 */
	public DmContentItemComparator(final String sort, final boolean ascending, final boolean listFloatingPagesLast, final boolean listLevelDescriptorsFirst) {
		super(sort, ascending);
		_listFloatingPagesLast = listFloatingPagesLast;
		_listLevelDescriptorsFirst = listLevelDescriptorsFirst;
	}
	
	public DmContentItemComparator(final String sort, final boolean ascending, final boolean listFloatingPagesLast, final boolean listLevelDescriptorsFirst,final String secondLevelSortBy,final boolean secLevelCompareReq,final boolean secLevAscending) {
        this(sort,ascending,listFloatingPagesLast,listLevelDescriptorsFirst);
        this._secondLevelSortBy = secondLevelSortBy;
        this._isSecondLevelCompareRequired = secLevelCompareReq;
        this._secondLevelAscending = secLevAscending;
    }
	
	public boolean areEqual(DmContentItemTO item1, DmContentItemTO item2) {
		if (item1 == null && item2 == null) {
			return true;
		} else if (item1 == null || item2 == null) {
			return false;
		} else {
			return item1.equals(item2);
		}
	}
	
	public int hashCodeOf(DmContentItemTO item) {
		if (item != null) {
			return item.hashCode();
		} else {
			return 0;
		}
	}
	
	public int compare(final DmContentItemTO item1, final DmContentItemTO item2) {
		// check for level descriptors
		if (_listLevelDescriptorsFirst) {
			if (item1.isLevelDescriptor() && !item2.isLevelDescriptor()) {
				return (_ascending) ? -1 : 1;
			} else if (!item1.isLevelDescriptor() && item2.isLevelDescriptor()) {
				return (_ascending) ? 1 : -1;
			}
		}
		// check for floating pages
		if (_listFloatingPagesLast) {
			if (item1.isFloating() && !item2.isFloating()) {
				return (_ascending) ? 1 : -1;
			} else if (!item1.isFloating() && item2.isFloating()) {
				return (_ascending) ? -1 : 1;
			}
		}
		// if no checking for floating page & level descriptors
		// or both of them are the same type
		// compare them by their metadata
		if (SORT_EVENT_DATE.equals(_sort)) {
			int rt = compareDates(item1.getEventDateAsDate(), item2.getEventDateAsDate(), _ascending);
            if(rt == 0 && _isSecondLevelCompareRequired) {
                return secondLevelCompare(item1,item2);
            }
            return rt;

		} else if (SORT_INTERNAL_NAME.equals(_sort)) {
			String item1InternalName = (item1.getInternalName() != null) ? item1.getInternalName().toLowerCase() : null;
			String item2InternalName = (item2.getInternalName() != null) ? item2.getInternalName().toLowerCase() : null;
			int rt = compareStrings(item1InternalName, item2InternalName, _ascending);
            if(rt == 0 && _isSecondLevelCompareRequired) {
                return secondLevelCompare(item1,item2);
            }
            return rt;
		} else if (SORT_BROWSER_URI.equals(_sort)) {
			String item1Uri = (item1.getUri() != null) ? item1.getUri().toLowerCase() : null;
			String item2Uri = (item2.getUri() != null) ? item2.getUri().toLowerCase() : null;
			int rt = compareStrings(item1Uri, item2Uri, _ascending);
            if(rt == 0 && _isSecondLevelCompareRequired) {
                return secondLevelCompare(item1,item2);
            }
            return rt;
		} else if (SORT_PATH.equals(_sort)) {
			String item1Path = (item1.getPath() != null) ? item1.getPath().toLowerCase() : null;
			String item2Path = (item2.getPath() != null) ? item2.getPath().toLowerCase() : null;
			int rt = compareStrings(item1Path, item2Path, _ascending);
            if(rt == 0 && _isSecondLevelCompareRequired) {
                return secondLevelCompare(item1,item2);
            }
            return rt;
		} else if (SORT_USER_LAST_NAME.equals(_sort)) {
			String item1LastName = (item1.getUserLastName() != null) ? item1.getUserLastName().toLowerCase() : null;
			String item2LastName = (item2.getUserLastName() != null) ? item2.getUserLastName().toLowerCase() : null;
			int result = compareStrings(item1LastName, item2LastName, _ascending);
			if (result == 0) {
				String item1FirstName = (item1.getUserFirstName() != null) ? item1.getUserFirstName().toLowerCase() : null;
				String item2FirstName = (item2.getUserFirstName() != null) ? item2.getUserFirstName().toLowerCase() : null;
				return compareStrings(item1FirstName, item2FirstName, _ascending);
			} else {
				return result;
			}
		}
		return 0;
	}
	
	public int secondLevelCompare(final DmContentItemTO item1, final DmContentItemTO item2) {
        // if no checking for floating page & level descriptors
        // or both of them are the same type
        // compare them by their metadata
        if (SORT_EVENT_DATE.equals(_secondLevelSortBy)) {
            return compareDates(item1.getEventDateAsDate(), item2.getEventDateAsDate(), _secondLevelAscending);
        } else if (SORT_INTERNAL_NAME.equals(_secondLevelSortBy)) {
            String item1InternalName = (item1.getInternalName() != null) ? item1.getInternalName().toLowerCase() : null;
            String item2InternalName = (item2.getInternalName() != null) ? item2.getInternalName().toLowerCase() : null;
            return compareStrings(item1InternalName, item2InternalName, _secondLevelAscending);
        } else if (SORT_BROWSER_URI.equals(_secondLevelSortBy)) {
            String item1Uri = (item1.getUri() != null) ? item1.getUri().toLowerCase() : null;
            String item2Uri = (item2.getUri() != null) ? item2.getUri().toLowerCase() : null;
            return compareStrings(item1Uri, item2Uri, _secondLevelAscending);
        } else if (SORT_PATH.equals(_secondLevelSortBy)) {
            String item1Path = (item1.getPath() != null) ? item1.getPath().toLowerCase() : null;
            String item2Path = (item2.getPath() != null) ? item2.getPath().toLowerCase() : null;
            return compareStrings(item1Path, item2Path, _secondLevelAscending);
        } else if (SORT_USER_LAST_NAME.equals(_secondLevelSortBy)) {
            String item1LastName = (item1.getUserLastName() != null) ? item1.getUserLastName().toLowerCase() : null;
            String item2LastName = (item2.getUserLastName() != null) ? item2.getUserLastName().toLowerCase() : null;
            int result = compareStrings(item1LastName, item2LastName, _secondLevelAscending);
            if (result == 0) {
                String item1FirstName = (item1.getUserFirstName() != null) ? item1.getUserFirstName().toLowerCase() : null;
                String item2FirstName = (item2.getUserFirstName() != null) ? item2.getUserFirstName().toLowerCase() : null;
                return compareStrings(item1FirstName, item2FirstName, _secondLevelAscending);
            } else {
                return result;
            }
        }
        return 0;
    }
	
	public void setListFloatingPagesLast(boolean listFloatingPagesLast) {
		this._listFloatingPagesLast = listFloatingPagesLast;
	}

	public boolean isListFloatingPagesLast() {
		return _listFloatingPagesLast;
	}

	public void setListLevelDescriptorsFirst(boolean listLevelDescriptorsFirst) {
		this._listLevelDescriptorsFirst = listLevelDescriptorsFirst;
	}

	public boolean isListLevelDescriptorsFirst() {
		return _listLevelDescriptorsFirst;
	}

    public String getSecondLevelSortBy() {
        return _secondLevelSortBy;
    }

    public void setSecondLevelSortBy(String secondLevelSort) {
        this._secondLevelSortBy = secondLevelSort;
    }

    public boolean isSecondLevelCompareRequired() {
        return _isSecondLevelCompareRequired;
    }

    public void setSecondLevelCompareRequired(boolean secondLevelCompareReq) {
        _isSecondLevelCompareRequired = secondLevelCompareReq;
    }

    public boolean isSecondLevelAscending() {
        return _secondLevelAscending;
    }

    public void setSecondLevelAscending(boolean secondLevelAscending) {
        this._secondLevelAscending = secondLevelAscending;
    }
}
