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

package org.craftercms.studio.api.v2.service.dependency;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;

import java.util.List;

public interface DependencyService {

    /**
     * Get a soft dependencies of a item. A soft
     * dependency is:
     * * an edited, shared (not item specific) dependency
     *
     * @param site Site to operate on
     * @param path Paths to item to retrieve deps for
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    List<String> getSoftDependencies(String site, String path)
            throws SiteNotFoundException, ServiceLayerException;

    /**
     * Get a soft dependencies of a list of items. A soft
     * dependency is:
     * * an edited, shared (not item specific) dependency
     *
     * @param site Site to operate on
     * @param paths List of paths to items to retrieve deps for
     * @throws SiteNotFoundException Site doesn't exist
     * @throws ServiceLayerException Internal error, see exception details
     */
    List<String> getSoftDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ServiceLayerException;
}
