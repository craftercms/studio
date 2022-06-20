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

package org.craftercms.studio.api.v2.service.cmis;

import org.craftercms.studio.api.v1.exception.CmisPathNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisTimeoutException;
import org.craftercms.studio.api.v1.exception.CmisUnavailableException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.CmisContentItem;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.model.rest.CmisUploadItem;

import java.io.InputStream;
import java.util.List;

public interface CmisService {

    List<CmisContentItem> list(String siteId, String cmisRepo, String path)
            throws CmisRepositoryNotFoundException, CmisUnavailableException, CmisTimeoutException, ConfigurationException;

    List<CmisContentItem> search(String siteId, String cmisRepo, String searchTerm, String path)
            throws CmisRepositoryNotFoundException, CmisUnavailableException, CmisTimeoutException, ConfigurationException;

    void cloneContent(String siteId, String cmisRepoId, String cmisPath, String studioPath)
            throws CmisRepositoryNotFoundException, CmisUnavailableException,
            CmisTimeoutException, CmisPathNotFoundException, ServiceLayerException, UserNotFoundException;

    CmisUploadItem uploadContent(String siteId, String cmisRepoId, String cmisPath, String filename, InputStream content)
            throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException,
            CmisPathNotFoundException, ConfigurationException;
}
