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

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CLUSTER_NODE_REMOTE_NAME_PREFIX;

public class RemoteRepositoryInfo implements Serializable {

    private static final long serialVersionUID = -47166234696510116L;

    private String name;
    private String url;
    private String fetch;
    private String pushUrl;
    private List<String> branches;
    private boolean reachable = true;
    private String unreachableReason;
    private boolean removable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.removable = !StringUtils.startsWith(name, CLUSTER_NODE_REMOTE_NAME_PREFIX);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFetch() {
        return fetch;
    }

    public void setFetch(String fetch) {
        this.fetch = fetch;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public List<String> getBranches() {
        return branches;
    }

    public void setBranches(List<String> branches) {
        this.branches = branches;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public String getUnreachableReason() {
        return unreachableReason;
    }

    public void setUnreachableReason(String unreachableReason) {
        this.unreachableReason = unreachableReason;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void setRemovable(boolean removable) {
        // not used
    }
}
