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

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.craftercms.cstudio.alfresco.to.TableIndexCheckTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyDaoServiceImpl implements DependencyDaoService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyDaoServiceImpl.class);

    /**
     * alfresco dataSource
     */
    protected DataSource _daDataSource;

    protected SqlMapClient _sqlMap = null;
    public SqlMapClient getSqlMapClient() {
        return this._sqlMap;
    }
    public void setSqlMapClient(SqlMapClient sqlMap) {
        this._sqlMap = sqlMap;
    }

    /**
     * dependency table name
     */
    protected String _dependencyTableName;

    /**
     * path table name
     */
    protected String _pathTableName;


    /** statements **/
    protected static final String STATEMENT_GET_DEPENDENCY_TYPE = "dependency.getDependencyType";
    protected static final String STATEMENT_GET_DEPENDENCIES = "dependency.getDependencies";
    protected static final String STATEMENT_GET_DEPENDENCIES_BY_TYPE = "dependency.getDependenciesByType";
    protected static final String STATEMENT_DELETE_ALL_DEPENDENCIES = "dependency.deleteAllDependencies";
    protected static final String STATEMENT_DELETE_ALL_SOURCE_DEPENDENCIES = "dependency.deleteAllSourceDependencies";
    protected static final String STATEMENT_DELETE_ALL_TARGET_DEPENDENCIES = "dependency.deleteAllTargetDependencies";
    protected static final String STATEMENT_DELETE_DEPENDENCY = "dependency.deleteDependency";
    protected static final String STATEMENT_DELETE_DEPENDENCIES_FOR_SITE = "dependency.deleteDependenciesForSite";
    protected static final String STATEMENT_INSERT_DEPENDENCY = "dependency.insertDependency";
    protected static final String STATEMENT_UPDATE_DEPENDENCY_TYPE = "dependency.updateDependencyType";

    /** table check and creation **/
    private static final String STATEMENT_CREATE_TABLE = "dependency.createTable";
    private static final String STATEMENT_CHECK_TABLE_EXISTS = "dependency.checkTableExists";
    private static final String STATEMENT_CREATE_TRIGGER = "dependency.createTrigger";

    /** table indexes **/
    private static final String STATEMENT_ADD_SITE_IDX = "dependency.addSiteIndex";
    private static final String STATEMENT_CHECK_SITE_IDX = "dependency.checkSiteIndex";
    private static final String STATEMENT_ADD_SOURCEPATH_IDX = "dependency.addSourcePathIndex";
    private static final String STATEMENT_CHECK_SOURCEPATH_IDX = "dependency.checkSourcePathIndex";

    protected String initializeScriptPath;
    public String getInitializeScriptPath() {
        return initializeScriptPath;
    }
    public void setInitializeScriptPath(String initializeScriptPath) {
        this.initializeScriptPath = initializeScriptPath;
    }

    @Override
    public void initIndexes() {
        DataSource dataSource = _sqlMap.getDataSource();
        Connection connection = null;
        int oldval = -1;
        try {
            connection = dataSource.getConnection();
            oldval = connection.getTransactionIsolation();
            if (oldval != Connection.TRANSACTION_READ_COMMITTED) {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
            List<HashMap> checkTable = _sqlMap.queryForList(STATEMENT_CHECK_TABLE_EXISTS);
            if (checkTable == null || checkTable.size() < 1) {
                ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);
                scriptRunner.runScript(Resources.getResourceAsReader(initializeScriptPath));
            }
            connection.commit();
            List<TableIndexCheckTO> indexCheckResult = _sqlMap.queryForList(STATEMENT_CHECK_SITE_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                _sqlMap.insert(STATEMENT_ADD_SITE_IDX);
            }
            indexCheckResult = _sqlMap.queryForList(STATEMENT_CHECK_SOURCEPATH_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                _sqlMap.insert(STATEMENT_ADD_SOURCEPATH_IDX);
            }
            connection.commit();
            if (oldval != -1) {
                connection.setTransactionIsolation(oldval);
            }
        } catch (SQLException e) {
            logger.error("Error while initializing Dependency table DB indexes.", e);
        } catch (IOException e) {
            logger.error("Error while initializing Sequence table DB indexes.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.wcm.dependency.DependencyDaoService#getDependenciesByType(java.lang.String, java.lang.String, java.lang.String)
      */
    @SuppressWarnings("unchecked")
    @Override
    public List<DependencyEntity> getDependenciesByType(String site, String path, String type) throws SQLException {
        // create dependency entity
        DependencyEntity entity = new DependencyEntity(site, path, null, type);
        return (List<DependencyEntity>) _sqlMap.queryForList(STATEMENT_GET_DEPENDENCIES_BY_TYPE, entity);
    }


    /*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.wcm.dependency.DependencyDaoService#getDependencies(java.lang.String, java.lang.String)
	 */
    @SuppressWarnings("unchecked")
    @Override
    public List<DependencyEntity> getDependencies(String site, String path) throws SQLException {
        DependencyEntity dependencyObj = new DependencyEntity(site, path);
        List<DependencyEntity> items = _sqlMap.queryForList(STATEMENT_GET_DEPENDENCIES, dependencyObj);
        return items;
    }

    /*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.wcm.dependency.DependencyDaoService#setDependencies(java.lang.String, java.lang.String, java.util.Map)
	 */
    @Override
    public void setDependencies(String site, String path, Map<String, List<String>> dependencies) throws SQLException {
        try {
            _sqlMap.startTransaction();
            // clean up dependencies first
            DependencyEntity sourceObj = new DependencyEntity(site, path);
            _sqlMap.delete(STATEMENT_DELETE_ALL_SOURCE_DEPENDENCIES, sourceObj);
            // insert all dependencies
            _sqlMap.startBatch();
            if (dependencies != null) {
                for (String type : dependencies.keySet()) {
                    List<String> files = dependencies.get(type);
                    if (files != null) {
                        for (String file : files) {
                            DependencyEntity dependencyObj = new DependencyEntity(site, path, file, type);
                            _sqlMap.insert(STATEMENT_INSERT_DEPENDENCY, dependencyObj);
                        }
                    }
                }
            }
            _sqlMap.executeBatch();
            _sqlMap.commitTransaction();
        } finally {
            _sqlMap.endTransaction();
        }
    }

    /**
     * delete all dependencies that the give path file is relying on
     *
     * @param site
     * @param path
     * @throws SQLException
     */
    protected void deleteAllSourceDependencies(String site, String path) throws SQLException {
        DependencyEntity dependencyObj = new DependencyEntity(site, path);
        _sqlMap.delete(STATEMENT_DELETE_ALL_SOURCE_DEPENDENCIES, dependencyObj);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.wcm.dependency.DependencyDaoService#deleteDependency(java.lang.String, java.lang.String, java.lang.String)
      */
    @Override
    public void deleteDependency(String site, String path, String dependency) throws SQLException {
        DependencyEntity dependencyObj = new DependencyEntity(site, path, dependency, null);
        _sqlMap.delete(STATEMENT_DELETE_DEPENDENCY, dependencyObj);
    }

    @Override
    public void deleteDependenciesForSite(String site) {
        try {
            logger.info("Deleting dependencies for site " + site);
            _sqlMap.delete(STATEMENT_DELETE_DEPENDENCIES_FOR_SITE, site);
        } catch (SQLException e) {
            logger.error("Error while deleting dependencies for site " + site);
        }
    }
}
