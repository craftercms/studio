/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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

import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.dom4j.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DmDependencyService {

    /** dependency types **/
    String DEPENDENCY_NAME_ASSET = "asset";
    String DEPENDENCY_NAME_COMPONENT = "component";
    String DEPENDENCY_NAME_DOCUMENT = "document";
    String DEPENDENCY_NAME_PAGE = "page";
    String DEPENDENCY_NAME_DELETE = "delete";
    String DEPENDENCY_NAME_RENDERING_TEMPLATE = "rendTemplate";
    String DEPENDENCY_NAME_LEVEL_DESCRIPTOR = "levelDesc";

    Map<String, Object> getDependencies(String site, String request, Boolean deleteDependencies) throws ServiceException;

    DmDependencyTO getDependenciesNoCalc(String site, String path, boolean populateUpdatedDependenciesOnly, boolean recursive, Set<String> processedDependencies);

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
    void setDependencies(String site, String path, Map<String,Set<String>> dependencies) throws ServiceException;

    void updateDependencies(String site,String path,String state);

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
    InputStream replaceDependencies(String site, Document document, Map<String,String> dependencies) throws ServiceException;

    /**
     * Return dependencies that are protected based on the copy pattern provided in the configuration
     *
     * <Dependency matching copy pattern, target location>
     * @param dependencyPath
     */
    Map<String, String> getCopyDependencies(String site,String relativePath, String dependencyPath) throws ServiceException;

    void extractDependenciesTemplate(String site, String path, StringBuffer templateContent, Map<String, Set<String>> globalDeps) throws ServiceException;

    void extractDependenciesStyle(String site, String path, StringBuffer styleContent, Map<String, Set<String>> globalDeps) throws ServiceException;

    void extractDependenciesJavascript(String site, String path, StringBuffer javascriptContent, Map<String, Set<String>> globalDeps) throws ServiceException;

    List<String> getDependencyPaths(String site, String path);

    List<String> getDependantPaths(String site, String path);

    void deleteDependenciesForSite(String site);

    void deleteDependenciesForSiteAndPath(String site, String path);

    /**
     * Get the content information of all dependant Items of the given path
     * @param site Site of owner of the path.
     * @param path Path of the content which will be check for dependant items.
     * @return A unmodifiable Set of ContentItemTO with all dependant items.
     */
    Set<ContentItemTO> getDependantItems(String site, String path);

    /**
     * Get the content information of all dependencies Items of the given path
     * @param site Site of owner of the path.
     * @param path Path of the content which will be check for dependency items.
     * @return A unmodifiable Set of ContentItemTO with all dependencies items.
     */
    Set<ContentItemTO> getDependenciesItems(String site, String path);
}
