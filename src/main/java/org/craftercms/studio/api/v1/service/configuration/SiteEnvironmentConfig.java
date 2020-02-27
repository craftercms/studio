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
package org.craftercms.studio.api.v1.service.configuration;


import org.craftercms.studio.api.v1.to.EnvironmentConfigTO;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;

import java.util.List;

public interface SiteEnvironmentConfig {

    String PUBLISHING_TARGET_XPATH = "publishing-targets/target";
    String XML_TAG_REPO_BRANCH_NAME = "repo-branch-name";
    String XML_TAG_DISPLAY_LABEL = "display-label";
    String XML_TAG_PREVIEW_ENGINE_SERVER_URL = "preview-engine-server-url";
    String XML_TAG_GRAPHQL_SERVER_URL = "graphql-server-url";

	/**
	 * get the common preview server URL for all content in the given site
	 * 
	 * @param site
	 * @return preview server url
	 */
	String getPreviewServerUrl(String site);

    /**
     * get the common preview engine server URL for all content in the given site
     *
     * @param site
     * @return preview engine server url
     */
    String getPreviewEngineServerUrl(String site);

    /**
     * get the common graphql server URL for all content in the given site
     *
     * @param site
     * @return graphql server url
     */
    String getGraphqlServerUrl(String site);
	
	/**
	 * get the live site url 
	 * 
	 * @param site
	 * @return live site url
	 */
	String getLiveServerUrl(String site);
	
	/**
	 * get admin email address
	 * 
	 * @param site
	 * @return admin email address
	 */
	String getAdminEmailAddress(String site);
	
	/**
	 * get the authoring server URL 
	 * 
	 * @param site
	 * @return authoring server URL
	 */
    String getAuthoringServerUrl(String site);

	/**
	 * get the environment config for the given site
	 * 
	 * @param site
	 * @return environment configuration
	 */
	EnvironmentConfigTO getEnvironmentConfig(String site);

	List<PublishingTargetTO> getPublishingTargetsForSite(String site);
    
    boolean exists(String site);

    String getPreviewDeploymentEndpoint(String site);

    void reloadConfiguration(String site);
}
