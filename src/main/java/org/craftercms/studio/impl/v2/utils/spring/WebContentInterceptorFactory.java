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

package org.craftercms.studio.impl.v2.utils.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.beans.ConstructorProperties;
import java.util.concurrent.TimeUnit;

/**
 * {@link AbstractFactoryBean} implementation to configure a {@link WebContentInterceptor} instance
 * to handle paths including etag header.
 *
 * @author jmendeza
 * @since 4.0.3
 */
public class WebContentInterceptorFactory extends AbstractFactoryBean<WebContentInterceptor> {

    // Set the default to max-age=0 so it always has to revalidate
    private static final CacheControl ALWAYS_REVALIDATE = CacheControl.maxAge(0, TimeUnit.SECONDS).mustRevalidate();

    /**
     * {@link CacheControl} instance used when request matches cachedPaths
     */
    private final CacheControl cacheControl;
    /**
     * Path patterns that should include cacheControl
     */
    private final String[] cachedPaths;
    /**
     * Path patterns that should included must-revalidate Cache-Control
     */
    private final String[] alwaysRevalidatePaths;


    @ConstructorProperties({"cacheControl", "cachedPaths", "alwaysRevalidatePaths"})
    public WebContentInterceptorFactory(final CacheControl cacheControl, final String[] cachedPaths,
                                        final String[] alwaysRevalidatePaths) {
        this.cacheControl = cacheControl;
        this.cachedPaths = cachedPaths;
        this.alwaysRevalidatePaths = alwaysRevalidatePaths;
    }

    @Override
    public Class<?> getObjectType() {
        return WebContentInterceptor.class;
    }

    @Override
    @NonNull
    protected WebContentInterceptor createInstance() throws Exception {
        WebContentInterceptor interceptor = new WebContentInterceptor();
        for (String alwaysRevalidatePath : alwaysRevalidatePaths) {
            interceptor.addCacheMapping(ALWAYS_REVALIDATE, alwaysRevalidatePath);
        }
        for (String cachedPath : this.cachedPaths) {
            interceptor.addCacheMapping(cacheControl, cachedPath);
        }
        return interceptor;
    }
}
