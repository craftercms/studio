/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Provides access to all known instances of {@link BlobStore}
 *
 * @author joseross
 * @since 3.1.6
 */
public interface BlobStoreFactory {

    String CONFIG_KEY_STORE = "blobStore";
    String CONFIG_KEY_TYPE = "type";

    /**
     * Returns the first {@link BlobStore} compatible with all given paths
     *
     * @param site the id of the site
     * @param paths the lists of paths to check
     * @return the blob store object
     * @throws ServiceLayerException if there is any error looking up the stores
     * @throws ConfigurationException if there is any error reading the configuration
     */
    BlobStore getByPaths(String site, String[] paths) throws ServiceLayerException, ConfigurationException;

}
