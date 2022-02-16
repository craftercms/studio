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

package org.craftercms.studio.model.ui;

/**
 * Represents an item that is displayed in a UI menu.
 *
 * @author avasquez
 */
public class MenuItem {

    private String id;
    private String label;
    private String icon;

    /**
     * Returns the ID of the item.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the item.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the label of the item.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of the item.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the icon of the item as a Font Awesome id, example: fa-smile.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the icon of the item as a Font Awesome id, example: fa-smile.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
}
