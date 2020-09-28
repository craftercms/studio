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

package org.craftercms.studio.api.v2.repository;

import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RepoOperation;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface ContentRepository {

    /**
     * List subtree items for give site and path
     *
     * @param site site identifier
     * @param path path for subtree root
     * @return list of item paths contained in the subtree
     */
    List<String> getSubtreeItems(String site, String path);

    /**
     * Get a list of operations since the commit ID provided (compare that commit to HEAD)
     *
     * @param site         site to use
     * @param commitIdFrom commit ID to start at
     * @param commitIdTo   commit ID to end at
     * @return commit ID of current HEAD, updated operationsSinceCommit
     */
    List<RepoOperation> getOperations(String site, String commitIdFrom, String commitIdTo);

    /**
     * Get a list of operations since the commit ID provided (compare that commit to HEAD)
     *
     * @param site         site to use
     * @param commitIdFrom commit ID to start at
     * @param commitIdTo   commit ID to end at
     * @return commit ID of current HEAD, updated operationsSinceCommit
     */
    List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo);

    /**
     * Get first id from repository for given site
     *
     * @param site site id
     * @return first commit id
     */
    String getRepoFirstCommitId(String site);

    /**
     * Get git log object from database
     *
     * @param siteId   site id
     * @param commitId commit ID
     * @return git log object
     */
    GitLog getGitLog(String siteId, String commitId);

    /**
     * Mark Git log as verified
     *
     * @param siteId   site identifier
     * @param commitId commit id
     */
    void markGitLogVerifiedProcessed(String siteId, String commitId);

    /**
     * Insert Git Log
     *
     * @param siteId    site
     * @param commitId  commit ID
     * @param processed processed
     */
    void insertGitLog(String siteId, String commitId, int processed);

    /**
     * Get publishing history
     *
     * @param siteId site identifier
     * @param environment environment
     * @param path path regular expression to use as filter
     * @param publisher user to filter by
     * @param fromDate lower boundary for published date
     * @param toDate upper boundary for published date
     * @param limit number of records to return
     * @return publishing history
     */
    List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String path,
                                                     String publisher, ZonedDateTime fromDate, ZonedDateTime toDate,
                                                     int limit);

    /**
     * Create a new site based on a blueprint
     *
     * @param blueprintLocation blueprint location
     * @param siteId site identifier
     * @param sandboxBranch sandbox branch name
     * @param params site parameters
     * @return true if successful, false otherwise
     */
    boolean createSiteFromBlueprint(String blueprintLocation, String siteId, String sandboxBranch,
                                    Map<String, String> params);

    /**
     * Publish content to specified environment.
     *
     * @param siteId site identifier
     * @param sandboxBranch sandbox branch name
     * @param deploymentItems items to be published
     * @param environment environment to publish to
     * @param author author
     * @param comment submission comment
     * @throws DeploymentException deployment error
     */
    void publish(String siteId, String sandboxBranch, List<DeploymentItemTO> deploymentItems, String environment,
                 String author, String comment) throws DeploymentException;

    /**
     * Check if repository exists for  given site
     *
     * @param site     site id
     * @return true if it repository exists, otherwise false
     */
    boolean repositoryExists(String site);

    /**
     * Check if given commit id exists
     *
     * @param site     site id
     * @param commitId commit id to check
     * @return true if it exists in site repository, otherwise false
     */
    boolean commitIdExists(String site, String commitId);

    /**
     * Create new site as a clone from remote repository
     *
     * @param siteId         site identifier
     * @param sandboxBranch  sandbox branch name
     * @param remoteName     remote name
     * @param remoteUrl      remote repository url
     * @param remoteBranch   remote branch name
     * @param singleBranch   flag to signal if clone single branch or full repository
     * @param authenticationType type of authentication to use to connect remote repository
     * @param remoteUsername remote username
     * @param remotePassword remote password
     * @param remoteToken    remote token
     * @param remotePrivateKey remote private key
     * @param params         site parameters
     * @param createAsOrphan create as orphan
     *
     * @return true if success
     *
     * @throws InvalidRemoteRepositoryException invalid remote repository
     * @throws InvalidRemoteRepositoryCredentialsException invalid credentials for remote repository
     * @throws RemoteRepositoryNotFoundException remote repository not found
     * @throws InvalidRemoteUrlException invalid url for remote repository
     * @throws ServiceLayerException general service error
     */
    boolean createSiteCloneRemote(String siteId, String sandboxBranch, String remoteName, String remoteUrl,
                                  String remoteBranch, boolean singleBranch, String authenticationType,
                                  String remoteUsername, String remotePassword, String remoteToken,
                                  String remotePrivateKey, Map<String, String> params, boolean createAsOrphan)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, InvalidRemoteUrlException, ServiceLayerException;

    /**
     * Remove remote with given name for site
     *
     * @param siteId     site identifier
     * @param remoteName remote name
     * @return true if operation was successful
     */
    boolean removeRemote(String siteId, String remoteName);

    /**
     * Determine if content exists in the repository at a given path
     *
     * @param site site id where the operation will be executed
     * @param path
     * @return true if site has content object at path
     */
    boolean contentExists(String site, String path);

    /**
     * Get last commit id from repository for given site.
     *
     * @param site site id
     * @return last commit id (current HEAD)
     */
    String getRepoLastCommitId(String site);

    Item getItem(String siteId, String path);

}
