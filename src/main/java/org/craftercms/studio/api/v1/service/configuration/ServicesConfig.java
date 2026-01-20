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
package org.craftercms.studio.api.v1.service.configuration;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.CopyDependencyConfigTO;
import org.craftercms.studio.api.v1.to.FacetTO;

import java.util.List;
import java.util.Map;

/**
 * This class provides the repository configuration information
 *
 * @author hyanghee
 */
public interface ServicesConfig {

	/**
	 * get DM content type configuration by the given site and name
	 *
	 * @param site
	 * @param name
	 * @return content type
	 */
	ContentTypeConfigTO getContentTypeConfig(String site, String name) throws SiteNotFoundException;

	/**
	 * get component item URI patterns
	 *
	 * @return component item URI patterns
	 */
	List<String> getComponentPatterns(String site) throws SiteNotFoundException;

	/**
	 * get asset item URI patterns
	 *
	 * @return asset item URI patterns
	 */
	List<String> getAssetPatterns(String site) throws SiteNotFoundException;

	/**
	 * get page item URI patterns
	 *
	 * @return page item URI patterns
	 */
	List<String> getPagePatterns(String site) throws SiteNotFoundException;

	/**
	 * get document item URI patterns
	 *
	 * @return document item URI patterns
	 */
	List<String> getDocumentPatterns(String site) throws SiteNotFoundException;

	/**
	 * get rendering template item URI patterns
	 *
	 * @return rendering template item URI patterns
	 */
	List<String> getRenderingTemplatePatterns(String site) throws SiteNotFoundException;

	/**
	 * get scripts item URI patterns
	 *
	 * @return scripts item URI patterns
	 */
	List<String> getScriptsPatterns(String site) throws SiteNotFoundException;

	/**
	 * Get configuration item URI patterns
	 *
	 * @param site site identifier
	 * @return configuration items
	 */
	List<String> getConfigurationPatterns(String site) throws SiteNotFoundException;

	/**
	 * get level descriptor item URI patterns
	 *
	 * @return level descriptor item URI patterns
	 */
	List<String> getLevelDescriptorPatterns(String site) throws SiteNotFoundException;

	/**
	 * get the name of level descriptor
	 *
	 * @param site
	 * @return level descriptor name
	 */
	String getLevelDescriptorName(String site) throws SiteNotFoundException;

	/**
	 * get the copy dependencies pattern for a content type
	 *
	 * @param site
	 * @param contentType
	 * @return copy dependencies patterns
	 */
	List<CopyDependencyConfigTO> getCopyDependencyPatterns(String site, String contentType) throws SiteNotFoundException;

	/**
	 * get the default timezone value
	 *
	 * @param site
	 * @return default timezone
	 */
	String getDefaultTimezone(String site) throws SiteNotFoundException;

	List<String> getPreviewableMimetypesPaterns(String site) throws SiteNotFoundException;

	/**
	 * Get the pattern for the plugin folder in the given site
	 */
	String getPluginFolderPattern(String site) throws SiteNotFoundException;

	String getStagingEnvironment(String site) throws SiteNotFoundException;

	String getLiveEnvironment(String site) throws SiteNotFoundException;

	boolean isStagingEnvironmentEnabled(String site) throws SiteNotFoundException;

	/**
	 * Returns the search field configuration for the given site
	 *
	 * @param site the site
	 * @return the search fields
	 */
	Map<String, Float> getSearchFields(String site) throws SiteNotFoundException;

	/**
	 * Returns the search facets configuration for the given site
	 *
	 * @param site the site
	 * @return the facets
	 */
	Map<String, FacetTO> getFacets(String site) throws SiteNotFoundException;

	/**
	 * Get configured authoring url for given site
	 *
	 * @param siteId site identifier
	 * @return authoring url
	 */
	String getAuthoringUrl(String siteId) throws SiteNotFoundException;

	/**
	 * Get configured live url for given site
	 *
	 * @param siteId site identifier
	 * @return live url
	 */
	String getLiveUrl(String siteId) throws SiteNotFoundException;

	/**
	 * Get configured admin email address for notification emails for given site
	 *
	 * @param siteId site identifier
	 * @return admin email address
	 */
	String getAdminEmailAddress(String siteId) throws SiteNotFoundException;

	/**
	 * Check if it is configured to require peer review
	 *
	 * @param siteId site identifier
	 * @return true if require peer review is configured for site
	 */
	boolean isRequirePeerReview(String siteId) throws SiteNotFoundException;

	/**
	 * Get configured protected folder patterns for site
	 *
	 * @param siteId site identifier
	 * @return list of configured protected folders patterns
	 */
	List<String> getProtectedFolderPatterns(String siteId) throws SiteNotFoundException;
}
