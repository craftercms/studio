/*
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
 *
 */

package org.craftercms.studio.api.v1.service.site;

import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.BlueprintNotFoundException;
import org.craftercms.studio.api.v1.exception.PreviewDeployerUnreachableException;
import org.craftercms.studio.api.v1.exception.SearchUnreachableException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteCreationException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.PublishStatus;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
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
    boolean writeConfiguration(String site, String path, InputStream content) throws ServiceException;

	/**
	 * write configuraiton content at the given path
	 * (can be any kind of content)
	 * @param path
	 */
	boolean writeConfiguration(String path, InputStream content) throws ServiceException;

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

    List<PublishingTargetTO> getPublishingTargetsForSite(String site);

    Set<String> getAllAvailableSites();

    /**
     * Create a new site based on an existing blueprint
     * @param blueprintName
     * @param siteName
     * @param siteId
     * @param desc
     */
    void createSiteFromBlueprint(String blueprintName, String siteName, String siteId, String sandboxBranch,
                                 String desc)
            throws SiteAlreadyExistsException, SiteCreationException, PreviewDeployerUnreachableException,
            SearchUnreachableException, BlueprintNotFoundException;

    /**
     * Create a new site with remote option (clone from remote or push to remote repository)
     *
     * @param siteId
     * @param description
     * @param blueprintName
     * @param remoteName
     * @param remoteUrl
     * @param remoteUsername
     * @param remotePassword
     * @param createOption
     */
    void createSiteWithRemoteOption(String siteId, String sandboxBranch, String description, String blueprintName,
                                    String remoteName, String remoteUrl, String remoteBranch, boolean singleBranch,
                                    String authenticationType, String remoteUsername, String remotePassword,
                                    String remoteToken, String remotePrivateKey, String createOption)
            throws ServiceException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, RemoteRepositoryNotBareException, InvalidRemoteUrlException;

    /**
     * remove a site from the system
     */
   	boolean deleteSite(String siteId);

	/**
	 * Synchronize our internal database with the underlying repository. This is required when a user bypasses the UI
	 * and manipulates the underlying repository directly.
	 *
	 * @param siteId site to sync
	 * @param fromCommitId commit ID to start at and sync up until current commit
	 * @return true if successful, false otherwise
	 */
	boolean syncDatabaseWithRepo(String siteId, String fromCommitId);

   	/**
   	 * get a list of available blueprints
   	 */
   	SiteBlueprintTO[] getAvailableBlueprints();

    String getPreviewServerUrl(String site);

    String getLiveServerUrl(String site);

    String getAuthoringServerUrl(String site);

    String getAdminEmailAddress(String site);

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
     * Get total number of sites that user is allowed access to for given username
     *
     * @param username username
     * @return number of sites
     * @throws UserNotFoundException
     */
    int getSitesPerUserTotal(String username) throws UserNotFoundException;

    /**
     * Get sites that user is allowed access to for given username
     *
     * @param username username
     * @param start start position for pagination
     * @param number number of sites per page
     * @return number of sites
     * @throws UserNotFoundException
     */
    List<SiteFeed> getSitesPerUser(String username, int start, int number) throws UserNotFoundException;

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
     * @param message new publishing status message
     * @return true if publishing status message is successfully updated
     * @throws SiteNotFoundException
     */
    boolean updatePublishingStatusMessage(String siteId, String message) throws SiteNotFoundException;

    /**
     * Get publish status for given site
     * @param site site id
     * @return publish status
     */
    PublishStatus getPublishStatus(String site) throws SiteNotFoundException;

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
            throws InvalidRemoteUrlException, ServiceException;

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
    List<RemoteRepositoryInfoTO> listRemote(String siteId) throws ServiceException;
}
