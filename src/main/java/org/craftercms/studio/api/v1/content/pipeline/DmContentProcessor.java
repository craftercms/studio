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
package org.craftercms.studio.api.v1.content.pipeline;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.ContentItemTO;

public interface DmContentProcessor {

    /**
     * create missing folders in the given path
     *
     * @param site
     * @param path
     * @param isPreview
     * @return last child folder in the path
     */
    ContentItemTO createMissingFoldersInPath(String site, String path, boolean isPreview)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * change file to folder content. See WcmClipboardServiceImpl when updating this logic.
     * Duplicate exists due to prevent circular dependency
     *
     * @param site site id
     * @param path content path
     * @return new content path
     */
    String fileToFolder(String site, String path) throws ServiceLayerException, UserNotFoundException;
}
