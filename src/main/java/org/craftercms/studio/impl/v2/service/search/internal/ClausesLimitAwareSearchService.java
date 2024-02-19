/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.service.search.internal;

import org.craftercms.search.opensearch.exception.TooManyNestedClausesSearchException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.search.SearchService;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;

import static java.lang.String.format;
import static org.craftercms.core.util.ExceptionUtils.getThrowableOfType;

/**
 * A search service that is aware of {@link org.craftercms.search.opensearch.exception.TooManyNestedClausesSearchException}
 * and will retry failing queries with a lower number of max expansions.
 * It will stop retrying once max expansions reaches 1, a different exception is thrown or the query succeeds.
 */
public class ClausesLimitAwareSearchService implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(ClausesLimitAwareSearchService.class);

    protected final SearchService actualSearchService;

    @ConstructorProperties({"actualSearchService"})
    public ClausesLimitAwareSearchService(final SearchService actualSearchService) {
        this.actualSearchService = actualSearchService;
    }

    @Override
    public SearchResult search(final String siteId, final SearchParams params, final int initialMaxExpansions) throws ServiceLayerException {
        int maxExpansions = initialMaxExpansions;
        TooManyNestedClausesSearchException lastException;
        do {
            try {
                return actualSearchService.search(siteId, params, maxExpansions);
            } catch (Exception e) {
                TooManyNestedClausesSearchException tooManyClausesException = getThrowableOfType(e, TooManyNestedClausesSearchException.class);
                if (tooManyClausesException == null) {
                    throw e;
                }
                lastException = tooManyClausesException;
                logger.warn("Search query for site '{}' with max_expansions '{}' contains too many nested clauses, " +
                        ((maxExpansions > 1) ? "retrying with a lower number of max_expansions" : ""), siteId, maxExpansions);
                maxExpansions = maxExpansions / 2;
            }
        } while (maxExpansions >= 1);
        throw new ServiceLayerException(format("Search query for site '%s' contains too many nested clauses", siteId), lastException);
    }
}
