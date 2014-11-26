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
package org.craftercms.cstudio.alfresco.sequence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapSession;
import org.craftercms.cstudio.alfresco.service.exception.SequenceException;
import org.craftercms.cstudio.alfresco.to.SequenceTO;
import org.craftercms.cstudio.alfresco.to.TableIndexCheckTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.sqlmap.client.SqlMapClient;

import javax.sql.DataSource;

public class SequenceDaoServiceImpl implements SequenceDaoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SequenceDaoServiceImpl.class);
	
	/**
	 * 
	 */
	protected SqlMapClient _sqlMap = null;
	
	/** 
	 * the size of each id space
	 */
	private int _step;
	
	/** statements **/
	private static final String STATEMENT_GET_SEQUENCE = "sequence.getSequence";
	private static final String STATEMENT_CREATE_SEQUENCE = "sequence.createSequence";
	private static final String STATEMENT_DELETE_SEQUENCE = "sequence.deleteSequence";
	private static final String STATEMENT_INCREASE_ID_SPACE = "sequence.increaseIdSpace";

    /** table check and creation **/
    private static final String STATEMENT_CREATE_TABLE = "sequence.createTable";
    private static final String STATEMENT_CHECK_TABLE_EXISTS = "sequence.checkTableExists";

    /** table indexes **/
    private static final String STATEMENT_ADD_NAMESPACE_IDX = "sequence.addNamespaceIndex";
    private static final String STATEMENT_CHECK_NAMESPACE_IDX = "sequence.checkNamespaceIndex";

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
//        try {
//            connection = dataSource.getConnection();
//            oldval = connection.getTransactionIsolation();
//        } catch (SQLException e) { }
        try {
//            if (connection == null) {
//                connection = dataSource.getConnection();
//            }
//            if (oldval != -1) {
//                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//            }
            //connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
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
            List<HashMap> indexCheckResult = _sqlMap.queryForList(STATEMENT_CHECK_NAMESPACE_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                _sqlMap.insert(STATEMENT_ADD_NAMESPACE_IDX);
            }
            connection.commit();
            if (oldval != -1) {
                connection.setTransactionIsolation(oldval);
            }
        } catch (SQLException e) {
            LOGGER.error("Error while initializing Sequence table DB indexes.", e);
        } catch (IOException e) {
            LOGGER.error("Error while initializing Sequence table DB indexes.", e);
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
      * @see org.craftercms.crafter.alfresco.sequence.SequenceDaoService#createSequence(java.lang.String)
      */
	public synchronized SequenceTO createSequence(String namespace) {
		try {
			SequenceDAO entity = (SequenceDAO) _sqlMap.queryForObject(STATEMENT_GET_SEQUENCE, namespace);
			if (entity == null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(namespace + " does not exists. creating new sequence.");
				}
				entity = new SequenceDAO(namespace, 0, _step);
				_sqlMap.insert(STATEMENT_CREATE_SEQUENCE, entity);
				if (entity != null) {
					SequenceTO sequence = new SequenceTO(entity.getNamespace(), entity.getSql_generator(), entity.getStep());
					return sequence;
				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Sequence cannot be created for " + namespace + ".");
					}
					return null;
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(namespace + " already exists. returning the next ID space.");
				}
				return getIdSpace(namespace, false);
			}
		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Sequence cannot be created for " + namespace + ".", e);
			}
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.crafter.alfresco.sequence.SequenceDaoService#deleteSequence(java.lang.String)
	 */
	public void deleteSequence(String namespace) throws SequenceException {
		try {
			_sqlMap.delete(STATEMENT_DELETE_SEQUENCE, namespace);
		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Sequence cannot be deleted for " + namespace + ".", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.crafter.alfresco.sequence.SequenceDaoService#sequenceExists(java.lang.String)
	 */
	public boolean sequenceExists(String namespace) {
		try {
			SequenceDAO entity = (SequenceDAO) _sqlMap.queryForObject(STATEMENT_GET_SEQUENCE, namespace);
			return entity != null;
		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error while checking if " + namespace + " exists.", e);
			}
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.crafter.alfresco.sequence.SequenceDaoService#getIdSpace(java.lang.String, boolean)
	 */
	public synchronized SequenceTO getIdSpace(String namespace, boolean create) {
		try {
			SequenceDAO entity = (SequenceDAO) _sqlMap.queryForObject(STATEMENT_GET_SEQUENCE, namespace);
			if (entity == null) {
				// if the sequence doesn't exist, create the sequence and return the first id space
				if (create) {
					entity = new SequenceDAO(namespace, 0, _step);
                    _sqlMap.insert(STATEMENT_CREATE_SEQUENCE, entity);
					SequenceTO sequence = new SequenceTO(entity.getNamespace(), entity.getSql_generator(), entity.getStep());
					return sequence;
				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Error while getting the next ID space for " + namespace
                                + ". The namespace does not exists and cannot be created (set create = true).");
					}
					return null;
				}
			} else {
				// if it exists, return the next id space, increasing the id space by 1
				_sqlMap.update(STATEMENT_INCREASE_ID_SPACE, namespace);
				entity = (SequenceDAO) _sqlMap.queryForObject(STATEMENT_GET_SEQUENCE, namespace);
				if (entity != null) {
					SequenceTO sequence = new SequenceTO(entity.getNamespace(), entity.getSql_generator(), entity.getStep());
					return sequence;
				} else {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error("Unable to get the next ID space for " + namespace);
					}
					return null;
				}
			}
		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error while getting the next ID space for " + namespace + ".", e);
			}
			return null;
        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.crafter.alfresco.sequence.SequenceDaoService#getSequence(java.lang.String)
	 */
	public SequenceTO getSequence(String namespace) {
		try {
			SequenceDAO entity = (SequenceDAO) _sqlMap.queryForObject(STATEMENT_GET_SEQUENCE, namespace);
			if (entity != null) {
				SequenceTO sequence = new SequenceTO(entity.getNamespace(), entity.getSql_generator(), entity.getStep());
				return sequence;
			} else {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Sequence is not found for " + namespace + ".");
				}
				return null;
			}
		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error while getting a sequence for " + namespace + ".", e);
			}
			return null;
		}
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(int step) {
		this._step = step;
	}

	/**
	 * 
	 * @param sqlMap set the SqlMapClient
	 */
    public void setSqlMapClient(SqlMapClient sqlMap) {
        this._sqlMap = sqlMap;
    }


}
