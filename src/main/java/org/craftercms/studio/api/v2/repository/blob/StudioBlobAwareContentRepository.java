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
package org.craftercms.studio.api.v2.repository.blob;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.repository.ContentRepository;

/**
 * {@link ContentRepository} extension that provides blob aware operations.
 */
public interface StudioBlobAwareContentRepository extends ContentRepository {

    /**
     * Extract every blob from the source site and storeit in the target site in the blob store matching its path
     *
     * @param sourceSiteId the source site
     * @param siteId       the target site
     * @throws ServiceLayerException if an error occurs during the operation
     */
    void duplicateBlobs(String sourceSiteId, String siteId) throws ServiceLayerException;
}
