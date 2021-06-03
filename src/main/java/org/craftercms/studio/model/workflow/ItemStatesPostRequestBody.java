/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.workflow;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class ItemStatesPostRequestBody {

    @NotEmpty
    private String siteId;
    @NotEmpty
    private List<String> items;
    private boolean clearSystemProcessing;
    private boolean clearUserLocked;
    private boolean live;
    private boolean staged;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

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

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public boolean isStaged() {
        return staged;
    }

    public void setStaged(boolean staged) {
        this.staged = staged;
    }
}
