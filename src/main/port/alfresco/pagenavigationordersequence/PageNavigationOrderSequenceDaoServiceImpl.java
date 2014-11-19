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
package org.craftercms.cstudio.alfresco.pagenavigationordersequence;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.craftercms.cstudio.alfresco.to.PageNavigationOrderSequenceTO;
import org.craftercms.cstudio.alfresco.to.TableIndexCheckTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class PageNavigationOrderSequenceDaoServiceImpl implements PageNavigationOrderSequenceDaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageNavigationOrderSequenceDaoServiceImpl.class);

    protected SqlMapClient _sqlMapClient;
    public SqlMapClient getSqlMapClient() {
        return _sqlMapClient;
    }
    public void setSqlMapClient(SqlMapClient sqlMapClient) {
        this._sqlMapClient = sqlMapClient;
    }

    protected String initializeScriptPath;
    public String getInitializeScriptPath() {
        return initializeScriptPath;
    }
    public void setInitializeScriptPath(String initializeScriptPath) {
        this.initializeScriptPath = initializeScriptPath;
    }

    /** statements **/
    private static final String STATEMENT_GET_SEQUENCE = "pageNavigationOrderSequence.getSequence";
    private static final String STATEMENT_CREATE_SEQUENCE = "pageNavigationOrderSequence.createSequence";
    private static final String STATEMENT_DELETE_SEQUENCE = "pageNavigationOrderSequence.deleteSequence";
    private static final String STATEMENT_DELETE_SEQUENCES_FOR_SITE = "pageNavigationOrderSequence.deleteSequencesForSite";
    private static final String STATEMENT_INCREASE_SEQUENCE = "pageNavigationOrderSequence.increaseSequence";
    private static final String STATEMENT_SET_SEQUENCE = "pageNavigationOrderSequence.setSequence";

    /** table check and creation **/
    private static final String STATEMENT_CREATE_TABLE = "pageNavigationOrderSequence.createTable";
    private static final String STATEMENT_CHECK_TABLE_EXISTS = "pageNavigationOrderSequence.checkTableExists";

    /** table indexes **/
    private static final String STATEMENT_ADD_FOLDER_IDX = "pageNavigationOrderSequence.addFolderIndex";
    private static final String STATEMENT_CHECK_FOLDER_IDX = "pageNavigationOrderSequence.checkFolderIndex";

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
            }
            connection.commit();
            List<TableIndexCheckTO> indexCheckResult = _sqlMapClient.queryForList(STATEMENT_CHECK_FOLDER_IDX);
            if (indexCheckResult == null || indexCheckResult.size() < 1) {
                _sqlMapClient.insert(STATEMENT_ADD_FOLDER_IDX);
            }
            connection.commit();
            if (oldval != -1) {
                connection.setTransactionIsolation(oldval);
            }
        } catch (SQLException e) {
            LOGGER.error("Error while initializing Page Navigation Order DB indexes.", e);
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

    @Override
    public PageNavigationOrderSequenceTO createSequence(String folderId, String site, String path) {
        try {
            PageNavigationOrderSequenceDAO entity = (PageNavigationOrderSequenceDAO)_sqlMapClient.queryForObject(STATEMENT_GET_SEQUENCE, folderId);
            if (entity == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(folderId + " does not exists. creating new sequence.");
                }
                entity = new PageNavigationOrderSequenceDAO(folderId, site, path, 0F);
                _sqlMapClient.insert(STATEMENT_CREATE_SEQUENCE, entity);
                if (entity != null) {
                    PageNavigationOrderSequenceTO sequence = new PageNavigationOrderSequenceTO(entity.getFolder_id(), entity.getSite(), entity.getPath(), entity.getMax_count());
                    return sequence;
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Page Navigation Order Sequence cannot be created for " + folderId + ".");
                    }
                    return null;
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(folderId + " already exists. returning the next sequence.");
                }
                return increaseSequence(folderId);
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Page Navigation Order Sequence cannot be created for " + folderId + ".", e);
            }
            return null;
        }
    }

    @Override
    public PageNavigationOrderSequenceTO getSequence(String folderId) {
        try {
            PageNavigationOrderSequenceDAO entity = (PageNavigationOrderSequenceDAO) _sqlMapClient.queryForObject(STATEMENT_GET_SEQUENCE, folderId);
            if (entity != null) {
                PageNavigationOrderSequenceTO sequence = new PageNavigationOrderSequenceTO(entity.getFolder_id(), entity.getSite(), entity.getPath(), entity.getMax_count());
                return sequence;
            } else {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Page Navigation Order Sequence is not found for " + folderId + ".");
                }
                return null;
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while getting a sequence for " + folderId + ".", e);
            }
            return null;
        }
    }

    @Override
    public PageNavigationOrderSequenceTO increaseSequence(String folderId) {
        try {
            PageNavigationOrderSequenceDAO entity = (PageNavigationOrderSequenceDAO) _sqlMapClient.queryForObject(STATEMENT_GET_SEQUENCE, folderId);
            if (entity == null) {
                LOGGER.error("Sequnece does not exist for " + folderId + ".");
                return null;
            } else {
                // if it exists, return the next id space, increasing the id space by 1
                _sqlMapClient.update(STATEMENT_INCREASE_SEQUENCE, folderId);
                entity = (PageNavigationOrderSequenceDAO) _sqlMapClient.queryForObject(STATEMENT_GET_SEQUENCE, folderId);
                if (entity != null) {
                    PageNavigationOrderSequenceTO sequence = new PageNavigationOrderSequenceTO(entity.getFolder_id(), entity.getSite(), entity.getPath(), entity.getMax_count());
                    return sequence;
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Unable to get the next sequence for " + folderId);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while getting the next sequence for " + folderId + ".", e);
            }
            return null;
        }
    }

    @Override
    public void setSequence(PageNavigationOrderSequenceTO sequenceTO) {
        try {
            PageNavigationOrderSequenceDAO entity = (PageNavigationOrderSequenceDAO) _sqlMapClient.queryForObject(STATEMENT_GET_SEQUENCE, sequenceTO.getFolderId());
            if (entity == null) {
                entity = new PageNavigationOrderSequenceDAO(sequenceTO.getFolderId(), sequenceTO.getSite(), sequenceTO.getPath(), sequenceTO.getMaxCount());
                _sqlMapClient.insert(STATEMENT_CREATE_SEQUENCE, entity);
            } else {
                // if it exists, return the next id space, increasing the id space by 1
                entity.setMax_count(sequenceTO.getMaxCount());
                _sqlMapClient.update(STATEMENT_SET_SEQUENCE, entity);
            }
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while getting the next sequence for " + sequenceTO.getFolderId() + ".", e);
            }
        }
    }

    @Override
    public void deleteSequence(String folderId) {
        try {
            _sqlMapClient.delete(STATEMENT_DELETE_SEQUENCE, folderId);
        } catch (SQLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Sequence cannot be deleted for " + folderId + ".", e);
            }
        }
    }

    @Override
    public void deleteSequencesForSite(String site) {
        try {
            LOGGER.debug("Deleting sequences for site " + site);
            _sqlMapClient.delete(STATEMENT_DELETE_SEQUENCES_FOR_SITE, site);
        } catch (SQLException e) {
            LOGGER.error("Error while deleting sequences for site " + site);
        }
    }
}
