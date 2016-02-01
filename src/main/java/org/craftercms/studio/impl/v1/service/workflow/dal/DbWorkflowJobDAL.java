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
package org.craftercms.studio.impl.v1.service.workflow.dal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.workflow.WorkflowItem;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJobProperty;
import org.craftercms.studio.api.v1.to.TableIndexCheckTO;

public class DbWorkflowJobDAL extends AbstractWorkflowJobDAL {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbWorkflowJobDAL.class);

	/** table names **/
	private static final String TABLE_JOB = "workflowJob";
	private static final String TABLE_JOB_PROPERTY = "workflowJobProperty";
	private static final String TABLE_ITEM = "workflowItem";

	/** table check and creation **/
	private static final String STATEMENT_CHECK_TABLE_EXISTS = "checkTableExists";

	/** table indexes **/
	private static final String INDEX_ID = "Id";
	private static final String INDEX_STATE = "State";

	/** job table statements **/
	private static final String STATEMENT_GET_JOB = "getJob";
	private static final String STATEMENT_GET_JOBS_BY_STATES = "getJobsByStates";
	private static final String STATEMENT_UPDATE_JOB = "updateJob";
	private static final String STATEMENT_DELETE_JOB = "deleteJob";
	private static final String STATEMENT_CREATE_JOB = "createJob";
	
	/** item table statements **/
	private static final String STATEMENT_GET_ITEM = "getItem";
	private static final String STATEMENT_GET_ITEMS_BY_JOB = "getItemsByJob";
	private static final String STATEMENT_UPDATE_ITEM = "updateItem";
	private static final String STATEMENT_DELETE_ITEM = "deleteItem";
	private static final String STATEMENT_CREATE_ITEM = "createItem";
	
	/** job property table statements **/
	private static final String STATEMENT_DELETE_JOB_PROPERTIES = "deleteJobProperties";
	private static final String STATEMENT_CREATE_JOB_PROPERTY = "createJobProperty";
	
	/**
	 * alfresco dataSource
	 */
	protected DataSource dataSource;

	/**
	 * SQL mapping client
	 */
	protected SqlMapClient sqlMap;

	/**
	 * db initialization script path
	 */
	protected String initializeScriptPath;

	@Override
	public WorkflowJob getJob(String id) {
		WorkflowJob job = null;
		try {
			LOGGER.debug("[TRANSLATION] looking up job by id: {0}", id);

			job = (WorkflowJob) sqlMap.queryForObject(STATEMENT_GET_JOB, id);
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while finding a job by id: {0}", e, id);
		}
		return job;
	}

	@Override
	public List<WorkflowJob> getJobsByState(Set<String> states) {
		List<WorkflowJob> jobs = null;
		try {
			LOGGER.debug("[TRANSLATION] looking up job by states: {0}", states);

			// TODO: might change the param type to be set and just pass a set
			Map<String, Object> params = null;
			if (states != null && !states.isEmpty()) {
				params = new HashMap<String, Object>();
				params.put("states", states);
			}
			jobs = (List<WorkflowJob>) sqlMap.queryForList(STATEMENT_GET_JOBS_BY_STATES, params);
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while querying for jobs by states: {0}", e, states);
		}
		return jobs;
	}

	@Override
	public WorkflowJob updateJob(WorkflowJob job) {
		try {
			LOGGER.debug("[TRANSLATION] updating job: {0}", job);

			sqlMap.startTransaction();
			// update job first
			sqlMap.update(STATEMENT_UPDATE_JOB, job);

			// delete all job properties
			String jobId = job.getId();
			sqlMap.delete(STATEMENT_DELETE_JOB_PROPERTIES, jobId);
			// create new job properties
			Map<String, String> properties = job.getProperties();
			if (!properties.isEmpty()) {
				sqlMap.startBatch();
				for (String name : properties.keySet()) {
					WorkflowJobProperty property = new WorkflowJobProperty(jobId, name, properties.get(name));
					sqlMap.insert(STATEMENT_CREATE_JOB_PROPERTY, property);
				}
				sqlMap.executeBatch();
			}
			sqlMap.commitTransaction();
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while updating job: {0}", e, job);
		}
		finally {
			try {
				sqlMap.endTransaction();
			}
			catch (SQLException e) {
				LOGGER.error("[TRANSLATION] Error creating a job: {0}", e, job);
			}
		}
		return job;
	}

	@Override
	public boolean deleteJob(String id) {
		boolean result = false;
		try {
			LOGGER.debug("[TRANSLATION] deleting a job by id: {0}", id);
			sqlMap.startTransaction();
			sqlMap.delete(STATEMENT_DELETE_JOB, id);
			sqlMap.delete(STATEMENT_DELETE_JOB_PROPERTIES, id);
			sqlMap.delete("deleteJobItems", id);
			sqlMap.commitTransaction();
			result = true;
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while deleting a job by id: {0}", e, id);
		}
		finally {
			try {
				sqlMap.endTransaction();
			}
			catch (SQLException e) {
				LOGGER.error("[TRANSLATION] Error deleting a job: {0}", e, id);
			}
		}
		return result;
	}

	@Override
	protected void writeNewJob(WorkflowJob job) {
		try {
			LOGGER.debug("[TRANSLATION] creating a job: {0}", job);

			sqlMap.startTransaction();
			sqlMap.insert(STATEMENT_CREATE_JOB, job);

			String jobId = job.getId();
			LOGGER.debug("[TRANSLATION] writing properties of job: {0}", jobId);

			// create job items
			List<WorkflowItem> items = job.getItems();
			if (!items.isEmpty()) {
				sqlMap.startBatch();
				for (WorkflowItem item : items) {
					sqlMap.insert(STATEMENT_CREATE_ITEM, item);
				}
				sqlMap.executeBatch();
			}
			// create job properties
			Map<String, String> properties = job.getProperties();
			if (!properties.isEmpty()) {
				sqlMap.startBatch();
				for (String name : properties.keySet()) {
					WorkflowJobProperty property = new WorkflowJobProperty(jobId, name, properties.get(name));
					sqlMap.insert(STATEMENT_CREATE_JOB_PROPERTY, property);
				}
				sqlMap.executeBatch();
			}

			sqlMap.commitTransaction();
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error creating a job: {0}", e, job);
		}
		finally {
			try {
				sqlMap.endTransaction();
			}
			catch (SQLException e) {
				LOGGER.error("[TRANSLATION] Error creating a job: {0}", e, job);
			}
		}
	}

	@Override
	public WorkflowItem getItem(String id) {
		WorkflowItem item = null;
		try {
			LOGGER.debug("[TRANSLATION] looking up an item by id: {0}", id);

			item = (WorkflowItem) sqlMap.queryForObject(STATEMENT_GET_ITEM, id);
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while querying for an item by id: {0}", e, id);
		}
		return item;
	}

	@Override
	public List<WorkflowItem> getItemsByJob(String jobId) {
		List<WorkflowItem> items = null;
		try {
			LOGGER.debug("[TRANSLATION] looking up items by job id: {0}", jobId);

			items = (List<WorkflowItem>) sqlMap.queryForList(STATEMENT_GET_ITEMS_BY_JOB, jobId);
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while querying for itmes by job id: {0}", e, jobId);
		}
		return items;
	}

	@Override
	public WorkflowItem updateItem(WorkflowItem item) {
		try {
			LOGGER.debug("[TRANSLATION] updating item: {0}", item);
			sqlMap.update(STATEMENT_UPDATE_ITEM, item);
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while updating item by id: {0}", e, item.getId());
		}
		return item;	
	}

	@Override
	public boolean deleteItem(String id) {
		boolean result = false;
		try {
			LOGGER.debug("[TRANSLATION] deleting an item by id: {0}", id);

			sqlMap.delete(STATEMENT_DELETE_ITEM, id);
			result = true;
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while deleting an item by id: {0}", e, id);
		}
		return result;
	}

	@Override
	protected void writeNewItem(WorkflowItem item) {
		try {
			LOGGER.debug("[TRANSLATION] creating an item: {0}", item);

			WorkflowItem newItem = (WorkflowItem) sqlMap.insert(STATEMENT_CREATE_ITEM, item);
			item.setId(newItem.getId());
		}
		catch (SQLException e) {
			LOGGER.error("[TRANSLATION] Error while creating an item: {0}", e, item);
		}
	}


	/**
	 * init tables and indexes
	 */
	public void initTable() {
		DataSource dataSource = sqlMap.getDataSource();
		Connection connection = null;
		int oldval = -1;
		try {
			connection = dataSource.getConnection();
			oldval = connection.getTransactionIsolation();
			if (oldval != Connection.TRANSACTION_READ_COMMITTED) {
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			}
			List<HashMap> checkTable = sqlMap.queryForList(STATEMENT_CHECK_TABLE_EXISTS);
			if (checkTable == null || checkTable.size() == 0) {
				ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);
				scriptRunner.runScript(Resources.getResourceAsReader(initializeScriptPath));
				LOGGER.debug("Adding indexes to {0}" + TABLE_JOB);
				addTableIndex(connection, TABLE_JOB, INDEX_ID);
				addTableIndex(connection, TABLE_JOB, INDEX_STATE);
				LOGGER.debug("Adding indexes to {0}" + TABLE_JOB_PROPERTY);
				addTableIndex(connection, TABLE_JOB_PROPERTY, INDEX_ID);
				LOGGER.debug("Adding indexes to {0}" + TABLE_ITEM);
				addTableIndex(connection, TABLE_ITEM, INDEX_ID);
			}
			if (oldval != -1) {
				connection.setTransactionIsolation(oldval);
			}
		}
		catch (Exception e) {
			LOGGER.error("Error while initializing workflow tables and indexes.", e);
		}
		finally {
			closeConnection(connection);
			connection = null;
		}
	}

	/**
	 * add table indexes
	 * 
	 * @param connection
	 * @param table the table to add index
	 * @param column the column to be indexed
	 * @throws SQLException
	 * @throws IOException
	 */
	private void addTableIndex(Connection connection, String table, String column) throws SQLException, IOException {
		List<TableIndexCheckTO> indexCheckResult = sqlMap.queryForList(table+".check"+column+"Index");
		if (indexCheckResult == null || indexCheckResult.size() == 0) {
			sqlMap.insert(table+".add"+column+"Index");
			connection.commit();
		}
	}

	/**
	 * close connection 
	 * 
	 * @param connection
	 */
	private void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			}
			catch (SQLException e) {
				LOGGER.error("Error while closing connection.", e);
			}
			connection = null;
		}
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the sqlMap
	 */
	public SqlMapClient getSqlMap() {
		return sqlMap;
	}

	/**
	 * @param sqlMap the sqlMap to set
	 */
	public void setSqlMap(SqlMapClient sqlMap) {
		this.sqlMap = sqlMap;
	}

	/**
	 * @return the initializeScriptPath
	 */
	public String getInitializeScriptPath() {
		return initializeScriptPath;
	}

	/**
	 * @param initializeScriptPath the initializeScriptPath to set
	 */
	public void setInitializeScriptPath(String initializeScriptPath) {
		this.initializeScriptPath = initializeScriptPath;
	}

}
