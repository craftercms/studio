/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.service.api;

import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.DeploymentConfigTO;
import org.craftercms.cstudio.alfresco.to.PublishingChannelGroupConfigTO;
import org.craftercms.cstudio.alfresco.to.SiteTO;

import java.util.Map;
import java.util.Set;

/**
 * Provides site-related data and configuration.
 * 
 * @author hyanghee
 *
 */
public interface SiteService {
	
	String SITE_MANAGER_GROUP_NAME = "site_%s_SiteManager";

	/**
	 * get the common preview server URL for all content in the given site
	 * 
	 * @param site
	 * @return preview server url
	 */
	public String getPreviewServerUrl(String site);
	
	/**
	 * get the common live server URL for all content in the given site
	 * 
	 * @param site
	 * @return live server url
	 */
	public String getLiveServerUrl(String site);
	
	public String getAdminEmailAddress(String site);
	
	/**
	 * get the authoring server URL
	 * 
	 * @param site
	 * @return authoring server URL
	 */
	public String getAuthoringServerUrl(String site);
	
	/**
	 * get the form server URL
	 * 
	 * @param site
	 * 
	 * @return form server URL
	 */
	public String getFormServerUrl(String site);
	
	/**
	 * create a preview URL for a given asset
	 * 
	 * @param site
	 *            the site id
	 * @param assetId
	 *            the asset id (path)
	 * @param storeId
	 *            the store ID
	 * @param versionId
	 *            the verion id
	 * @param geoId
	 *            the Geo Id
	 * @return a URL for the asset
	 * @throws ServiceException
	 */
	public String createPreviewUrl(String site, String assetId, String storeId, int versionId, String geoId)
			throws ServiceException;

	/**
	 * get a list of sites menu items
	 * 
	 * @return sites menu items
	 */
	public Map<String, String> getSitesMenuItems();
	
	/**
	 * get the collaborative sandbox of the given site 
	 * 
	 * @return
	 */
	public String getCollabSandbox(String site);

	/**
	 * get configuraiton content as XML string at the given path 
	 * 
	 * @param site
	 * @param path
	 * @param applyEnv
	 * 			find from the environment overrides location?
	 * @return configuration as XML string
	 */
	public String getConfiguration(String site, String path, boolean applyEnv);

	/**
	 * get the site information obj 
	 * 
	 * @param key
	 * 			siteId, authoringUrl, previewUrl
	 * @param value
	 * @return
	 */
	public SiteTO getSite(String key, String value);

    /**
     * Reload site configurations
     */
    public void reloadSiteConfigurations();

    public Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs(String site);

    public void deleteSite(String site);

	public void createObjectStatesforNewSite(NodeRef siteRoot);

    public void extractDependenciesForNewSite(NodeRef siteRoot);

    public void extractMetadataForNewSite(NodeRef siteRoot);

    public DeploymentEndpointConfigTO getDeploymentEndpoint(String site, String endpoint);

    public Set<String> getAllAvailableSites();

    String getLiveEnvironmentName(String site);

    public void addConfigSpaceExportAspect(String site);

    public DeploymentEndpointConfigTO getPreviewDeploymentEndpoint(String site);
}
