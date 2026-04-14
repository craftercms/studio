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

package org.craftercms.studio.api.v2.service.repository;

import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteNotRemovableException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v2.dal.DiffConflictedFile;
import org.craftercms.studio.api.v2.dal.repository.RemoteRepository;
import org.craftercms.studio.api.v2.dal.repository.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.dal.repository.RepositoryStatus;

import java.util.List;

/**
 * Service to manage repositories.
 */
public interface RepositoryManagementService {

	//TODO JM: revisit this and add the proper exceptions instead of returning boolean

	/**
	 * Add a remote repository
	 *
	 * @param siteId           the site id
	 * @param remoteRepository the remote repository
	 * @throws ServiceLayerException     if there is any error adding the remote
	 * @throws InvalidRemoteUrlException if the remote url is invalid
	 */
	void addRemote(String siteId, RemoteRepository remoteRepository)
		throws ServiceLayerException, InvalidRemoteUrlException;

	/**
	 * List the remote repositories for a site
	 *
	 * @param siteId the site id
	 * @return the list of remote repositories
	 * @throws ServiceLayerException if there is any error listing the remotes
	 */
	List<RemoteRepositoryInfo> listRemotes(String siteId) throws ServiceLayerException;

	/**
	 * Pull from a remote repository
	 *
	 * @param siteId        the site id
	 * @param remoteName    the remote name
	 * @param remoteBranch  the remote branch
	 * @param mergeStrategy the merge strategy (theirs|ours|none)
	 * @return the merge result
	 * @throws InvalidRemoteUrlException                   if the remote url is invalid
	 * @throws ServiceLayerException                       if there is any error pulling from the remote
	 * @throws InvalidRemoteRepositoryCredentialsException if the remote repository credentials are invalid
	 * @throws RemoteRepositoryNotFoundException           if the remote repository is not found
	 */
	MergeResult pullFromRemote(String siteId, String remoteName, String remoteBranch, String mergeStrategy)
		throws InvalidRemoteUrlException, ServiceLayerException,
		InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException;

	/**
	 * Push to a remote repository
	 *
	 * @param siteId       the site id
	 * @param remoteName   the remote name
	 * @param remoteBranch the remote branch
	 * @param force        if true, force the push
	 * @return true if the push was successful
	 * @throws InvalidRemoteUrlException                   if the remote url is invalid
	 * @throws ServiceLayerException                       if there is any error pushing to the remote
	 * @throws InvalidRemoteRepositoryCredentialsException if the remote repository credentials are invalid
	 * @throws RemoteRepositoryNotFoundException           if the remote repository is not found
	 */
	boolean pushToRemote(String siteId, String remoteName, String remoteBranch, boolean force)
		throws InvalidRemoteUrlException, ServiceLayerException,
		InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException;

	/**
	 * Remove a remote repository
	 *
	 * @param siteId     the site id
	 * @param remoteName the remote name
	 * @return true if the remote was removed
	 * @throws SiteNotFoundException       if the site is not found
	 * @throws RemoteNotRemovableException if the remote cannot be removed
	 */
	boolean removeRemote(String siteId, String remoteName)
		throws SiteNotFoundException, RemoteNotRemovableException;

	/**
	 * Get the sandbox repository status
	 *
	 * @param siteId the site id
	 * @return the repository status
	 * @throws ServiceLayerException if there is any error getting the repository status
	 */
	RepositoryStatus getRepositoryStatus(String siteId) throws ServiceLayerException;

	/**
	 * Resolve a conflict
	 *
	 * @param siteId     the site id
	 * @param path       the path of the conflicted file
	 * @param resolution the {@link ConflictResolution} resolution
	 * @return the repository status after the operation
	 * @throws ServiceLayerException if there is any error resolving the conflict
	 */
	RepositoryStatus resolveConflict(String siteId, String path, ConflictResolution resolution)
		throws ServiceLayerException;

	/**
	 * Get the diff for a conflicted file
	 *
	 * @param siteId the site id
	 * @param path   the path of the conflicted file
	 * @return the diff for the conflicted file
	 * @throws ServiceLayerException if there is any error getting the diff
	 */
	DiffConflictedFile getDiffForConflictedFile(String siteId, String path)
		throws ServiceLayerException;

	/**
	 * Commit the resolution of a conflict
	 *
	 * @param siteId        the site id
	 * @param commitMessage the commit message
	 * @return the repository status after the operation
	 * @throws ServiceLayerException if there is any error committing the resolution
	 */
	RepositoryStatus commitResolution(String siteId, String commitMessage)
		throws ServiceLayerException;

	/**
	 * Cancel a failed pull operation
	 *
	 * @param siteId the site id
	 * @return the repository status after the operation
	 * @throws ServiceLayerException if there is any error canceling the failed pull
	 */
	RepositoryStatus cancelFailedPull(String siteId) throws ServiceLayerException;

	/**
	 * Unlock local git repository
	 *
	 * @param siteId         site identifier, if null or empty it is global repository
	 * @param repositoryType repository type (GLOBAL, SANDBOX, PUBLISHED)
	 * @throws SiteNotFoundException if the site is not found
	 * @throws ServiceLayerException if there is any error unlocking the repository
	 */
	void unlockRepository(String siteId, GitRepositories repositoryType) throws ServiceLayerException;

	/**
	 * Checks if a given Git repository is corrupted
	 *
	 * @param siteId         the id of the site
	 * @param repositoryType the type of the repository
	 * @return true if the repo is corrupted
	 * @throws ServiceLayerException if there is any error checking the repository
	 */
	boolean isCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException;

	/**
	 * Repairs a corrupted Git repository
	 *
	 * @param siteId         the id of the site
	 * @param repositoryType the type of the repository
	 * @throws ServiceLayerException if there is any error repairing the repository
	 */
	void repairCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException;

}
