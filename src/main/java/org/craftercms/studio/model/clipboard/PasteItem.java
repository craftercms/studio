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
package org.craftercms.studio.model.clipboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Holds the data for a single item to be copied/moved
 *
 * @author joseross
 * @since 3.2
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PasteItem {

    /**
     * The path of the item
     */
    @NotEmpty
    protected String path;

    /**
     * The list of child items to include
     */
    protected List<PasteItem> children;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<PasteItem> getChildren() {
        return children;
    }

    public void setChildren(List<PasteItem> children) {
        this.children = children;
    }

}
