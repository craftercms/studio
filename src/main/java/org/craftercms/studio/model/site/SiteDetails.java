/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.site;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.model.blobstore.BlobStoreDetails;

import java.util.List;

/**
 * Site details.
 * Extra information (blob stores config) is meant to be used by the UI during duplicate site operations.
 */
public class SiteDetails {
    @JsonUnwrapped
    private final SiteFeed siteFeed;

    private final List<BlobStoreDetails> blobStores;

    public SiteDetails(SiteFeed siteFeed, List<BlobStoreDetails> blobStores) {
        this.siteFeed = siteFeed;
        this.blobStores = blobStores;
    }

    public SiteFeed getSiteFeed() {
        return siteFeed;
    }

    public List<BlobStoreDetails> getBlobStores() {
        return blobStores;
    }
}
