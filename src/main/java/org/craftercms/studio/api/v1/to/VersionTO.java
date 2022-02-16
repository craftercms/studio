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
package org.craftercms.studio.api.v1.to;

import org.craftercms.studio.impl.v2.utils.DateUtils;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Scanner;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ;

/**
 * a version record
 */
public class VersionTO implements Comparable<VersionTO>, Serializable {

    private static final long serialVersionUID = 2451314126621963140L;
    protected ZonedDateTime lastModifiedDate;
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
            String dateStr = DateUtils.formatDate(lastModifiedDate, DATE_PATTERN_WORKFLOW_WITH_TZ);
            return dateStr;
        } else {
            return null;
        }
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
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
        int toRet = 0;
        if (version == null) {
            toRet = 1;
        } else {

            Scanner scanner1 = new Scanner(this.getVersionNumber());
            scanner1.useDelimiter("\\.");

            Scanner scanner2 = new Scanner(version.getVersionNumber());
            scanner2.useDelimiter("\\.");

            boolean found = false;
            while (scanner1.hasNextInt() && scanner2.hasNextInt()) {
                int v1 = scanner1.nextInt();
                int v2 = scanner2.nextInt();
                if (v1 < v2) {
                    toRet = -1;
                    found = true;
                    break;
                } else if (v1 > v2) {
                    toRet = 1;
                    found = true;
                    break;
                }
            }

            if (!found && scanner1.hasNextInt()) {
                toRet = 1; //str1 has an additional lower-level version number
            } else if (!found && scanner2.hasNextInt()) {
                toRet = -1;
            }
            scanner1.close();
            scanner2.close();
        }
        return toRet;
    }
}
