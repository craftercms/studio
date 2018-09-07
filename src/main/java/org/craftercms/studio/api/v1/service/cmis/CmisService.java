/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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

package org.craftercms.studio.api.v1.service.cmis;

import org.craftercms.studio.api.v1.exception.*;
import org.craftercms.studio.api.v1.to.CmisContentItemTO;

import java.util.List;

/** Cmis Service **/
public interface CmisService {

    int listTotal(String site, String cmisRepo, String path)
        throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException;

    List<CmisContentItemTO> list(String site, String cmisRepo, String path, int start, int number)
        throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException;

    long searchTotal(String site, String cmisRepo, String searchTerm, String path)
        throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException;

    List<CmisContentItemTO> search(String site, String cmisRepo, String searchTerm, String path, int start, int number)
        throws CmisUnavailableException, CmisTimeoutException, CmisRepositoryNotFoundException;

    void cloneContent(String siteId, String cmisRepoId, String cmisPath, String studioPath)
        throws CmisUnavailableException, CmisTimeoutException, CmisPathNotFoundException, ServiceLayerException,
        StudioPathNotFoundException, CmisRepositoryNotFoundException;
}
