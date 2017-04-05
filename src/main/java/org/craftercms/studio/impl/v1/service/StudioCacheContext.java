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
    public ContentStoreAdapter getStoreAdapter() {
        return null;
    }

    @Override
    public String getStoreServerUrl() {
        return null;
    }

    @Override
    public String getRootFolderPath() {
        return null;
    }

    @Override
    public boolean isMergingOn() {
        return false;   // TODO: SJ: Validate with Dejan and Alfonso
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

    protected boolean isConfig;
    protected String site;
    protected String contextId;

}
