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
package org.craftercms.studio.api.v1.service.site;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.craftercms.studio.api.v1.to.PublishingChannelGroupConfigTO;
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
    boolean writeConfiguration(String site, String path, InputStream content);

	/**
	 * write configuraiton content at the given path
	 * (can be any kind of content)
	 * @param path
	 */
	boolean writeConfiguration(String path, InputStream content);
	
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
	 * @param site
	 * @param path
	 * @param applyEnv
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

	List<SiteFeed> getUserSites(String user);

    DeploymentEndpointConfigTO getDeploymentEndpoint(String site, String endpoint);

    Map<String, PublishingChannelGroupConfigTO> getPublishingChannelGroupConfigs(String site);

    DeploymentEndpointConfigTO getPreviewDeploymentEndpoint(String site);

    Set<String> getAllAvailableSites();

    String getLiveEnvironmentName(String site);

    /**
     * Create a new site based on an existing blueprint
     * @param blueprintName
     * @param siteName
     * @param siteId
     * @param desc
     */
    boolean createSiteFromBlueprint(String blueprintName, String siteName, String siteId, String desc);

    /**
     * remove a site from the system
     */
   	boolean deleteSite(String siteId);

   	/**
   	 * get a list of available blueprints
   	 */
   	SiteBlueprintTO[] getAvailableBlueprints();

    String getPreviewServerUrl(String site);

    String getLiveServerUrl(String site);

    String getAdminEmailAddress(String site);

    void reloadSiteConfigurations();

    void reloadSiteConfiguration(String site);

    void reloadSiteConfiguration(String site, boolean triggerEvent);

    void reloadGlobalConfiguration();
}
