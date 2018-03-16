/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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

package org.craftercms.studio.api.v1.repository;


import org.craftercms.studio.api.v1.dal.GitLog;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.repository.*;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v1.to.RepoOperationTO;
import org.craftercms.studio.api.v1.to.VersionTO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * This interface represents the repository layer of Crafter Studio.  All interaction with the backend
 * Store must go through this interface.
 * @author russdanner
 */
public interface ContentRepository {

    /**
     * Determine if content exists in the repository at a given path
     *
     * @param site site id where the operation will be executed
     * @param path
     * @return true if site has content object at path
     */
    boolean contentExists(String site, String path);

    /**
     * get document from wcm content
     *
     * @param site site id where the operation will be executed
     * @param path
     * @return document
     */
    InputStream getContent(String site, String path) throws ContentNotFoundException;

    /**
     * get file size
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @return Size in bytes
     */
    long getContentSize(String site, String path);

    /**
     * write content
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @param content stream of content to write
     * @return Commit Id if successful, null otherwise
     */
    String writeContent(String site, String path, InputStream content) throws ServiceException;

    /**
     * create a folder
     *
     * @param site site id where the operation will be executed
     * @param path path to create a folder in
     * @param name a folder name to create
     * @return Commit Id if successful, null otherwise
     */
    String createFolder(String site, String path, String name);

    /**
     * delete content
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @param approver user that approves delete content
     * @return Commit ID if successful, null otherwise
     */
    String deleteContent(String site, String path, String approver);

    /**
     * move content from PathA to pathB
     *
     * @param site site id where the operation will be executed
     * @param fromPath source content
     * @param toPath target path
     * @return Commit ID if successful, null otherwise
     */
    Map<String, String> moveContent(String site, String fromPath, String toPath);

    /**
     * move content from PathA to pathB
     *
     * @param site site id where the operation will be executed
     * @param fromPath source content
     * @param toPath target path
     * @param newName new file name for rename
     * @return Commit ID if successful, empty string otherwise
     */
    // TODO: SJ: Should refactor to be from path to path without the newName param
    Map<String, String> moveContent(String site, String fromPath, String toPath, String newName);

    /**
     * copy content from PathA to pathB
     *
     * @param site site id where the operation will be executed
     * @param fromPath paths to content
     * @param toPath target path
     * @return Commit ID if successful, empty string otherwise
     */
    String copyContent(String site, String fromPath, String toPath);

    /**
     * get immediate children for path
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @return a list of children
     */
    RepositoryItem[] getContentChildren(String site, String path);

    /**
     * get the version history for an item
     *
     * @param site - the project ID
     * @param path - the path of the item
     * @return a list of versions
     */
    VersionTO[] getContentVersionHistory(String site, String path);

    /**
     * create a version
     *
     * @param site site id where the operation will be executed
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String site, String path, boolean majorVersion);

    /**
     * create a version
     *
     * @param site site id where the operation will be executed
     * @param path location of content
     * @param comment version history comment
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String site, String path, String comment, boolean majorVersion);

    /**
     * revert a version (create a new version based on an old version)
     *
     * @param site site id where the operation will be executed
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     * @return Commit ID if successful, empty string otherwise
     */
    String revertContent(String site, String path, String version, boolean major, String comment);

    /**
     * return a specific version of the content
     *
     * @param site site id where the operation will be executed
     * @param path path of the content
     * @param version version to return
     * @return input stream
     */
    InputStream getContentVersion(String site, String path, String version) throws ContentNotFoundException;

    /**
     * lock an item
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path
     */
    void lockItem(String site, String path); // TODO: SJ: Change to have a return

    /**
     * lock an item
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path
     */
    void lockItemForPublishing(String site, String path); // TODO: SJ: Change to have a return

    /**
     * unlock an item for publishing
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path
     */
    void unLockItem(String site, String path); // TODO: SJ: Change to have a return

    /**
     * unlock an item for publishing
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path
     */
    void unLockItemForPublishing(String site, String path); // TODO: SJ: Change to have a return

    /**
     * Create a new site based on a blueprint
     *
     * @param blueprintName
     * @param siteId
     * @return true if successful, false otherwise
     */
    boolean createSiteFromBlueprint(String blueprintName, String siteId);

    /**
     * Deletes an existing site.
     *
     * @param siteId site to delete
     * @return true if successful, false otherwise
     */
    boolean deleteSite(String siteId);

