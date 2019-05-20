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

package org.craftercms.studio.api.v2.service.cmis;

import org.craftercms.studio.api.v1.exception.CmisPathNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisTimeoutException;
import org.craftercms.studio.api.v1.exception.CmisUnavailableException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.StudioPathNotFoundException;
import org.craftercms.studio.api.v2.dal.CmisContentItem;

import java.io.InputStream;
import java.util.List;

public interface CmisService {

    List<CmisContentItem> list(String siteId, String cmisRepo, String path)
            throws CmisRepositoryNotFoundException, CmisUnavailableException, CmisTimeoutException;

    List<CmisContentItem> search(String siteId, String cmisRepo, String searchTerm, String path)
            throws CmisRepositoryNotFoundException, CmisUnavailableException, CmisTimeoutException;

    void cloneContent(String siteId, String cmisRepoId, String cmisPath, String studioPath)
            throws StudioPathNotFoundException, CmisRepositoryNotFoundException, CmisUnavailableException,
            CmisTimeoutException, CmisPathNotFoundException, ServiceLayerException;

    void uploadContent(String siteId, String cmisRepoId, String cmisPath, String filename, InputStream content)
            throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException,
            CmisPathNotFoundException;
}
