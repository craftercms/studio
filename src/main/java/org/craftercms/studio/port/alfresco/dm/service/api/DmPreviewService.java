/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.service.api;

import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

import java.io.InputStream;

/**
 * Provides Alfresco Preview related services for DM
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public interface DmPreviewService {

    void deleteContent(String site, String relativePath) throws ServiceException;

    /**
     * write content to the given path in the preview layer
     *
     * @param site
     *            site to write the content to
     * @param path
     *            path to write the content to
     * @param fileName
     *            content name
     * @param contentType
     * 				content type
     * @param in
     *            content input stream
     * @throws ServiceException
     */
    public void writeContent(String site, String path, String fileName, String contentType, InputStream in) throws ServiceException;

    public void writeContent(String site, String path, String fileName, String contentType, InputStream in,boolean duplicate) throws ServiceException;

    /**
     * clean the given content from the preview layer
     *
     * @param site
     * @param path
     * @throws ServiceException
     */
    public void cleanContent(String site, String path) throws ServiceException;
}
