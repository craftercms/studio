/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.service.content;

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.dom4j.Document;

public interface DmMetadataService {
    /**
     * extract the metadata of the given content
     *
     * @param site
     * @param user (optional)
     * @param path  path to the content
     * @param contentType (optional)
     * @param content (optional)
     */
    public void extractMetadata(String site, String user, String path, String contentType, Document content) throws ServiceException;

}
