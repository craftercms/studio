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

/**
 * DmFolderConfig that specifies how each folder in UI maps to the folder
 * structure in DM web project
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class DmFolderConfigTO implements Serializable {

    private static final long serialVersionUID = 8918826498122949858L;
    protected boolean _attachRootPrefix;
    protected boolean _readDirectChildren;
    protected String _path;
    protected String _name;

    /**
     * @return the attachRootPrefix
     */
    public boolean isAttachRootPrefix() {
        return _attachRootPrefix;
    }

    /**
     * @param attachRootPrefix
     *            the attachRootPrefix to set
     */
    public void setAttachRootPrefix(boolean attachRootPrefix) {
        this._attachRootPrefix = attachRootPrefix;
    }

    /**
     * @return the readDirectChildren
     */
    public boolean isReadDirectChildren() {
        return _readDirectChildren;
    }

    /**
     * @param readDirectChildren
     *            the readDirectChildren to set
     */
    public void setReadDirectChildren(boolean readDirectChildren) {
        this._readDirectChildren = readDirectChildren;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return _path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this._path = path;
    }

    /**
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this._name = name;
    }
}
