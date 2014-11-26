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
package org.craftercms.studio.impl.v1.alfresco.service.api;


import java.util.List;

/**
 * This class provides the repository configuration information
 * 
 * @author hyanghee
 * 
 */
public interface ServicesConfig {

	/**
	 * get content type by site name
	 * 
	 * @param site
	 * @return content type
	 */
	//public QName getContentType(String site);

	/**
	 * get model configuration by site name
	 * 
	 * @param site
	 * @return model configuration
	 */
	//public Map<QName, ModelConfigTO> getModelConfig(String site);

	/**
	 * get search columns configuration by site name
	 * 
	 * @param site
	 * @return search columns configuration
	 */
	//public Map<String, QName> getSearchColumnsConfig(String site);

	/**
	 * get the content type by given namespace
	 * 
	 * @param site
	 * @param namespace
	 * @return QName if found. Otherwise it returns null
	 */
	//public QName getTypeByNamespace(String site, String namespace);

	/**
	 * get the root prefix of site. the root prefix represents the folder name
	 * pattern of the corporate and the geo site file locations e.g. if
	 * corporate files are under /site and the geo site files are under
	 * /site_geo then the root prefix should be "/site"
	 * 
	 * @param site
	 * @return root prefix
	 */
	//public String getRootPrefix(final String site);
	
	/**
	 * get the name of the web project for the given site
	 * 
	 * @param site
	 * @return web project name
	 */
	//public String getWemProject(final String site);

    /**
     * get a list of folder configuration. The top folders are used to as the
     * top categories when services return a collection of items such as
     * get-go-live-items call
     *
     * @param site
     * @return a list of folder configuration
     */
    //public List<DmFolderConfigTO> getFolders(final String site);

	/**
	 * get DM content type configuration by the given site and name
	 * @param site
	 * @param name
	 * @return content type
	 */
	//public ContentTypeConfigTO getContentTypeConfig(String site, String name);
	
	/**
	 * get component item URI patterns
	 * 
	 * @return component item URI patterns
	 */
	public List<String> getComponentPatterns(String site);

	/**
	 * get asset item URI patterns
	 * 
	 * @return asset item URI patterns
	 */
	public List<String> getAssetPatterns(String site);
	
	/**
	 * get page item URI patterns
	 * 
	 * @return page item URI patterns
	 */
	public List<String> getPagePatterns(String site);

	/**
	 * get document item URI patterns
	 * 
	 * @return document item URI patterns
	 */
	public List<String> getDocumentPatterns(String site);

    /**
     * get rendering template item URI patterns
     *
     * @return rendering template item URI patterns
     */
    public List<String> getRenderingTemplatePatterns(String site);

    /**
     * get level descriptor item URI patterns
     *
     * @return level descriptor item URI patterns
     */
    //public List<String> getLevelDescriptorPatterns(String site);

	/**
	 * get the category root path specified for each category (e.g. /site/websites for Pages)
	 * 
	 * @param site
	 * @param category
	 * @return category root path
	 */
	//public String getCategoryRootPath(String site, String category);

	/**
	 * get the name of level descriptor 
	 * 
	 * @param site
	 * @return level descriptor name
	 */
	public String getLevelDescriptorName(String site);

	
	/**
	 * get the delete dependencies related to a content type
	 * 
	 * @param site
	 * @return delete dependencys patterns
	 */
	//public List<DeleteDependencyConfigTO> getDeleteDependencyPatterns(String site, String contentType);
	
	/**
	 * get the copy dependencies pattern for a content type
	 * 
	 * @param site
	 * @param contentType
	 * @return
	 */
	//public List<CopyDependencyConfigTO> getCopyDependencyPatterns(String site, String contentType);
	
	/**
	 * get the default search configuration for all content types
	 * 
	 * @param site
	 * @return default search configuration
	 */
	//public SearchConfigTO getDefaultSearchConfig(String site);

	/**
	 * get a list of paths to exclude when traversing file/folder hierarchy
	 * 
	 * @param site
	 * @return a list of paths to exclude
	 */
	//public List<String> getExcludePaths(String site);

	/**
	 * get a list of paths to display in widgets
	 * 
	 * @param site
	 * @return a list of paths to display in widgets
	 */
	//public List<String> getDisplayInWidgetPathPatterns(String site);

	/**
	 * get the default timezone value
	 * @param site
	 * @return default timezone
	 */
	public String getDefaultTimezone(String site);
	
	/**
	 * is configuration for the site updated?
	 * 
	 * @param site
	 * @return
	 */
	//public boolean isUpdated(String site);
	
	/**
	 * 
	 * @param site
	 * @return
	 */
	//public boolean isCheckForRename(String site);
	
	/**
	 * get the common template configuration for the given site
	 * 
	 * @param site
	 * @return
	 */
	//public TemplateConfigTO getTemplateConfig(String site);

    //public String getRepositoryRootPath(String site);
    
    //public List<String> getPreviewableMimetypesPaterns(String site);


    //public String getLiveRepositoryPath(String site);
    
    //public boolean siteExists(String site);

    //Set<String> getAllAvailableSites();
}