    /**
     * Initial publish to specified environment.
     *
     * @param site
     * @param environment
     * @param author
     * @param comment
     */
    void initialPublish(String site, String environment, String author, String comment) throws DeploymentException;

    /**
     * Publish content to specified environment.
     *
     * @param deploymentItems
     * @param environment
     * @param author
     * @param comment
     */
    void publish(String site, List<DeploymentItemTO> deploymentItems, String environment, String author, String comment) throws DeploymentException;

    /**
     * Get a list of operations since the commit ID provided (compare that commit to HEAD)
     *
     * @param site site to use
     * @param commitIdFrom commit ID to start at
     * @param commitIdTo commit ID to end at
     * @return commit ID of current HEAD, updated operationsSinceCommit
     */
    List<RepoOperationTO> getOperations(String site, String commitIdFrom, String commitIdTo);

    /**
     * Get last commit id from repository for given site.
     *
     * @param site site id
     * @return last commit id (current HEAD)
     */
    String getRepoLastCommitId(String site);

    /**
     * Get first id from repository for given site
     *
     * @param site site id
     * @return first commit id
     */
    String getRepoFirstCommitId(String site);

    /**
     * Get a list of commits for updates on a content
     * @param site site id
     * @param path path
     * @param commitIdFrom range from commit id (inclusive)
     * @param commitIdTo range to commit id (inclusive)
     * @return list of edit commit ids
     */
    List<String> getEditCommitIds(String site, String path, String commitIdFrom, String commitIdTo);

    /**
     * Check if given commit id exists
     * @param site site id
     * @param commitId commit id to check
     * @return true if it exists in site repository, otherwise false
     */
    boolean commitIdExists(String site, String commitId);

    /**
     * Get git log object from database
     * @param siteId site id
     * @param commitId commit ID
     * @return git log object
     */
    GitLog getGitLog(String siteId, String commitId);

    /**
     * Insert Git Log
     * @param siteId site
     * @param commitId commit ID
     * @param processed processed
     */
    void insertGitLog(String siteId, String commitId, int processed);

    /**
     * Insert Full Git Log
     * @param siteId site
     * @param processed processed
     */
    void insertFullGitLog(String siteId, int processed);

    /**
     * Mark Git log as verified
     * @param siteId site identifier
     * @param commitId commit id
     */
    void markGitLogVerifiedProcessed(String siteId, String commitId);

    /**
     * Delete Git log for site
     * @param siteId site identifier
     */
    void deleteGitLogForSite(String siteId);

    /**
     * Create new site as a clone from remote repository
     *
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteUrl remote repository url
     * @param remoteUsername remote username
     * @param remotePassword remote password
     * @return true if success
     */
    boolean createSiteCloneRemote(String siteId, String remoteName, String remoteUrl, String remoteUsername, String remotePassword) throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException;

    /**
     * Push new site to remote repository
     *
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteUrl remote repository url
     * @param remoteUsername remote username
     * @param remotePassword remote password
     * @return true if success
     */
    boolean createSitePushToRemote(String siteId, String remoteName, String remoteUrl, String remoteUsername, String remotePassword) throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException, RemoteRepositoryNotBareException;

    /**
     * Add remote repository for site content repository
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteUrl remote url
     * @param authenticationType authentication type
     * @param remoteUsername remote username
     * @param remotePassword remote password
     * @param remoteToken remote token
     * @param remotePrivateKey remote private key
     * @return true if operation was successful
     */
    boolean addRemote(String siteId, String remoteName, String remoteUrl, String authenticationType, String remoteUsername, String remotePassword, String remoteToken, String remotePrivateKey) throws InvalidRemoteUrlException, ServiceException;

    /**
     * Remove remote with given name for site
     * @param siteId site identifier
     * @param remoteName remote name
     * @return true if operation was successful
     */
    boolean removeRemote(String siteId, String remoteName);

    /*
    List<PublishTO> getPublishEvents(String site, String commitIdFrom, String commitIdTo);
    List<PublishTO> getPublishEvents(String site, String commitIdFrom);
    List<PublishTO> getPublishEvents(String site, Date from, String to);
    List<PublishTO> getPublishEvents(String site, Date from);
    List<PublishTO> getPublishEvents(String site);

    get tags or similar from now to limit
        get delta from tag to tag

    dump and resync from git
    import site from disk

    */

}