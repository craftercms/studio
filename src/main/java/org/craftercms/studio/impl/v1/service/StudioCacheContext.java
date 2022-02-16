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

package org.craftercms.studio.impl.v1.service;

import org.craftercms.core.service.Context;
import org.craftercms.core.store.ContentStoreAdapter;

public class StudioCacheContext implements Context {

    private final String CONFIG_CONTEXT = "StudioConfiguration";
    private final String CONTENT_CONTEXT = "StudioContent";

    public StudioCacheContext(String site) {
        this(site, false);
    }

    public StudioCacheContext(String site, boolean isConfig) {
        this.isConfig = isConfig;
        this.site = site;
        StringBuilder sb = new StringBuilder();
        if (isConfig) {
            sb.append(CONFIG_CONTEXT);
        } else {
            sb.append(CONTENT_CONTEXT);
        }
        sb.append(":").append(site);
        contextId = sb.toString();
    }

    @Override
    public String getId() {
        return contextId;
    }

    @Override
    public long getCacheVersion() {
        return 1;
    }

    @Override
    public void setCacheVersion(long cacheVersion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCacheScope() {
        return getId();
    }

    @Override
    public ContentStoreAdapter getStoreAdapter() {
        return null;
    }

    @Override
    public boolean isMergingOn() {
        return DEFAULT_MERGING_ON;
    }

    @Override
    public boolean isCacheOn() {
        return DEFAULT_CACHE_ON;
    }

    @Override
    public int getMaxAllowedItemsInCache() {
        return DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE;
    }

    @Override
    public boolean ignoreHiddenFiles() {
        return DEFAULT_IGNORE_HIDDEN_FILES;
    }

    @Override
    public Context clone() {
        try {
            return (Context) super.clone();
        } catch (CloneNotSupportedException e) {
            // Shouldn't happen
            throw new RuntimeException(e);
        }
    }

    protected boolean isConfig;
    protected String site;
    protected String contextId;

}
