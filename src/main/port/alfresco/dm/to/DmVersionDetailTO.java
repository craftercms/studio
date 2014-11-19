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
package org.craftercms.cstudio.alfresco.dm.to;

import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class for storing documents in process or in review item 
 *
 * @author subhasha
 *
 */
public class DmVersionDetailTO implements Serializable {


    private static final long serialVersionUID = -4199807016773084457L;

    protected Date lastModifiedDate;
    protected String lastModifier;
    protected String versionNumber;
    protected String _timezone = ContentFormatUtils.DATE_PATTERN_TIMEZONE_GMT;
    protected DmContentItemTO _contentItem;
    protected String _comment;


    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    /**
     * @return the lastEditedDate
     */
    public String getLastModifiedDate() {
        if (lastModifiedDate != null) {
            SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW);
            String dateStr = ContentFormatUtils.formatDate(format, lastModifiedDate, _timezone);
            return dateStr;
        } else {
            return null;
        }
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public DmContentItemTO getContentItem() {
        return _contentItem;
    }

    public void setContentItem(DmContentItemTO contentItem) {
        this._contentItem = contentItem;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        this._comment = comment;
    }

    public void setTimeZone(String timeZone) {
        this._timezone = timeZone;
    }

    public String toString() {
        return "version number: " + versionNumber
                + ", author: " + lastModifier
                + ", lastModifiedDate: " + lastModifiedDate;
    }

}
