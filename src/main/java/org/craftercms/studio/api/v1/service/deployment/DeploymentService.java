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
import org.craftercms.studio.api.v1.exception.CommitNotFoundException;
import org.craftercms.studio.api.v1.exception.EnvironmentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.dal.PublishRequest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * 	// document
 */
public interface DeploymentService {

    // document
    void deploy(String site, String environment, List<String> paths, ZonedDateTime scheduledDate, String approver,
                String submissionComment, final boolean scheduleDateNow)
            throws DeploymentException, ServiceLayerException, UserNotFoundException;

    /**
     * Delete content
     * @param site site identifier
     * @param paths list of paths to delete
     * @param approver user that approved deletion
     * @param scheduledDate scheduled date to execute deletion
     * @param submissionComment submission comment
     * @throws DeploymentException general deployment error
     * @throws SiteNotFoundException if site does not exist
     */
    void delete(String site, List<String> paths, String approver, ZonedDateTime scheduledDate, String submissionComment)
            throws DeploymentException, ServiceLayerException, UserNotFoundException;

    List<PublishRequest> getScheduledItems(String site, String filterType);

    void cancelWorkflow(String site, String path) throws DeploymentException;

    void cancelWorkflowBulk(String site, Set<String> paths) throws DeploymentException;

    void deleteDeploymentDataForSite(String site);

    List<ContentItemTO> getScheduledItems(String site, String sort, boolean ascending, String subSort,
                                          boolean subAscending, String filterType) throws ServiceLayerException;

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

    /**
     * Enable/Disable publishing for given site
     * @param site site id
     * @param enabled true to enable publishing, false to disable publishing
     * @throws SiteNotFoundException
     */
    boolean enablePublishing(String site, boolean enabled) throws SiteNotFoundException, AuthenticationException;

    /**
     * Publish given commit IDs on given environment for given site
     * @param site site id to use for publishing
     * @param environment environment to use for publishing
     * @param commitIds commit IDs to publish
     */
    void publishCommits(String site, String environment, List<String> commitIds, String comment)
            throws SiteNotFoundException, EnvironmentNotFoundException, CommitNotFoundException;

    /**
     * Publish items in given environment for given site
     * @param site site id to use for publishing
     * @param environment environment to use for publishing
     * @param paths item paths to publish
     * @throws SiteNotFoundException
     * @throws EnvironmentNotFoundException
     */
    void publishItems(String site, String environment, ZonedDateTime schedule, List<String> paths,
                      String submissionComment)
            throws ServiceLayerException, DeploymentException, UserNotFoundException;

    /**
     * Reset staging environment to live for given site
     *
     * @param siteId site id to use for resetting
     */
    void resetStagingEnvironment(String siteId) throws ServiceLayerException, CryptoException;
}
