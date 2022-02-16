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

package org.craftercms.studio.api.v2.dal;

import java.io.Serializable;

public class CmisContentItem implements Serializable {

    private static final long serialVersionUID = -1551759995506398586L;

    private String itemId;
    private String itemName;
    private String itemPath;
    private String mimeType;
    private long size;

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId;
    }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemPath() { return itemPath; }
    public void setItemPath(String itemPath) { this.itemPath = itemPath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
