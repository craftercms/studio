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
package org.craftercms.studio.api.v2.repository.blob;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Holds the reference to a file in a blob store
 *
 * @author joseross
 * @since 3.1.6
 */
@JsonRootName("blob")
public class Blob {

    public Blob(String storeId, String url) {
        this.storeId = storeId;
        this.url = url;
    }

    /**
     * The id of the blob store
     */
    protected String storeId;

    /**
     * The url of the file in the blob store
     */
    protected String url;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
