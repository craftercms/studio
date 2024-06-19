/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal.publish;

import java.time.Instant;

/**
 * Represents a record from item_target table containing the old path of an item
 * for a publishing target, if any.
 */
public class ItemTarget {
    protected long itemId;
    protected String target;
    protected String oldPath;
    protected Instant lastPublishedOn;
    protected String publishedCommitId;

    public long getItemId() {
        return itemId;
    }

    @SuppressWarnings("unused")
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getTarget() {
        return target;
    }

    @SuppressWarnings("unused")
    public void setTarget(String target) {
        this.target = target;
    }

    public String getOldPath() {
        return oldPath;
    }

    @SuppressWarnings("unused")
    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public Instant getLastPublishedOn() {
        return lastPublishedOn;
    }

    @SuppressWarnings("unused")
    public void setLastPublishedOn(Instant lastPublishedOn) {
        this.lastPublishedOn = lastPublishedOn;
    }

    public String getPublishedCommitId() {
        return publishedCommitId;
    }

    @SuppressWarnings("unused")
    public void setPublishedCommitId(String publishedCommitId) {
        this.publishedCommitId = publishedCommitId;
    }
}
