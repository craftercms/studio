/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.repository;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.VersionTO;

import java.util.List;

public interface GitContentRepository extends ContentRepository{

    /**
     * get immediate children for path
     *
     * @param site site id where the operation will be executed
     * @param path path to content
     * @return array of children
     */
    RepositoryItem[] getContentChildren(String site, String path);

    /**
     * get the version history for an item
     *
     * @param site - the project ID
     * @param path - the path of the item
     * @return array of versions
     */
    VersionTO[] getContentVersionHistory(String site, String path);

    /**
     * create a version
     *
     * @param site         site id where the operation will be executed
     * @param path         location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String site, String path, boolean majorVersion);

    /**
     * create a version
     *
     * @param site         site id where the operation will be executed
     * @param path         location of content
     * @param comment      version history comment
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    String createVersion(String site, String path, String comment, boolean majorVersion);

    /**
     * revert a version (create a new version based on an old version)
     *
     * @param site    site id where the operation will be executed
     * @param path    - the path of the item to "revert"
     * @param version - old version ID to base to version on
     * @param major flag if it is major version
     * @param comment add comment when committing content
     * @return Commit ID if successful, empty string otherwise
     */
    String revertContent(String site, String path, String version, boolean major, String comment);

    /**
     * lock an item
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path path of the item
     */
    void lockItemForPublishing(String site, String path); // TODO: SJ: Change to have a return

    /**
     * unlock an item for publishing
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path path of the item
     */
    void unLockItem(String site, String path); // TODO: SJ: Change to have a return

    /**
     * unlock an item for publishing
     * NOTE: site will be removed from this interface
     *
     * @param site site id where the operation will be executed
     * @param path path of the item
     */
    void unLockItemForPublishing(String site, String path); // TODO: SJ: Change to have a return

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
     * Add remote repository for site content repository
     *
     * @param siteId             site identifier
     * @param remoteName         remote name
     * @param remoteUrl          remote url
     * @param authenticationType authentication type
     * @param remoteUsername     remote username
     * @param remotePassword     remote password
     * @param remoteToken        remote token
     * @param remotePrivateKey   remote private key
     * @return true if operation was successful
     *
     * @throws InvalidRemoteUrlException invalid url for remote repository
     * @throws ServiceLayerException general service error
     */
    boolean addRemote(String siteId, String remoteName, String remoteUrl,
                      String authenticationType, String remoteUsername, String remotePassword, String remoteToken,
                      String remotePrivateKey)
            throws InvalidRemoteUrlException, ServiceLayerException;

    /**
     * Remove all remotes for given site
     *
     * @param siteId site identifier
     */
    void removeRemoteRepositoriesForSite(String siteId);

    /**
     * List remote repositories for given site
     *
     * @param siteId site identifier
     * @param sandboxBranch sandbox branch name
     * @return list of names of remote repositories
     *
     * @throws ServiceLayerException general service error
     * @throws CryptoException git repository helper error
     */
    List<RemoteRepositoryInfoTO> listRemote(String siteId, String sandboxBranch)
            throws ServiceLayerException, CryptoException;

    /**
     * Push content to remote repository
     *
     * @param siteId       site identifier
     * @param remoteName   remote name
     * @param remoteBranch remote branch
     * @return true if operation was successful
     *
     * @throws ServiceLayerException general service error
     * @throws InvalidRemoteUrlException invalid url for remote repository
     * @throws CryptoException git repository helper error
     */
    boolean pushToRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException, CryptoException;

    /**
     * Pull from remote repository
     *
     * @param siteId       site identifier
     * @param remoteName   remote name
     * @param remoteBranch remote branch
     * @return true if operation was successful
     *
     * @throws ServiceLayerException general service error
     * @throws InvalidRemoteUrlException invalid url for remote repository
     * @throws CryptoException git repository helper error
     */
    boolean pullFromRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException, CryptoException;

    /**
     * Check if content at given path is folder
     *
     * @param siteId site identifier
     * @param path   content path
     * @return true if path is folder, otherwise false
     */
    boolean isFolder(String siteId, String path);

    /**
     * Reset staging repository to live for given site
     *
     * @param siteId site identifier to use for resetting
     *
     * @throws ServiceLayerException general service error
     */
    void resetStagingRepository(String siteId) throws ServiceLayerException;

    /**
     * Performs a cleanup all repositories for the given site
     * @param siteId site identifier
     */
    void cleanupRepositories(String siteId);

    /**
     * Deletes an existing site.
     *
     * @param siteId site to delete
     * @return true if successful, false otherwise
     * @deprecated use {@link org.craftercms.studio.api.v2.repository.GitContentRepository#deleteSite(String)} instead
     */
    @Deprecated
    boolean deleteSite(String siteId);
}
