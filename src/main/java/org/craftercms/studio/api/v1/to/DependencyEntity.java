/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

public class DependencyEntity implements Serializable {

    private static final long serialVersionUID = 8269131276224604766L;

    /** properties **/
    protected String _id;
    public String getId() {
        return _id;
    }
    public void setId(String id) {
        this._id = id;
    }

    protected String _site;
    public String getSite() {
        return _site;
    }
    public void setSite(String site) {
        this._site = site;
    }
    protected String _sourcePath;
    public String getSourcePath() {
        return _sourcePath;
    }
    public void setSourcePath(String sourcePath) {
        this._sourcePath = sourcePath;
    }

    protected String _targetPath;
    public String getTargetPath() {
        return _targetPath;
    }
    public void setTargetPath(String targetPath) {
        this._targetPath = targetPath;
    }

    protected String _type;
    public String getType() {
        return _type;
    }
    public void setType(String type) {
        this._type = type;
    }


    /**
     * default constructor
     */
    public DependencyEntity() {}

    /**
     * constructor
     *
     * @param site
     * @param sourcePath
     */
    public DependencyEntity(String site, String sourcePath) {
        this.setSite(site);
        this.setSourcePath(sourcePath);
    }

    /**
     * constructor
     *
     * @param site
     * @param sourcePath
     * @param targetPath
     * @param type
     */
    public DependencyEntity(String site, String sourcePath, String targetPath, String type) {
        this.setSite(site);
        this.setSourcePath(sourcePath);
        this.setTargetPath(targetPath);
        this.setType(type);
    }

    public String toString() {
        return "id: " + _id + ", site: " + _site + ", soruce: " + _sourcePath
                + ", target:" + _targetPath + ", type:";
    }
}
