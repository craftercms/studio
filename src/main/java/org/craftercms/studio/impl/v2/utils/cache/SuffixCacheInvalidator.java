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

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

/**
 * Implementation of {@link CacheInvalidator} that appends a suffix to the key
 *
 * @author joseross
 * @since 4.0
 * @param <K> the type for the keys
 * @param <V> the type for the values
 */
public class SuffixCacheInvalidator<K extends String, V> implements CacheInvalidator<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(SuffixCacheInvalidator.class);

    /**
     * The default separator for the suffix
     */
    public static final String DEFAULT_SEPARATOR = ":";

    /**
     * The separator for the suffix
     */
    protected String separator = DEFAULT_SEPARATOR;

    /**
     * The suffix to append
     */
    protected String suffix;

    @ConstructorProperties({"suffix"})
    public SuffixCacheInvalidator(String suffix) {
        this.suffix = suffix;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public void invalidate(Cache<K, V> cache, K key) {
        if (!endsWithIgnoreCase(key, separator + suffix)) {
            var newKey = key + separator + suffix;
            logger.debug("Invalidating cache for {0}", newKey);
            cache.invalidate(newKey);
        }
    }

}
