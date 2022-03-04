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
import java.util.List;

public class DmPasteItemTO implements Serializable {

    private static final long serialVersionUID = -8894929242693343887L;
    /** uri of this item **/
    protected String _uri;
    public String getUri() {
        return _uri;
    }
    public void setUri(String uri) {
        this._uri = uri;
    }

    /** is deep copy? **/
    protected boolean _deep;
    public boolean isDeep() {
        return _deep;
    }
    public void setDeep(boolean deep) {
        this._deep = deep;
    }

    /** a list of children **/
    protected List<DmPasteItemTO> _children;
    public List<DmPasteItemTO> getChildren() {
        return _children;
    }
    public void setChildren(List<DmPasteItemTO> children) {
        this._children = children;
    }
}
