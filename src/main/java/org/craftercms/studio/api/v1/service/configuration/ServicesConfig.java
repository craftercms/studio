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
package org.craftercms.studio.api.v1.service.configuration;


import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.CopyDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DmFolderConfigTO;

import java.util.List;

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

    void reloadConfiguration(String site);

    /**
     * Get sandbox branch name for given site
     *
     * @param site
     * @return
     */
    String getSandboxBranchName(String site);

    String getStagingEnvironment(String site);

    String getLiveEnvironment(String site);
}
