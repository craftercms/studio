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

import java.io.Serializable;

public class CmisContentItemTO implements Serializable {

    private static final long serialVersionUID = -1465165864907850143L;

    private String item_id;
    private String item_name;
    private String item_path;
    private String mime_type;
    private long size;

    public String getItem_id() { return item_id; }
    public void setItem_id(String item_id) { this.item_id = item_id;
    }

    public String getItem_name() { return item_name; }
    public void setItem_name(String item_name) { this.item_name = item_name; }

    public String getItem_path() { return item_path; }
    public void setItem_path(String item_path) { this.item_path = item_path; }

    public String getMime_type() { return mime_type; }
    public void setMime_type(String mime_type) { this.mime_type = mime_type; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
