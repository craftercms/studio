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
package org.craftercms.studio.impl.v2.utils.cache;

import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v2.utils.cache.CacheInvalidator;

import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.commons.lang.RegexUtils.matchesAny;

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
     * The list of patterns to match the key
     */
    protected List<String> patterns;

    protected CacheInvalidator<K, V> actualCacheInvalidator;

    @ConstructorProperties({"patterns", "actualCacheInvalidator"})
    public ConditionalCacheInvalidator(List<String> patterns, CacheInvalidator<K, V> actualCacheInvalidator) {
        this.patterns = patterns;
        this.actualCacheInvalidator = actualCacheInvalidator;
    }

    @Override
    public void invalidate(Cache<K, V> cache, K key) {
        logger.debug("Checking if key {} matches patterns {}", key, patterns);
        if (matchesAny(key, patterns)) {
            logger.debug("Invalidating cache for {}", key);
            actualCacheInvalidator.invalidate(cache, key);
        }
    }

}
