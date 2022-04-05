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
package org.craftercms.studio.model.rest.dashboard;

import java.time.ZonedDateTime;

/**
 * Holds the data for an item that is close to or has already expired
 *
 * @author joseross
 * @since 4.0.0
 */
public class ExpiringContentItem {

    /**
     * The name of the item
     */
    protected String itemName;

    /**
     * The path of the item
     */
    protected String itemPath;

    /**
     * The expiry data of the item
     */
    protected ZonedDateTime expiredDateTime;

    public ExpiringContentItem(String itemName, String itemPath, ZonedDateTime expiredDateTime) {
        this.itemName = itemName;
        this.itemPath = itemPath;
        this.expiredDateTime = expiredDateTime;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemPath() {
        return itemPath;
    }

    public ZonedDateTime getExpiredDateTime() {
        return expiredDateTime;
    }

}
