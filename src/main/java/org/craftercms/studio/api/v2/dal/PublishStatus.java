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

package org.craftercms.studio.api.v2.dal;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PublishStatus {

    public static final String READY = "ready";
    public static final String QUEUED = "queued";
    public static final String PUBLISHING = "publishing";
    public static final String STOPPED = "stopped";
    public static final String ERROR = "error";

    @JsonIgnore
    private long id;
    private boolean enabled;
    @JsonIgnore
    private int enabledAsInt;
    private String status;
    private String message;
    private String lockOwner;
    private String lockTTL;

    @JsonIgnore
    public long getId() {
        return id;
    }

    @JsonIgnore
    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public String getLockTTL() {
        return lockTTL;
    }

    public void setLockTTL(String lockTTL) {
        this.lockTTL = lockTTL;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.enabledAsInt = enabled ? 1 : 0;
    }

    @JsonIgnore
    public int getEnabledAsInt() {
        return enabledAsInt;
    }

    @JsonIgnore
    public void setEnabledAsInt(int enabledAsInt) {
        this.enabledAsInt = enabledAsInt;
        this.enabled = (enabledAsInt == 0) ? false : true;
    }
}
