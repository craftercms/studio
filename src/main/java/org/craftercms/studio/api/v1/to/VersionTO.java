/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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

import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * a version record
 */
public class VersionTO implements Comparable<VersionTO>, Serializable {

    private static final long serialVersionUID = 2451314126621963140L;
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
        if (lastModifiedDate != null) {
            SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW);
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

    @Override
    public boolean equals(Object version) {
        if (this == version) {
            return true;
        }
        if (version == null) {
            return false;
        }
        if (this.getClass() != version.getClass()) {
            return false;
        }
        return this.compareTo((VersionTO) version) == 0;
    }

    @Override
    public int compareTo(VersionTO version) {
        if (version == null) {
            return 1;
        }

        Scanner scanner1 = new Scanner(this.getVersionNumber());
        scanner1.useDelimiter("\\.");

        Scanner scanner2 = new Scanner(version.getVersionNumber());
        scanner2.useDelimiter("\\.");

        while(scanner1.hasNextInt() && scanner2.hasNextInt()) {
            int v1 = scanner1.nextInt();
            int v2 = scanner2.nextInt();
            if (v1 < v2) {
                return -1;
            } else if (v1 > v2) {
                return 1;
            }
        }

        if (scanner1.hasNextInt()) {
            return 1; //str1 has an additional lower-level version number
        } else if (scanner2.hasNextInt()) {
            return -1;
        }
        return 0;
    }
}
