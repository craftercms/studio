/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.service.deployment;


import org.craftercms.studio.api.v1.exception.ServiceLayerException;

public interface DmPublishService {

    /**
     * Start executing bulk publish for given site, path on given environment
     *
     * @param site site identifier
     * @param environment environment to publish to
     * @param path base path for bulk publish
     * @param comment submission comment
     *
     * @throws ServiceLayerException exception is case of en error
     */
    void bulkGoLive(String site, String environment, String path, String comment) throws ServiceLayerException;
}
