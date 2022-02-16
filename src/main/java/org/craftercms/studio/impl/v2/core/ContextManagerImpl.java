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
package org.craftercms.studio.impl.v2.core;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.studio.api.v2.core.ContextManager;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.beans.ConstructorProperties;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter.STORE_TYPE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SANDBOX_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

/**
 * Default implementation of {@link ContextManager}
 *
 * @author joseross
 * @since 4.0.0
 */
public class ContextManagerImpl implements ContextManager {

    public static final String TAG = ContextManagerImpl.class.getName();

    protected StudioConfiguration studioConfiguration;
    protected ContentStoreService contentStoreService;

    protected Map<String, Context> contexts = new ConcurrentHashMap<>();

    @ConstructorProperties({"studioConfiguration", "contentStoreService"})
    public ContextManagerImpl(StudioConfiguration studioConfiguration, ContentStoreService contentStoreService) {
        this.studioConfiguration = studioConfiguration;
        this.contentStoreService = contentStoreService;
    }

    @Override
    public Context getContext(String siteId) {
        if (!contexts.containsKey(siteId)) {
            var rootFolder =  "file://" + Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                    studioConfiguration.getProperty(SITES_REPOS_PATH), siteId,
                    studioConfiguration.getProperty(SANDBOX_PATH))
                    .toAbsolutePath().toString();
            contexts.put(siteId, contentStoreService.getContext(TAG, STORE_TYPE, rootFolder, true, false, 0, true));
        }
        return contexts.get(siteId);
    }

    @Override
    public void destroyContext(String siteId) {
        var context = contexts.remove(siteId);
        if (context != null) {
            contentStoreService.destroyContext(context);
        }
    }

}
