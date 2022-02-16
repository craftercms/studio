/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.CopyDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DmFolderConfigTO;
import org.craftercms.studio.api.v1.to.FacetTO;

import java.util.List;
import java.util.Map;

/**
 * This class provides the repository configuration information
 * 
 * @author hyanghee
 * 
 */
public interface ServicesConfig {

	/**
	 * get the root prefix of site. the root prefix represents the folder name
	 * pattern of the corporate and the geo site file locations e.g. if
	 * corporate files are under /site and the geo site files are under
	 * /site_geo then the root prefix should be "/site"
	 * 
	 * @param site
	 * @return root prefix
	 */
	String getRootPrefix(final String site);
	
	/**
	 * get the name of the web project for the given site
	 * 
	 * @param site
	 * @return web project name
	 */
	String getWemProject(final String site);

    /**
     * get a list of folder configuration. The top folders are used to as the
     * top categories when services return a collection of items such as
     * get-go-live-items call
     *
     * @param site
     * @return a list of folder configuration
     */
    List<DmFolderConfigTO> getFolders(final String site);

	/**
	 * get DM content type configuration by the given site and name
	 * @param site
	 * @param name
	 * @return content type
	 */
	ContentTypeConfigTO getContentTypeConfig(String site, String name);
	
	/**
	 * get component item URI patterns
	 * 
	 * @return component item URI patterns
	 */
	List<String> getComponentPatterns(String site);

	/**
	 * get asset item URI patterns
	 * 
	 * @return asset item URI patterns
	 */
	List<String> getAssetPatterns(String site);
	
	/**
	 * get page item URI patterns
	 * 
	 * @return page item URI patterns
	 */
	List<String> getPagePatterns(String site);

	/**
	 * get document item URI patterns
	 * 
	 * @return document item URI patterns
	 */
	List<String> getDocumentPatterns(String site);

    /**
     * get rendering template item URI patterns
     *
     * @return rendering template item URI patterns
     */
    List<String> getRenderingTemplatePatterns(String site);

    /**
     * get scripts item URI patterns
     *
     * @return scripts item URI patterns
     */
    List<String> getScriptsPatterns(String site);

    /**
     * get level descriptor item URI patterns
     *
     * @return level descriptor item URI patterns
     */
    List<String> getLevelDescriptorPatterns(String site);

	/**
	 * get the name of level descriptor 
	 * 
	 * @param site
	 * @return level descriptor name
	 */
	String getLevelDescriptorName(String site);

	
	/**
	 * get the delete dependencies related to a content type
	 * 
	 * @param site
	 * @return delete dependencys patterns
	 */
	List<DeleteDependencyConfigTO> getDeleteDependencyPatterns(String site, String contentType);
	
	/**
	 * get the copy dependencies pattern for a content type
	 * 
	 * @param site
	 * @param contentType
	 * @return copy dependencies patterns
	 */
	List<CopyDependencyConfigTO> getCopyDependencyPatterns(String site, String contentType);

	/**
	 * get a list of paths to display in widgets
	 * 
	 * @param site
	 * @return a list of paths to display in widgets
	 */
	List<String> getDisplayInWidgetPathPatterns(String site);

	/**
	 * get the default timezone value
	 * @param site
	 * @return default timezone
	 */
	String getDefaultTimezone(String site);
    
    List<String> getPreviewableMimetypesPaterns(String site);

	/**
	 * Get the pattern for the plugin folder in the given site
	 */
	String getPluginFolderPattern(String site);

    /**
     * Get sandbox branch name for given site
     *
     * @param site
     * @return Sandbox branch name
     */
    String getSandboxBranchName(String site);

    String getStagingEnvironment(String site);

    String getLiveEnvironment(String site);

    boolean isStagingEnvironmentEnabled(String site);

	/**
	 * Returns the search field configuration for the given site
	 * @param site the site
	 * @return the search fields
	 */
	Map<String, Float> getSearchFields(String site);

	/**
	 * Returns the search facets configuration for the given site
	 * @param site the site
	 * @return the facets
	 */
	Map<String, FacetTO> getFacets(String site);

	/**
	 * Get configured authoring url for given site
	 * @param siteId site identifier
	 * @return authoring url
	 */
	String getAuthoringUrl(String siteId);

	/**
	 * Get configure staging url for given site
	 * @param siteId site identifier
	 * @return staging url
	 */
	String getStagingUrl(String siteId);

	/**
	 * Get configured live url for given site
	 * @param siteId site identifier
	 * @return live url
	 */
	String getLiveUrl(String siteId);

	/**
	 * Get configured admin email address for notification emails for given site
	 * @param siteId site identifier
	 * @return admin email address
	 */
	String getAdminEmailAddress(String siteId);

	/**
	 * Check if it is configured to require peer review
	 * @param siteId site identifier
	 * @return true if require peer review is configured for site
	 */
	boolean isRequirePeerReview(String siteId);

	/**
	 * Get configured protected folder patterns for site
	 * @param siteId site identifier
	 * @return list of configured protected folders patterns
	 */
    List<String> getProtectedFolderPatterns(String siteId);
}
