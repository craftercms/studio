/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.util;


import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.ContentItemTO;

/**
 * This class compares two DmContentItem based on their order values specified
 * by order name
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class ContentItemOrderComparator extends ContentComparatorBase<ContentItemTO> {

    private static final long serialVersionUID = 1771650786602918784L;

    private static final Logger logger = LoggerFactory.getLogger(ContentItemOrderComparator.class);

    /** if this is set to true, floating items will come last in the child list **/
    protected boolean _listFloatingPagesLast;
    /** if this is set to true, level descriptors will come first in the child list **/
    protected boolean _listLevelDescriptorsFirst;

    /**
     * @param listFloatingPagesLast the listFloatingPagesLast to set
     */
    public void setListFloatingPagesLast(boolean listFloatingPagesLast) {
        this._listFloatingPagesLast = listFloatingPagesLast;
    }

    /**
     * @return the listFloatingPagesLast
     */
    public boolean isListFloatingPagesLast() {
        return _listFloatingPagesLast;
    }

    /**
     * @param listLevelDescriptorsFirst the listLevelDescriptorsFirst to set
     */
    public void setListLevelDescriptorsFirst(boolean listLevelDescriptorsFirst) {
        this._listLevelDescriptorsFirst = listLevelDescriptorsFirst;
    }

    /**
     * @return the listLevelDescriptorsFirst
     */
    public boolean isListLevelDescriptorsFirst() {
        return _listLevelDescriptorsFirst;
    }

    /**
     * constructor that sets the order name and the sort type
     *
     * @param orderName
     * @param ascending
     * @param listFloatingPagesLast
     * 			if this is set to true, floating items will come last in the child list
     * @param listLevelDescriptorsFirst
     * 			if this is set to true, level descriptors will come first in the child list
     */
    public ContentItemOrderComparator(final String orderName, final boolean ascending, final boolean listFloatingPagesLast, final boolean listLevelDescriptorsFirst) {
        super(orderName, ascending);
        _listFloatingPagesLast = listFloatingPagesLast;
        _listLevelDescriptorsFirst = listLevelDescriptorsFirst;
    }

    @Override
    public int compare(ContentItemTO item1, ContentItemTO item2) {

        // check for level descriptors
        if (_listLevelDescriptorsFirst) {
            if (item1.isLevelDescriptor() && !item2.isLevelDescriptor()) {
                return (_ascending) ? -1 : 1;
            } else if (!item1.isLevelDescriptor() && item2.isLevelDescriptor()) {
                return (_ascending) ? 1 : -1;
            }
        }

        // check for folders
        if ((item1.isFolder() && !item1.isPage()) && (!item2.isFolder() || (item2.isFolder() && item2.isPage()))) {
            return (_ascending) ? 1 : -1;
        } else if ((!item1.isFolder() || (item1.isFolder() && item1.isPage())) && (item2.isFolder() && !item2.isPage())) {
            return (_ascending) ? -1 : 1;
        } else if ((item1.isFolder() && !item1.isPage()) && (item2.isFolder() && !item2.isPage())) {
            return this.compareStrings(item1.getName(), item2.getName(), _ascending);
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
        // compare them by their orders, Unless both are floating (if so, sort by internalName)

        // both floating -- sort by internalName
        if (item1.isFloating() && item2.isFloating()) {
            String item1InternalName = (item1.getInternalName() != null) ? item1.getInternalName().toLowerCase() : null;
            String item2InternalName = (item2.getInternalName() != null) ? item2.getInternalName().toLowerCase() : null;

            return this.compareStrings(item1InternalName, item2InternalName, _ascending);
        }

        // otherwise, by order-value.  Last option internalName
        Double value1 = item1.getOrder(_sort);
        Double value2 = item2.getOrder(_sort);
        if (value1 == null && value2 == null) {
            return 0;
        } else if (value1 == null) {
            return (_ascending) ? 1 : -1;
        } else if (value2 == null) {
            return (_ascending) ? -1 : 1;
        } else {
            // if both items don't have order values, compare by their internal name
            if (value1 < 0 && value2 < 0) {
                String item1InternalName = (item1.getInternalName() != null) ? item1.getInternalName().toLowerCase() : null;
                String item2InternalName = (item2.getInternalName() != null) ? item2.getInternalName().toLowerCase() : null;

                return this.compareStrings(item1InternalName, item2InternalName, _ascending);
            }
            value1 = (value1 < 0) ? Double.MAX_VALUE : value1;
            value2 = (value2 < 0) ? Double.MAX_VALUE : value2;
            if (_ascending) {
                return value1.compareTo(value2);
            } else {
                return value1.compareTo(value2) * -1;
            }
        }
    }
}
