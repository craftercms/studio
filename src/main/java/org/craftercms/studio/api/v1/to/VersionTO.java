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
package org.craftercms.studio.api.v1.to;

import java.util.Date;

/**
 * a version record
 */
public class VersionTO {

    protected Date lastModifiedDate;
    protected String lastModifier;
    protected String versionNumber;
    protected String _timezone; 
    protected ContentItemTO _contentItem;
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
        return "";
        // if (lastModifiedDate != null) {
        //     SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW);
        //     String dateStr = ContentFormatUtils.formatDate(format, lastModifiedDate, _timezone);
        //     return dateStr;
        // } else {
        //     return null;
        // }
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

    public ContentItemTO getContentItem() {
        return _contentItem;
    }

    public void setContentItem(ContentItemTO contentItem) {
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
