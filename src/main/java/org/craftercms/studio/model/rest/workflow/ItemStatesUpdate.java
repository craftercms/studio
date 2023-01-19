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

package org.craftercms.studio.model.rest.workflow;

public class ItemStatesUpdate {
    private boolean clearSystemProcessing;
    private boolean clearUserLocked;
    private Boolean isNew;
    private Boolean modified;
    private Boolean live;
    private Boolean staged;

    public boolean isClearSystemProcessing() {
        return clearSystemProcessing;
    }

    public void setClearSystemProcessing(boolean clearSystemProcessing) {
        this.clearSystemProcessing = clearSystemProcessing;
    }

    public boolean isClearUserLocked() {
        return clearUserLocked;
    }

    public void setClearUserLocked(boolean clearUserLocked) {
        this.clearUserLocked = clearUserLocked;
    }

    public Boolean getNew() {
        return isNew;
    }

    public void setNew(final Boolean isNew) {
        this.isNew = isNew;
    }

    public Boolean getModified() {
        return modified;
    }

    public void setModified(final Boolean modified) {
        this.modified = modified;
    }

    public Boolean getLive() {
        return live;
    }

    public void setLive(Boolean live) {
        this.live = live;
    }

    public Boolean getStaged() {
        return staged;
    }

    public void setStaged(Boolean staged) {
        this.staged = staged;
    }
}
