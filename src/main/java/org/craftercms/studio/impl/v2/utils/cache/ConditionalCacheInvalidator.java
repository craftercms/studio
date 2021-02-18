/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.utils.cache;

import com.google.common.cache.Cache;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.cache.CacheInvalidator;

/**
 * Implementation of {@link CacheInvalidator} that only performs the invalidation if the key matches a pattern
 *
 * @author joseross
 * @since 4.0
 * @param <K> the type for the keys
 * @param <V> the type for the values
 */
public class ConditionalCacheInvalidator<K extends String, V> implements CacheInvalidator<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(ConditionalCacheInvalidator.class);

    /**
     * The pattern to match the key
     */
    protected String pattern;

    protected CacheInvalidator<K, V> actualCacheInvalidator;

    public ConditionalCacheInvalidator(String pattern, CacheInvalidator<K, V> actualCacheInvalidator) {
        this.pattern = pattern;
        this.actualCacheInvalidator = actualCacheInvalidator;
    }

    @Override
    public void invalidate(Cache<K, V> cache, K key) {
        logger.debug("Checking if key {0} matches pattern {1}", key, pattern);
        if (key.matches(pattern)) {
            logger.debug("Invalidating cache for {0}", key);
            actualCacheInvalidator.invalidate(cache, key);
        }
    }

}
