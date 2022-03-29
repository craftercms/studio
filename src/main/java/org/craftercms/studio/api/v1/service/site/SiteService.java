/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.service.site;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.BlueprintNotFoundException;
import org.craftercms.studio.api.v1.exception.DeployerTargetException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteCreationException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.exception.MissingPluginParameterException;
import org.dom4j.Document;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.InputStream;

import org.craftercms.studio.api.v1.to.SiteBlueprintTO;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.
 * This service provides access to site configuration
 * @author russdanner
 */
public interface SiteService {

	/**
	 * write configuraiton content at the given path
	 * (can be any kind of content)
	 * @param path
	 */
    boolean writeConfiguration(String site, String path, InputStream content) throws ServiceLayerException;

	/**
	 * write configuraiton content at the given path
	 * (can be any kind of content)
	 * @param path
	 */
	boolean writeConfiguration(String path, InputStream content) throws ServiceLayerException;

	/**
	 * given a site ID return the configuration as a document
	 * This method allows extensions to add additional properties to the configuration that
	 * are not made available through the site configuration object
	 * @param site the name of the site
	 * @return a Document containing the entire site configuration
	 */
	Document getSiteConfiguration(String site) throws SiteConfigNotFoundException;

	/**
	 * get configuraiton content as XML string at the given path
	 *
	 * @param path
	 * 			find from the environment overrides location?
	 * @return configuration as XML string
	 */
	Map<String, Object> getConfiguration(String path);

	/**
	 * get configuraiton content as XML string at the given path
	 *
	 * @param site
	 * @param path
	 * @param applyEnv
	 * 			find from the environment overrides location?
	 * @return configuration as XML string
	 */
	Map<String, Object> getConfiguration(String site, String path, boolean applyEnv);

    Set<String> getAllAvailableSites();

    int countSites();

    /**
     * Create a new site based on an existing blueprint
     * @param blueprintName blueprint name to create site
     * @param siteName site name
     * @param siteId site identifier
     * @param desc description
     * @param params site parameters
     * @param createAsOrphan create the site from a remote repository as orphan (no git history)
     */
    void createSiteFromBlueprint(String blueprintName, String siteName, String siteId, String sandboxBranch,
                                 String desc, Map<String, String> params, boolean createAsOrphan)
            throws SiteAlreadyExistsException, SiteCreationException, DeployerTargetException,
			BlueprintNotFoundException, MissingPluginParameterException;

    /**
     * Create a new site with remote option (clone from remote or push to remote repository)
     *
     * @param siteId site identifier
     * @param sandboxBranch sandbox branch name
     * @param description description
     * @param blueprintName name of the blueprint to create site
     * @param remoteName remote repository name
     * @param remoteUrl remote repository url
     * @param remoteBranch remote repository branch to create site from
     * @param singleBranch clone single branch if true, otherwise clone whole repo
     * @param authenticationType remote repository authentication type
     * @param remoteUsername remote repository username to use for authentication
     * @param remotePassword remote repository username to use for authentication
     * @param remoteToken remote repository username to use for authentication
     * @param remotePrivateKey remote repository username to use for authentication
     * @param createOption remote repository username to use for authentication
     * @param params site parameters
     * @param createAsOrphan create the site from a remote repository as orphan (no git history)
     */
    void createSiteWithRemoteOption(String siteId, String sandboxBranch, String description, String blueprintName,
                                    String remoteName, String remoteUrl, String remoteBranch, boolean singleBranch,
                                    String authenticationType, String remoteUsername, String remotePassword,
                                    String remoteToken, String remotePrivateKey, String createOption,
                                    Map<String, String> params, boolean createAsOrphan)
            throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, RemoteRepositoryNotBareException, InvalidRemoteUrlException;

    /**
     * remove a site from the system
     */
   	boolean deleteSite(String siteId);

    @RetryingOperation
    void updateLastVerifiedGitlogCommitId(String site, String commitId);

    /**
	 * Update last audited gitlog commit id
	 * @param site site identifier
	 * @param commitId commit ID
	 */
	void updateLastSyncedGitlogCommitId(String site, String commitId);

    /**
	 * Synchronize our internal database with the underlying repository. This is required when a user bypasses the UI
	 * and manipulates the underlying repository directly.
	 *
	 * @param siteId site to sync
	 * @param fromCommitId commit ID to start at and sync up until current commit
	 * @return true if successful, false otherwise
	 */
	boolean syncDatabaseWithRepo(String siteId, String fromCommitId) throws SiteNotFoundException;

    /**
     * Synchronize our internal database with the underlying repository. This is required when a user bypasses the UI
     * and manipulates the underlying repository directly.
     *
     * @param siteId site to sync
     * @param fromCommitId commit ID to start at and sync up until current commit
     * @param generateAuditLog if true add operations to audit log
     * @return true if successful, false otherwise
     */
    boolean syncDatabaseWithRepo(String siteId, String fromCommitId, boolean generateAuditLog)
            throws SiteNotFoundException;

   	/**
   	 * get a list of available blueprints
   	 */
   	SiteBlueprintTO[] getAvailableBlueprints();

    void reloadSiteConfigurations();

    void reloadSiteConfiguration(String site);

    void reloadSiteConfiguration(String site, boolean triggerEvent);

