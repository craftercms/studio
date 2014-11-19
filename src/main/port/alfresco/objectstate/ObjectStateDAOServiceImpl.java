/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
 */
package org.craftercms.cstudio.alfresco.objectstate;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import javolution.util.FastList;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.to.ObjectStateTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectStateDAOServiceImpl implements ObjectStateDAOService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectStateDAOServiceImpl.class);

    protected SqlMapClient _sqlMapClient;

    public SqlMapClient getSqlMapClient() {
        return _sqlMapClient;
    }

    public void setSqlMapClient(SqlMapClient sqlMapClient) {
        this._sqlMapClient = sqlMapClient;
    }

    /**
     * statements 
     */
    private static final String STATEMENT_GET_OBJECT_STATE = "objectState.getObjectState";
    private static final String STATEMENT_GET_OBJECT_STATES = "objectState.getObjectStates";
    private static final String STATEMENT_GET_OBJECT_STATE_BY_STATUS = "objectState.getObjectByStatus";
    private static final String STATEMENT_INSERT_ENTRY = "objectState.insertEntry";
    private static final String STATEMENT_SET_OBJECT_STATE = "objectState.setObjectState";
    private static final String STATEMENT_SET_OBJECT_STATE_BULK = "objectState.setObjectStateBulk";
    private static final String STATEMENT_SET_SYSTEM_PROCESSING = "objectState.setSystemProcessing";
    private static final String STATEMENT_SET_SYSTEM_PROCESSING_BULK = "objectState.setSystemProcessingBulk";
    private static final String STATEMENT_UPDATE_OBJECT_PATH = "objectState.updateObjectPath";
    private static final String STATEMENT_IS_FOLDER_LIVE = "objectState.isFolderLive";
    private static final String STATEMENT_DELETE_OBJECT_STATES_FOR_SITE = "objectState.deleteObjectStatesForSite";
    private static final String STATEMENT_DELETE_OBJECT_STATE = "objectState.deleteObjectState";
    private static final String STATEMENT_DELETE_OBJECT_STATE_FOR_PATH = "objectState.deleteObjectStateForPath";
    private static final String STATEMENT_DELETE_OBJECT_STATE_FOR_PATHS = "objectState.deleteObjectStateForPaths";

    /**
     * table check and creation *
     */
    private static final String STATEMENT_CREATE_TABLE = "objectState.createTable";
    private static final String STATEMENT_CHECK_TABLE_EXISTS = "objectState.checkTableExists";

    /**
     * table indexes *
     */
    private static final String STATEMENT_ADD_OBJECT_IDX = "objectState.addObjectIndex";
    private static final String STATEMENT_CHECK_OBJECT_IDX = "objectState.checkObjectIndex";

    private static final String STATEMENT_CHECK_PATH_SIZE = "objectState.checkPathSize";

    protected String initializeScriptPath;

    public String getInitializeScriptPath() {
        return initializeScriptPath;
    }

    public void setInitializeScriptPath(String initializeScriptPath) {
        this.initializeScriptPath = initializeScriptPath;
    }

    protected int bulkOperationBatchSize = 500;

    public int getBulkOperationBatchSize() {
        return bulkOperationBatchSize;
    }

    public void setBulkOperationBatchSize(int bulkOperationBatchSize) {
        this.bulkOperationBatchSize = bulkOperationBatchSize;
    }

    @Override
    public void initIndexes() {
        DataSource dataSource = _sqlMapClient.getDataSource();
        Connection connection = null;
        int oldval = -1;
        try {
            connection = dataSource.getConnection();
            oldval = connection.getTransactionIsolation();
            if (oldval != Connection.TRANSACTION_READ_COMMITTED) {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
            List<HashMap> checkTable = _sqlMapClient.queryForList(STATEMENT_CHECK_TABLE_EXISTS);
            if (checkTable == null || checkTable.size() < 1) {
                ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);
                scriptRunner.runScript(Resources.getResourceAsReader(initializeScriptPath));
            } else {
                Integer checkColumnCTEUsername = (Integer)_sqlMapClient.queryForObject(STATEMENT_CHECK_PATH_SIZE);
                if (checkColumnCTEUsername < 2000) {
                    ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);
                    scriptRunner.runScript(Resources.getResourceAsReader(initializeScriptPath.replace("initialize.sql",
                            "alter_path_column_size.sql")));
                }

            }
            List<HashMap> indexCheckResult = _sqlMapClient.queryForList(STATEMENT_CHECK_OBJECT_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                _sqlMapClient.insert(STATEMENT_ADD_OBJECT_IDX);
            }
            connection.commit();
            if (oldval != -1) {
                connection.setTransactionIsolation(oldval);
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while initializing Object State table DB indexes.", e);
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while initializing Sequence table DB indexes.", e);
            }
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

    @Override
    public void insertNewObject(String objectId, String site, String path) {
        if (StringUtils.isNotEmpty(objectId)) {
            Long id = null;
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("object_id", objectId);
                ObjectStateDAO entity = (ObjectStateDAO) _sqlMapClient.queryForObject(STATEMENT_GET_OBJECT_STATE, params);
                if (entity != null) {
                    LOGGER.warn("Object state entry already exists for site " + site + " , path " + path);
                    return;
                }

                ObjectStateDAO entry = new ObjectStateDAO();
                entry.setObjectId(objectId);
                entry.setSite(site);
                entry.setPath(path);
                entry.setState(ObjectStateService.State.NEW_UNPUBLISHED_UNLOCKED.name());
                id = (Long) _sqlMapClient.insert(STATEMENT_INSERT_ENTRY, entry);
                _sqlMapClient.flushDataCache();
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("[" + Thread.currentThread().getName() + "] Error while adding new object state entry " +
                        "for site " + site + " , " + "path " + path, e);
                }
            }
        } else {
            LOGGER.warn("Object state can not be inserted for empty object id; site " + site + " ," + " path " + path);
        }
    }

    @Override
    public ObjectStateTO getObjectState(String objectId) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("object_id", objectId);
            ObjectStateDAO entity = (ObjectStateDAO) _sqlMapClient.queryForObject(STATEMENT_GET_OBJECT_STATE, params);
            if (entity != null) {
                return toObjectStateTo(entity);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("[" + Thread.currentThread().getName() + "] Object State is not found for " +
                        objectId + ".");
                }
                return null;
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while getting a Object State for " + objectId + ".", e);
            }
            return null;
        }
    }

    @Override
    public void setObjectState(String objectId, ObjectStateService.State state) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("object_id", objectId);
            params.put("state", state.name());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Setting state: " + state.name() + " for object " + objectId);
            }
            _sqlMapClient.update(STATEMENT_SET_OBJECT_STATE, params);
            _sqlMapClient.flushDataCache();
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
            	// TODO CodeRev: give the state as well
            	// TODO CodeRev: ALSO.... how does the caller know this operation failed?!?!
            	LOGGER.error("Error while updating state for " + objectId + ".", e);
            }
        }
    }

    @Override
    public void setSystemProcessing(String objectId, boolean isSystemProcessing) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("object_id", objectId);
            params.put("system_processing", isSystemProcessing);
            _sqlMapClient.update(STATEMENT_SET_SYSTEM_PROCESSING, params);
            _sqlMapClient.flushDataCache();
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
            	// TODO CodeRev: tell me what the value of the flag was
            	// TODO CodeRev: ALSO.... how does the caller know this operation failed?!?!
                LOGGER.error("Error while setting system processing for " + objectId + ".", e);
            }
        }
    }

    @Override
    public void setSystemProcessingBulk(List<String> objectIds, boolean isSystemProcessing) {
    	if (objectIds != null && !objectIds.isEmpty()) {
            if (objectIds.size() < bulkOperationBatchSize) {
                setSystemProcessingBulkPartial(objectIds, isSystemProcessing);
            } else {
                List<List<String>> partitions = new FastList<List<String>>();
                for (int i = 0; i < objectIds.size();) {
                    partitions.add(objectIds.subList(i, Math.min(i + bulkOperationBatchSize, objectIds.size())));
                    i = (i + 1) * bulkOperationBatchSize;
                }
                for (List<String> part : partitions) {
                    setSystemProcessingBulkPartial(part, isSystemProcessing);
                }
            }
        }
    }

    private void setSystemProcessingBulkPartial(List<String> objectIds, boolean isSystemProcessing) {
    	// TODO CodeRev: see comment above, I don't understand what partial is all about
    	if (objectIds != null && !objectIds.isEmpty()) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("object_ids", objectIds);
                params.put("system_processing", isSystemProcessing);
                _sqlMapClient.update(STATEMENT_SET_SYSTEM_PROCESSING_BULK, params);
                _sqlMapClient.flushDataCache();
            } catch (SQLException e) {
                // TODO CodeRev: This is really bad, if a partial fails there is no indication to the caller that
                // TODO CodeRev: the failure happened... now what?
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while setting system processing for package.", e);
                }
            }
        }
    }

    @Override
    public void deleteObjectStatesForSite(String site) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting object states for site " + site);
            }
            _sqlMapClient.delete(STATEMENT_DELETE_OBJECT_STATES_FOR_SITE, site);
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
            	// TODO CodeRev: How does the caller know this operation failed?
                LOGGER.error("Error while deleting object states for site " + site, e);
            }
        }
    }

    @Override
    public void deleteObjectState(String objectId) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting object state for " + objectId);
            }
            _sqlMapClient.delete(STATEMENT_DELETE_OBJECT_STATE, objectId);
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
            	// TODO CodeRev: How does the caller know this operation failed?
            	LOGGER.error("Error while deleting object state for " + objectId, e);
            }
        }
    }

    @Override
    public void deleteObjectStatesForPath(String site, String path) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting object states for site " + site + ", path " + path);
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("path", path);
            _sqlMapClient.delete(STATEMENT_DELETE_OBJECT_STATE_FOR_PATH, params);
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
            	// TODO CodeRev: How does the caller know this operation failed?
            	LOGGER.error("Error while deleting object states for site " + site + " path " + path, e);
            }
        }
    }

    @Override
    public void deleteObjectStatesForPaths(String site, List<String> paths) {
        if (paths != null && !paths.isEmpty()) {
            if (paths.size() < bulkOperationBatchSize) {
                deleteObjectStatesForPathsPartial(site, paths);
            } else {
                List<List<String>> partitions = new FastList<List<String>>();
                for (int i = 0; i < paths.size();) {
                    partitions.add(paths.subList(i, Math.min(i + bulkOperationBatchSize, paths.size())));
                    i = (i + 1) * bulkOperationBatchSize;
                }
                for (List<String> part : partitions) {
                    deleteObjectStatesForPathsPartial(site, part);
                }
            }
        }
    }

    private void deleteObjectStatesForPathsPartial(String site, List<String> paths) {
        if (paths != null && !paths.isEmpty()) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("paths", paths);
                _sqlMapClient.delete(STATEMENT_DELETE_OBJECT_STATE_FOR_PATHS, params);
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    // TODO CodeRev: How does the caller know this operation failed?
                    LOGGER.error("Error while deleting object states for site " + site , e);
                }
            }
        }
    }

    @Override
    public List<ObjectStateTO> getObjectStateByStates(String site, List<ObjectStateService.State> states) {
        if (states != null && !states.isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            List<String> statesValues = new ArrayList<String>();
            for (ObjectStateService.State state : states) {
                statesValues.add(state.name());
            }
            params.put("states", statesValues);
            params.put("site", site);
            List<ObjectStateTO> result = new ArrayList<ObjectStateTO>();
            try {
                List<ObjectStateDAO> tmp = _sqlMapClient.queryForList(STATEMENT_GET_OBJECT_STATE_BY_STATUS, params);
                for (ObjectStateDAO objectStateDAO : tmp) {
                    result.add(toObjectStateTo(objectStateDAO));
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    // TODO CodeRev: How does the caller know this operation failed, what they get is an empty list and
                    // TODO CodeRev: the false notion that an empty list is correct
                    LOGGER.error("Error getting ObjectState Status for " + params.toString(), e);
                }
            }
            return result;
        } else {
            return new FastList<ObjectStateTO>(0);
        }
    }

    @Override
    public void updateObjectPath(String objectId, String newPath) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("object_id", objectId);
            params.put("path", newPath);
            _sqlMapClient.update(STATEMENT_UPDATE_OBJECT_PATH, params);
            _sqlMapClient.flushDataCache();
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
            	// TODO CodeRev: How does the caller know this operation failed?
                LOGGER.error("Error while setting system processing for " + objectId + ".", e);
            }
        }
    }

    @Override
    public boolean isFolderLive(String site, String path) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("path", path + "%");
            int result = (Integer) _sqlMapClient.queryForObject(STATEMENT_IS_FOLDER_LIVE, params);
            if (result > 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
            	// TODO CodeRev: How does the caller know this operation failed? Now I get the wrong answer and I have no idea
                LOGGER.error("Error while checking if folder " + path + " in site " + site + " is live.");
            }
            return false;
        }
    }

    @Override
    public List<ObjectStateTO> getObjectStates(List<String> objectIds) {
        if (objectIds != null && !objectIds.isEmpty()) {
            if (objectIds.size() < bulkOperationBatchSize) {
                return getObjectStatesPartial(objectIds);
            } else {
                List<List<String>> partitions = new FastList<List<String>>();
                List<ObjectStateTO> result = new FastList<ObjectStateTO>();
                for (int i = 0; i < objectIds.size();) {
                    partitions.add(objectIds.subList(i, Math.min(i + bulkOperationBatchSize, objectIds.size())));
                    i = (i + 1) * bulkOperationBatchSize;
                }
                for (List<String> part : partitions) {
                    result.addAll(getObjectStatesPartial(part));
                }
                return result;
            }
        } else {
            return new FastList<ObjectStateTO>(0);
        }
    }

    private List<ObjectStateTO> getObjectStatesPartial(List<String> objectIds) {
    	// TODO CodeRev: here we return null when there is an error.. BUT in the method above... 
    	// TODO CodeRev we never check for null which will surely lead to an NPE
        if (objectIds != null && !objectIds.isEmpty()) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("object_ids", objectIds);
                List<ObjectStateDAO> entities = (List<ObjectStateDAO>) _sqlMapClient.queryForList(STATEMENT_GET_OBJECT_STATES, params);
                if (entities != null) {
                    return toObjectStateTo(entities);
                } else {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Object State is not found for " + objectIds + ".");
                    }
                    return null;
                }
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while getting a Object State for " + objectIds + ".", e);
                }
                return null;
            }
        } else {
            return new FastList<ObjectStateTO>(0);
        }
    }

    @Override
    public void setObjectStateBulk(List<String> objectIds, ObjectStateService.State state) {
        if (objectIds != null && !objectIds.isEmpty()) {
            if (objectIds.size() < bulkOperationBatchSize) {
                setObjectStateBulkPartial(objectIds, state);
            } else {
                List<List<String>> partitions = new FastList<List<String>>();
                for (int i = 0; i < objectIds.size();) {
                    partitions.add(objectIds.subList(i, Math.min(i + bulkOperationBatchSize, objectIds.size())));
                    i = (i + 1) * bulkOperationBatchSize;
                }
                for (List<String> part : partitions) {
                    setObjectStateBulkPartial(part, state);
                }
            }
        }
    }

    private void setObjectStateBulkPartial(List<String> objectIds, ObjectStateService.State state) {
    	// TODO CodeRev: previous comment about batches applies here
        if (objectIds != null && !objectIds.isEmpty()) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("object_ids", objectIds);
                params.put("state", state.name());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Setting state: " + state.name() + " for objects " + objectIds);
                }
                _sqlMapClient.update(STATEMENT_SET_OBJECT_STATE_BULK, params);
                _sqlMapClient.flushDataCache();
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while updating state for " + objectIds + ".", e);
                }
            }
        }
    }

    private ObjectStateTO toObjectStateTo(ObjectStateDAO objectStateDAO) {
    	// TODO CodeRev: One issue I see here is that this object looks like it direct maps to the other object except
    	// TODO CodeRev: except for some slight name differences... but now we have an O(n) iteration just to change names
        ObjectStateTO objectStateTO = new ObjectStateTO();
        objectStateTO.setObjectId(objectStateDAO.getObjectId());
        objectStateTO.setSite(objectStateDAO.getSite());
        objectStateTO.setPath(objectStateDAO.getPath());
        objectStateTO.setState(ObjectStateService.State.valueOf(objectStateDAO.getState()));
        objectStateTO.setSystemProcessing(objectStateDAO.isSystemProcessing());
        return objectStateTO;
    }

    private List<ObjectStateTO> toObjectStateTo(List<ObjectStateDAO> objectStateDAOs) {
        List<ObjectStateTO> toRet = new FastList<ObjectStateTO>(objectStateDAOs.size());
        for (ObjectStateDAO state : objectStateDAOs) {
            toRet.add(toObjectStateTo(state));
        }
        return toRet;
    }
    
 // TODO CodeRev: Also, I personally do not care much for private methods because they cannot be extended.  How about protected?
}
