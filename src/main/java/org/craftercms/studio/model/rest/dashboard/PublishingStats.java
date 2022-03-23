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

public class PublishingStats {

    private int numberOfPublishes;
    private int numberOfNewAndPublishedItems;
    private int numberOfEditedAndPublishedItems;

    public int getNumberOfPublishes() {
        return numberOfPublishes;
    }

    public void setNumberOfPublishes(int numberOfPublishes) {
        this.numberOfPublishes = numberOfPublishes;
    }

    public int getNumberOfNewAndPublishedItems() {
        return numberOfNewAndPublishedItems;
    }

    public void setNumberOfNewAndPublishedItems(int numberOfNewAndPublishedItems) {
        this.numberOfNewAndPublishedItems = numberOfNewAndPublishedItems;
    }

    public int getNumberOfEditedAndPublishedItems() {
        return numberOfEditedAndPublishedItems;
    }

    public void setNumberOfEditedAndPublishedItems(int numberOfEditedAndPublishedItems) {
        this.numberOfEditedAndPublishedItems = numberOfEditedAndPublishedItems;
    }
}
