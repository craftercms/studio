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
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.cache.CacheInvalidator;

import java.beans.ConstructorProperties;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.startsWith;

/**
 * Implementation of {@link CacheInvalidator} that invalidates all keys matching a pattern
 *
 * @author joseross
 * @since 4.0
 * @param <K> the type for the keys
 * @param <V> the type for the values
 */
public class PatternCacheInvalidator<K extends String, V> implements CacheInvalidator<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(PatternCacheInvalidator.class);

    /**
     * The pattern to match keys
     */
    protected String pattern;

    @ConstructorProperties({"pattern"})
    public PatternCacheInvalidator(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void invalidate(Cache<K, V> cache, K key) {
        var tokens = key.split(":");
        var siteId = tokens.length > 1? tokens[0] : null;

        if (isNotEmpty(siteId)) {
            logger.debug("The original key contains a siteId, matches will be limited to the same siteId");
        }

        logger.debug("Looking for keys matching {0}", pattern);
        var matchingKeys = cache.asMap().keySet().stream()
                .filter(k -> k.matches(pattern)) // include keys that match the pattern
                .filter(k -> isEmpty(siteId) || startsWith(k, siteId)) // include only keys for the same site
                .filter(not(key::equals)) // exclude the original to avoid double invalidation
                .collect(toList());
        logger.debug("Invalidating cache for keys {0}", matchingKeys);
        cache.invalidateAll(matchingKeys);
    }

}
