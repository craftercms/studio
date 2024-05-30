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
package org.craftercms.studio.api.v1.service.deployment;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;

import java.util.List;

/**
 * 	// document
 */
public interface DeploymentService {

    /**
     * Start executing bulk publish for given site, path on given environment
     *
     * @param site        site identifier
     * @param environment environment to publish to
     * @param path        base path for bulk publish
     * @param comment     submission comment
     * @return the created publish package id
     * @throws ServiceLayerException exception is case of en error
     */
    long bulkGoLive(String site, String environment, String path, String comment) throws ServiceLayerException, AuthenticationException;

    /**
     * Enable/Disable publishing for given site
     *
     * @param site    site id
     * @param enabled true to enable publishing, false to disable publishing
     * @throws SiteNotFoundException
     */
    void enablePublishing(String site, boolean enabled) throws SiteNotFoundException, AuthenticationException;

    /**
     * Publish given commit IDs on given environment for given site
     *
     * @param site        site id to use for publishing
     * @param environment environment to use for publishing
     * @param commitIds   commit IDs to publish
     * @return the created publish package id
     */
    long publishCommits(String site, String environment, List<String> commitIds, String comment)
            throws ServiceLayerException, AuthenticationException;


    /**
     * Reset staging environment to live for given site
     *
     * @param siteId site id to use for resetting
     */
    void resetStagingEnvironment(String siteId) throws ServiceLayerException, CryptoException;
}
