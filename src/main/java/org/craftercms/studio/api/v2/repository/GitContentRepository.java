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

package org.craftercms.studio.api.v2.repository;

import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.model.history.ItemVersion;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.core.io.Resource;
import org.springframework.util.function.ThrowingConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

/**
 * Interface for content repositories that support git operations
 */
public interface GitContentRepository extends ContentRepository {
	String PREVIOUS_COMMIT_SUFFIX = "~1";

	/**
	 * Get the site content item list for the given site
	 *
	 * @param site     site id
	 * @param repoType repository type
	 * @param revstr   A git object references expression (e.g.: HEAD, branch name, commit id)
	 * @return list of site content items paths
	 */
	default List<String> getItemPaths(String site, GitRepositories repoType, String revstr) {
		return getSubtreeItems(site, "", repoType, revstr);
	}

	/**
	 * List subtree items for give site and path
	 *
	 * @param site     site identifier
	 * @param path     path for subtree root
	 * @param repoType repository type
	 * @param revstr   A git object references expression (e.g.: HEAD, branch name, commit id)
	 * @return list of item paths contained in the subtree
	 */
	List<String> getSubtreeItems(String site, String path, GitRepositories repoType, String revstr);

	/**
	 * Get a list of operations since the commit ID provided (compare that commit to HEAD)
	 *
	 * @param site         site to use
	 * @param commitIdFrom commit ID to start at
	 * @param commitIdTo   commit ID to end at
	 * @return list of operations
	 */
	List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo) throws ServiceLayerException;

	/**
	 * Get a list of operations between given commit and its first parent
	 *
	 * @param site     site id
	 * @param commitId commit id
	 * @return list of operations
	 */
	default List<RepoOperation> getOperationsFromFirstParentDiff(final String site, final String commitId) throws ServiceLayerException {
		return getOperationsFromDelta(site, commitId + PREVIOUS_COMMIT_SUFFIX, commitId);
	}

	/**
	 * Check if repository exists for  given site
	 *
	 * @param siteId   site id
	 * @param repoType repository type
	 * @return true if repository exists, otherwise false
	 */
	boolean repositoryExists(String siteId, GitRepositories repoType);

	/**
	 * Check if given commit id (or revision string) exists
	 *
	 * @param site     site id
	 * @param repoType repository type
	 * @param commitId commit id or revision to check
	 * @return true if it exists in site repository, otherwise false
	 */
	boolean commitIdExists(String site, GitRepositories repoType, String commitId);

	/**
	 * Remove remote with given name for site
	 *
	 * @param siteId     site identifier
	 * @param remoteName remote name
	 * @return true if operation was successful
	 */
	boolean removeRemote(String siteId, String remoteName);

	/**
	 * Get last commit id from repository for given site.
	 *
	 * @param site site id, or null for global repository
	 * @return last commit id (current HEAD)
	 */
	String getRepoLastCommitId(String site);

	/**
	 * Execute consumers for all site paths for the given site
	 *
	 * @param siteId             the site id
	 * @param directoryProcessor the consumer to process the directory paths
	 * @param fileProcessor      the consumer to process the file paths
	 */
	void forAllSitePaths(String siteId,
						 ThrowingConsumer<String> directoryProcessor,
						 ThrowingConsumer<String> fileProcessor) throws Exception;

	/**
	 * Execute {@link java.util.function.Consumer<String>} for all file paths in the site
	 *
	 * @param siteId        site id
	 * @param fileProcessor the consumer to process the file paths
	 */
	default void forAllFileSitePaths(String siteId, ThrowingConsumer<String> fileProcessor) throws Exception {
		// Ignore directories
		forAllSitePaths(siteId, path -> {
		}, fileProcessor);
	}

	/**
	 * Get the previous commit id from repository for given a site id and a commit id
	 *
	 * @param siteId   site identifier
	 * @param commitId commit Id
	 * @return previous commit id
	 */
	String getPreviousCommitId(String siteId, String commitId);

	/**
	 * return a specific version of the content
	 *
	 * @param site     site id where the operation will be executed
	 * @param path     path of the content
	 * @param commitId version to return
	 * @return the resource if available
	 */
	Optional<Resource> getContentByCommitId(String site, String path, String commitId);

	/**
	 * Check if published repository exists for given site.
	 *
	 * @param siteId site identifier
	 * @return true if PUBLISHED repository exists, otherwise false
	 */
	boolean publishedRepositoryExists(String siteId);

	/*
	 * Get the history of a content item. <br/>
	 * <strong>Note:</strong> the results of this method are not guaranteed when the path does not currently exist in the repository.
	 * @param site site id
	 * @param path path of the content item
	 * @return list of item versions
	 * @throws IOException if there is any error reading the git log or getting diffs between commits
	 * @throws GitAPIException if there is any error while executing git commands
	 */
	List<ItemVersion> getContentItemHistory(String site, String path) throws IOException, GitAPIException, ServiceLayerException;

	/**
	 * Get the commits between two commit ids.
	 * This method must start in the commitTo and go back until it finds commitFrom.
	 * The actual result must be equivalent to <code>git log --first-parent --reverse commitFrom..commitTo</code>
	 *
	 * @param siteId     site id
	 * @param commitFrom the older commit id
	 * @param commitTo   the newer commit id
	 * @return list of commit ids between commitFrom (not included) and commitTo (inclusive)
	 * @throws IOException if there is any error reading the git log
	 */
	List<String> getCommitIdsBetween(String siteId, final String commitFrom, final String commitTo) throws IOException;

	/**
	 * Get the new commits introduced by <code>commitId</code> into <code>baseCommit</code>.<br/>
	 * This method assumes that baseCommit is an ancestor of commitId's first parent.
	 * Result will be equivalent to <code>git log baseCommit..commitId</code>
	 *
	 * @param site       site id
	 * @param baseCommit the commit id to compare against
	 * @param commitId   the commit id to compare
	 */
	List<String> getIntroducedCommits(String site, String baseCommit, String commitId) throws IOException, GitAPIException;

	/**
	 * Validates that all commits in the collection are valid for publishing and return a sorted list.
	 * Valid commits are those returned by "git log --first-parent"
	 *
	 * @param siteId    site id
	 * @param commitIds list of commit ids to validate
	 * @return list of valid commit ids, sorted chronologically, oldest to newest
	 * @throws IOException           if there is any error reading the git log
	 * @throws ServiceLayerException if there is any commit ID that is not valid for publishing
	 */
	SequencedCollection<String> validatePublishCommits(String siteId, Collection<String> commitIds) throws IOException, ServiceLayerException;

	/**
	 * Update the target branch ref to point to the given commit id
	 *
	 * @param siteId    the site id
	 * @param packageId the publish package id
	 * @param commitId  the commit id to update the target branch to
	 * @param target    the target branch to update
	 */
	void updateRef(String siteId, long packageId, String commitId, String target) throws IOException;

	/**
	 * Create a new site based on a blueprint
	 *
	 * @param blueprintLocation blueprint location
	 * @param siteId            site identifier
	 * @param sandboxBranch     sandbox branch name
	 * @param params            site parameters
	 * @param creator           site creator
	 * @return true if successful, false otherwise
	 */
	boolean createSiteFromBlueprint(String blueprintLocation, String siteId, String sandboxBranch,
									Map<String, String> params, String creator);

	/**
	 * Create new site as a clone from remote repository
	 *
	 * @param siteId             site identifier
	 * @param sandboxBranch      sandbox branch name
	 * @param remoteName         remote name
	 * @param remoteUrl          remote repository url
	 * @param remoteBranch       remote branch name
	 * @param singleBranch       flag to signal if clone single branch or full repository
	 * @param authenticationType type of authentication to use to connect remote repository
	 * @param remoteUsername     remote username
	 * @param remotePassword     remote password
	 * @param remoteToken        remote token
	 * @param remotePrivateKey   remote private key
	 * @param params             site parameters
	 * @param createAsOrphan     create as orphan
	 * @param creator            site creator
	 * @return true if success
	 * @throws InvalidRemoteRepositoryException            invalid remote repository
	 * @throws InvalidRemoteRepositoryCredentialsException invalid credentials for remote repository
	 * @throws RemoteRepositoryNotFoundException           remote repository not found
	 * @throws ServiceLayerException                       general service error
	 */
	boolean createSiteCloneRemote(String siteId, String sandboxBranch, String remoteName, String remoteUrl,
								  String remoteBranch, boolean singleBranch, String authenticationType,
								  String remoteUsername, String remotePassword, String remoteToken,
								  String remotePrivateKey, Map<String, String> params, boolean createAsOrphan,
								  String creator)
		throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
		RemoteRepositoryNotFoundException, ServiceLayerException;

	/**
	 * Check if a path is a folder
	 *
	 * @param siteId the site id
	 * @param path   the path
	 * @return true if the path is a folder, false otherwise
	 */
	boolean isFolder(String siteId, String path);

	/**
	 * lock an item
	 * NOTE: site will be removed from this interface
	 *
	 * @param site site id where the operation will be executed
	 * @param path path of the item
	 */
	void lockItem(String site, String path); // TODO: SJ: Change to have a return

	/**
	 * unlock an item
	 *
	 * @param site site id where the operation will be executed
	 * @param path path of the item
	 */
	void unlockItem(String site, String path);

	/**
	 * Deletes the underlying git repositories for a site
	 *
	 * @param siteId the id of the site
	 * @return true if successful, false otherwise
	 */
	boolean deleteSite(String siteId);

	/**
	 * Create copies of the source site's repositories.
	 * This method will copy sandbox and published (if exists) repositories under a new directory
	 * with the new site id.
	 * For sandbox repository, a new branch will be created (from the currently checked-out branch)
	 * with the given sandbox branch name if it does not exist.
	 *
	 * @param sourceSiteId  source site id
	 * @param siteId        new site id
	 * @param sandboxBranch sandbox branch name
	 * @throws IOException if there is any error while copying the directories
	 */
	void duplicateSite(String sourceSiteId, String siteId, String sourceSandboxBranch, String sandboxBranch) throws IOException, ServiceLayerException;

	/**
	 * Get a content Item
	 *
	 * @param siteId  site id where the operation will be executed
	 * @param path    path to content
	 * @param flatten if true, return a flattened version of the item
	 * @return item
	 */
	// TODO: Move this method to ContentService
	Item getItem(String siteId, String path, boolean flatten);

	/**
	 * Check if content has been published to the target
	 *
	 * @param siteId the site id
	 * @param target the publishing target
	 * @return true if the target has been published, false otherwise
	 */
	boolean isTargetPublished(String siteId, String target) throws IOException;

	/**
	 * Delete a list of items from the site repository
	 *
	 * @param siteId   site id
	 * @param paths    list of paths to delete
	 * @param approver the user that approved the delete operation
	 * @return the commit id of the delete operation
	 * @throws ServiceLayerException if there is any error while deleting the items
	 */
	String deleteContent(String siteId, Collection<String> paths, String approver) throws ServiceLayerException;

	/**
	 * Create empty file such as .keep to git repository and commit
	 *
	 * @param siteId site id
	 * @param paths  list of paths to create and commit to git
	 */
	void createEmptyFiles(String siteId, Collection<String> paths);

	/**
	 * Performs a garbage collect all repositories for the given site
	 *
	 * @param siteId site identifier
	 */
	void garbageCollectGitRepositories(String siteId);

	/**
	 * Get the children of a repository folder
	 *
	 * @param site       site id
	 * @param folderPath path to the folder
	 * @return list of children
	 */
	Collection<RepositoryItem> getContentChildren(String site, String folderPath) throws ServiceLayerException;

	/**
	 * Revert content to a previous version
	 *
	 * @param site    site id
	 * @param path    path of the content
	 * @param version version to revert to
	 * @param comment comment for the revert operation
	 * @return commit id of the new version
	 * @throws UserNotFoundException if the current user is not found
	 * @throws ServiceLayerException if there is any error while reverting the content
	 */
	String revertContent(String site, String path, String version, String comment) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Write a content item into the repository
	 *
	 * @param site    the site id
	 * @param path    the path to write the content
	 * @param content the content to write
	 * @return commit id after the operation
	 * @throws ServiceLayerException if the operation fails
	 * @throws UserNotFoundException if the current user is not found
	 */
	String writeContent(String site, String path, InputStream content) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Create a folder in the repository
	 *
	 * @param site the site id
	 * @param path the path to create the folder
	 * @param name the name of the folder
	 * @return commit id after the operation
	 * @throws ServiceLayerException if the operation fails
	 * @throws UserNotFoundException if the current user is not found
	 */
	String createFolder(String site, String path, String name) throws ServiceLayerException, UserNotFoundException;

	/**
	 * Write a collection of content items into the repository
	 *
	 * @param siteId     the site id
	 * @param writeItems the collection of ContentWriteItem to write
	 * @param folders    collection of folders to create
	 * @return commit id after the operation
	 * @throws ServiceLayerException if the operation fails
	 * @throws UserNotFoundException if the current user is not found
	 */
	String writeContent(String siteId, Collection<? extends ContentWriteItem> writeItems, Set<String> folders)
		throws ServiceLayerException, UserNotFoundException;

	/**
	 * Move content (files or directories) from one path to another
	 *
	 * @param site     the site id
	 * @param fromPath the path to move the content from
	 * @param toPath   the path to move the content to
	 * @return commit id after the operation
	 * @throws ServiceLayerException if the operation fails
	 */
	default String moveContent(String site, String fromPath, String toPath) throws ServiceLayerException, UserNotFoundException {
		return moveContent(site, fromPath, toPath, emptyList(), emptySet());
	}

	/**
	 * Move content (files or directories) from one path to another
	 * It also accepts a collection of additional items to be written and added to the same commit
	 *
	 * @param site            the site id
	 * @param fromPath        the path to move the content from
	 * @param toPath          the path to move the content to
	 * @param additionalItems collection of additional items to be written in the same commit
	 * @param newFolders      collection of folders to create
	 * @return commit id after the operation
	 * @throws ServiceLayerException if the operation fails
	 */
	String moveContent(String site, String fromPath, String toPath, Collection<? extends ContentWriteItem> additionalItems, Set<String> newFolders)
		throws ServiceLayerException, UserNotFoundException;

}
