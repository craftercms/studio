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
package org.craftercms.studio.api.v1.service.dependency;

import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.dom4j.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DmDependencyService {

    /** dependency types **/
    public static final String DEPENDENCY_NAME_ASSET = "asset";
    public static final String DEPENDENCY_NAME_COMPONENT = "component";
    public static final String DEPENDENCY_NAME_DOCUMENT = "document";
    public static final String DEPENDENCY_NAME_PAGE = "page";
    public static final String DEPENDENCY_NAME_DELETE = "delete";
    public static final String DEPENDENCY_NAME_RENDERING_TEMPLATE = "rendTemplate";
    public static final String DEPENDENCY_NAME_LEVEL_DESCRIPTOR = "levelDesc";

    /**
     * populate dependencies as content items into the given content item
     *  @param item
     * @param populateUpdatedDependecinesOnly
     * @param site
     */
    //public void populateDependencyContentItems(String site, DmContentItemTO item, boolean populateUpdatedDependecinesOnly);

    Map<String, Object> getDependencies(String site, String request, Boolean deleteDependencies) throws ServiceException;

    DmDependencyTO getDependenciesNoCalc(String site, String path, boolean populateUpdatedDependenciesOnly, boolean recursive);

    /**
     * get dependent items as WCM content items from the content at the given path
     *
     * @param site
     * @param sub (optional)
     * @param path
     * @param populateUpdatedDependecinesOnly
     * @param recursive
     * 			get dependency recursively?
     * @return a request item that contains a list of dependent file names
     */
    DmDependencyTO getDependencies(String site, String path, boolean populateUpdatedDependecinesOnly, boolean recursive);

    /**
     * get dependencies of all submitted items and present as a tree structure
     *
     * @param site
     * @param submittedItems
     * @param comparator
     * 			item comparator to sort dependency items
     * @param multiLevelChildren
     * 			create the dependency tree of child pages to be the same as the repository hierarchy if true
     * @return a list of items that contains dependent items
     */
    //public List<DmContentItemTO> getDependencies(String site, List<String> submittedItems, DmContentItemComparator comparator, boolean multiLevelChildren) throws ServiceException;

    /**
     * get dependencies of all submitted items and present as a tree structure
     *
     * @param site
     * @param submittedItems
     * @param comparator
     * 			item comparator to sort dependency items
     * @param multiLevelChildren
     * 			create the dependency tree of child pages to be the same as the repository hierarchy if true
     * @return a list of items that contains dependent items
     */
    //public List<DmContentItemTO> getDependencies(String site, List<String> submittedItems, DmContentItemComparator comparator, boolean multiLevelChildren,boolean deleteDependencies) throws ServiceException;

    //public void populatePageDependencies(String site, DmContentItemTO item, boolean populateUpdatedDependecinesOnly) ;

    /**
     * get direct dependent file names of the content at the given path
     *
     * @param site
     * @param path
     * @return a list of dependencies
     */
    //public List<DependencyEntity> getDirectDependencies(String site, String path);

    /**
     * extract direct dependencies from the given document
     *
     * @param site
     * @param path
     * @param document
     * @throws ServiceException
     */
    void extractDependencies(String site, String path, Document document, Map<String, Set<String>> globalDeps) throws ServiceException;

    /**
     *
     * @param site
     * @param path
     * @param dependencies map of type and targets. DB srcid|target id|type
     */
    void setDependencies(String site, String path, Map<String,List<String>> dependencies) throws ServiceException;

    void updateDependencies(String site,String path,String state);

	//public Set<DmDependencyTO> getDeleteDependencies(String site,
	//		String sourceContentPath, String dependencyPath) throws ServiceException;

	//public List<String> extractDependenciesFromDocument(String site,
	//		Document doc) throws ServiceException ;

	//public List<String> getRemovedDependenices(DmDependencyDiffService.DiffRequest diffRequest,
	//		boolean b) throws ServiceException;

    /**
     * Replace dependencies in the document based on the values in the Map original,target
     *
     * used by copy/paste scenario where page dependencies are duplicated
     *
     * @param site
     * @param document
     * @param dependencies
     * @return
     * @throws ServiceException
     */
    public InputStream replaceDependencies(String site, Document document, Map<String,String> dependencies) throws ServiceException;

    /**
     * Return dependencies that are protected based on the copy pattern provided in the configuration
     *
     * <Dependency matching copy pattern, target location>
     * @param dependencyPath
     */
    public Map<String, String> getCopyDependencies(String site,String relativePath, String dependencyPath) throws ServiceException;

    public void extractDependenciesTemplate(String site, String path, StringBuffer templateContent, Map<String, Set<String>> globalDeps) throws ServiceException;

    public void extractDependenciesStyle(String site, String path, StringBuffer styleContent, Map<String, Set<String>> globalDeps) throws ServiceException;

    public void extractDependenciesJavascript(String site, String path, StringBuffer javascriptContent, Map<String, Set<String>> globalDeps) throws ServiceException;

    public List<String> getDependencyPaths(String site, String path);

	//Set<DmDependencyTO> getDeleteDependencies(String site, String sourceContentPath, String dependencyPath, boolean isLiveRepo) throws ServiceException;

    void deleteDependenciesForSite(String site);

    void deleteDependenciesForSiteAndPath(String site, String path);
}
