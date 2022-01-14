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

import java.beans.ConstructorProperties;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Utility class to configure a {@link CacheControl} instance
 *
 * @author joseross
 * @since 4.0.0
 */
public class CacheControlFactory extends AbstractFactoryBean<CacheControl> {

    /**
     * Indicates if the browser should cache responses
     */
    boolean enabled;

    /**
     * The max age in minutes that the browser should cache responses
     */
    long maxAge;

    @ConstructorProperties({"enabled", "maxAge"})
    public CacheControlFactory(boolean enabled, long maxAge) {
        this.enabled = enabled;
        this.maxAge = maxAge;
    }

    @Override
    public Class<?> getObjectType() {
        return CacheControl.class;
    }

    @Override
    protected CacheControl createInstance() throws Exception {
        if (enabled) {
            return CacheControl.maxAge(maxAge, MINUTES).mustRevalidate();
        } else {
            return CacheControl.noStore();
        }
    }

}
