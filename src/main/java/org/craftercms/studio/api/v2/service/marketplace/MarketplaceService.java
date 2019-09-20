/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v2.service.marketplace;

import java.util.Map;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceException;

/**
 * Provides access to all available Marketplace operations
 *
 * @author joseross
 * @since 3.1.2
 */
public interface MarketplaceService {

    String HEADER_STUDIO_ID = "x-studio-id";
    String HEADER_STUDIO_BUILD = "x-studio-build";
    String HEADER_STUDIO_VERSION = "x-studio-version";
    String HEADER_JAVA_VERSION = "x-java-version";
    String HEADER_OS_NAME = "x-os-name";
    String HEADER_OS_VERSION = "x-os-version";
    String HEADER_OS_ARCH = "x-os-arch";

    /**
     * Performs a search for all available plugins that match the given filters
     * @param type the type of plugins to search
     * @param keywords the keywords to filter plugins
     * @param offset the offset for pagination
     * @param limit the limit for pagination
     * @return the result from the Marketplace
     * @throws MarketplaceException if there is any error performing the search
     */
    Map<String, Object> searchPlugins(String type, String keywords, long offset, long limit)
        throws MarketplaceException;

}
