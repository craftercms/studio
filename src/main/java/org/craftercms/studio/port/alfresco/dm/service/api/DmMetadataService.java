/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.service.api;

import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Document;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

/**
 * Created by IntelliJ IDEA.
 * User: dejan
 * Date: 12/30/11
 * Time: 9:03 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DmMetadataService {
    /**
     * extract the metadata of the given content
     *
     * @param site
     * @param user (optional)
     * @param sub (optional)
     * @param path  path to the content
     * @param contentType (optional)
     * @param nodeRef DM noderef for content.(Right now only downloads)
     * @param content (optional)
     * @throws org.craftercms.cstudio.alfresco.service.exception.ServiceException
     */
    public void extractMetadata(String site, String user, String sub, String path, String contentType, NodeRef nodeRef, Document content) throws ServiceException;

    //public void extractMetadataNewNode(String site, String user, String sub, String path, String contentType, NodeRef nodeRef, Document content) throws ServiceException;
}
