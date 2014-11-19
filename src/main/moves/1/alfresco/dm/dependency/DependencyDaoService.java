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
package org.craftercms.cstudio.alfresco.dm.dependency;


import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DependencyDaoService {

    public void initIndexes();

    /**
     * get dependencies that the content at the given path is dependent on  by the given dependency type
     *
     * @param site
     * @param path
     * @param type
     *            dependency type (e.g. component, asset, document)
     * @return a list of dependencies
     * @throws SQLException
     */
    public List<DependencyEntity> getDependenciesByType(String site, String path, String type) throws SQLException;

    /**
     * get dependencies that the content at the given path is dependent on
     *
     * @param site
     * @param path
     * @return a list of dependencies
     * @throws SQLException
     */
    public List<DependencyEntity> getDependencies(String site, String path) throws SQLException;

    /**
     * set dependency files of the given path. This operation will remove old
     * dependency relationship that do not appear in the dependencies map
     *
     * @param site
     * @param path
     * @param dependencies
     * @throws SQLException
     */
    public void setDependencies(String site, String path, Map<String, List<String>> dependencies) throws SQLException;

    /**
     * delete dependency between the given two files
     *
     * @param site
     * @param path
     * @param dependency
     * @throws SQLException
     */
    public void deleteDependency(String site, String path, String dependency) throws SQLException;

    public void deleteDependenciesForSite(String site);
}
