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

package org.craftercms.studio.api.v2.repository;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Interface for content repositories that support publishing
 */
public interface PublishCapableContentRepository {
    /**
     * Execute initial publish for given site
     *
     * @param siteId site identifier
     * @return commit id of the initial publish.
     * After this method runs, the returned value is the same as the last
     * commit in the published repository for both branches(live and staging, if configured)
     */
    String initialPublish(String siteId) throws ServiceLayerException;
}