    void reloadGlobalConfiguration();

    /**
     * Synchronize Database with repository
     *
     * @param site site id
     */
    void syncRepository(String site) throws SiteNotFoundException;

    /**
     * Rebuild database for site
     *
     * @param site site id
     */
    void rebuildDatabase(String site);

    void updateLastCommitId(String site, String commitId);

    /**
     * Check if site already exists
     *
     * @param site site ID
     * @return true if site exists, false otherwise
     */
    boolean exists(String site);

    /**
     * Check if site already exists
     *
     * @param id site ID in DB
     * @return true if site exists, false otherwise
     */
    boolean existsById(String id);

    /**
     * Check if site already exists
     *
     * @param name site name in DB
     * @return true if site exists, false otherwise
     */
    boolean existsByName(String name);

	/**
	 * Get total number of sites that user is allowed access to for current user
	 *
	 * @return number of sites
	 * @throws UserNotFoundException
	 */
	int getSitesPerUserTotal() throws UserNotFoundException, ServiceLayerException;

    /**
     * Get total number of sites that user is allowed access to for given username
     *
     * @param username username
     * @return number of sites
     * @throws UserNotFoundException
     */
    int getSitesPerUserTotal(String username) throws UserNotFoundException, ServiceLayerException;

	/**
	 * Get sites that user is allowed access to for current user
	 *
	 * @param start start position for pagination
	 * @param number number of sites per page
	 * @return number of sites
	 * @throws UserNotFoundException
	 */
	List<SiteFeed> getSitesPerUser(int start, int number) throws UserNotFoundException,
			ServiceLayerException;

    /**
     * Get sites that user is allowed access to for given username
     *
     * @param username username
     * @param start start position for pagination
     * @param number number of sites per page
     * @return number of sites
     * @throws UserNotFoundException
     */
    List<SiteFeed> getSitesPerUser(String username, int start, int number) throws UserNotFoundException,
		ServiceLayerException;

    /**
     * Get site details
     * @param siteId site id
     * @return site details
     */
    SiteFeed getSite(String siteId) throws SiteNotFoundException;

    /**
     * Check if publishing is enabled for given site
     * @param siteId site id
     * @return true if publishing is enabled for given site, otherwise false
     */
    boolean isPublishingEnabled(String siteId);

    /**
     * Enable/Disable publishing for given site
     * @param siteId site id
     * @param enabled true to enable publishing, false to disable publishing
     */
    boolean enablePublishing(String siteId, boolean enabled) throws SiteNotFoundException;

    /**
     * Update publishing status message for given site
     * @param siteId site id
	 * @param status status
     * @param message new publishing status message
     * @return true if publishing status message is successfully updated
     * @throws SiteNotFoundException
     */
    boolean updatePublishingStatusMessage(String siteId, String status, String message) throws SiteNotFoundException;

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
    boolean addRemote(String siteId, String remoteName, String remoteUrl,
                      String authenticationType, String remoteUsername, String remotePassword, String remoteToken,
                      String remotePrivateKey)
            throws InvalidRemoteUrlException, ServiceLayerException;

    /**
     * Remove remote with given name for site
     * @param siteId site identifier
     * @param remoteName remote name
     * @return true if operation was successful
     */
    boolean removeRemote(String siteId, String remoteName) throws SiteNotFoundException;

    /**
     * List remote repositories for given site
     *
     * @param siteId site identifier
     * @return list of names of remote repositories
     * @throws SiteNotFoundException
     */
    List<RemoteRepositoryInfoTO> listRemote(String siteId) throws ServiceLayerException, CryptoException;

    /**
     * Get deleted sites
     *
     * @return List of deleted sites from DB
     */
    List<SiteFeed> getDeletedSites();

	/**
	 * Lock publishing for site
	 * @param siteId site identifier
	 * @param lockOwnerId lock owner identifier
	 * @param ttl TTL for lock
	 * @return true if locking was successful
	 */
	boolean tryLockPublishingForSite(String siteId, String lockOwnerId, int ttl);

    /**
	 * Unlock publishing for site
	 * @param siteId site identifier
	 * @return true if unlocking was successful
	 */
    boolean unlockPublishingForSite(String siteId, String lockOwnerId);

    /**
	 * update publishing lock heartbeat for site
	 * @param siteId site identifier
	 */
	void updatePublishingLockHeartbeatForSite(String siteId);

	/**
	 * get last commit id for site
	 * @param siteId site identifier
	 * @return last commit id for local studio node
	 */
	String getLastCommitId(String siteId);

	String getSiteState(String siteId);

	/**
	 * get last verified git log commit id for site
	 * @param siteId site identifier
	 * @return last verified git log commit id for local studio node
	 */
	String getLastVerifiedGitlogCommitId(String siteId);

	/**
	 * Get list of all sites with state = CREATED
	 * @return list of sites
	 */
	List<String> getAllCreatedSites();

	void setSiteState(String siteId, String state);

	boolean isPublishedRepoCreated(String siteId);

    void setPublishedRepoCreated(String siteId);

	/**
	 * get last audited git log commit id for site
	 * @param siteId site identifier
	 * @return last audited git log commit id for local studio node
	 */
	String getLastSyncedGitlogCommitId(String siteId);
}
