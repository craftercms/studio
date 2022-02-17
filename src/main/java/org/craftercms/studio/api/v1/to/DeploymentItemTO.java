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
import java.util.Objects;

public class DeploymentItemTO implements Serializable {
    private static final long serialVersionUID = -14642162984484167L;

    protected String site;
    protected String path;
    protected String commitId;
    protected String packageId;
    protected boolean move;
    protected boolean delete;
    protected String oldPath;

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentItemTO)) return false;
        DeploymentItemTO that = (DeploymentItemTO) o;
        return move == that.move &&
                delete == that.delete &&
                Objects.equals(site, that.site) &&
                Objects.equals(path, that.path) &&
                Objects.equals(commitId, that.commitId) &&
                Objects.equals(packageId, that.packageId) &&
                Objects.equals(oldPath, that.oldPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, path, commitId, packageId, move, delete, oldPath);
    }

    @Override
    public String toString() {
        return "DeploymentItemTO{" +
                "site='" + site + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

}
