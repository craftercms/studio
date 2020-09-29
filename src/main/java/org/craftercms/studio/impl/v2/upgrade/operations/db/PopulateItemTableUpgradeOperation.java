/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.upgrade.operations.db;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;

public final class PopulateItemTableUpgradeOperation extends DbScriptUpgradeOperation {

    public static final Logger logger = LoggerFactory.getLogger(PopulateItemTableUpgradeOperation.class);

    public static final String CONFIG_KEY_CLEAR_EXISTING_DATA = "clearExistingData";
    public static final String CONFIG_KEY_STORED_PROCEDURE_NAME = "spName";
    public static final String QUERY_GET_ALL_SITES =
            "SELECT id, site_id FROM " + CRAFTER_SCHEMA_NAME + ".site WHERE system = 0 AND deleted = 0";
    public static final String STORED_PROCEDURE_NAME = "@spName";
    public static final String SP_PARAM_SITE = "@site";
    public static final String QUERY_PARAM_SITE_ID = "@siteId";
    public static final String QUERY_CHECK_DATA_EXISTS =
            "SELECT count(1) FROM " + CRAFTER_SCHEMA_NAME + ".item WHERE site_id = " + QUERY_PARAM_SITE_ID;
    public static final String QUERY_CALL_STORED_PROCEDURE =
            "call @spName(@site)";

    private boolean clearExistingData;
    private String crafterSchemaName;
    private String spName;

    public PopulateItemTableUpgradeOperation(StudioConfiguration studioConfiguration, String scriptFolder,
                                             DbIntegrityValidator integrityValidator) {
        super(studioConfiguration, scriptFolder, integrityValidator);
    }

    @Override
    public void doInit(HierarchicalConfiguration config) {
        super.doInit(config);
        clearExistingData = config.getBoolean(CONFIG_KEY_CLEAR_EXISTING_DATA, false);
        crafterSchemaName = studioConfiguration.getProperty(DB_SCHEMA);
        spName = config.getString(CONFIG_KEY_STORED_PROCEDURE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        // create stored procedure from script
        super.doExecute(context);
        // get all sites from DB
        Map<Long, String> sites = new HashMap<Long, String>();
        try (Connection connection = context.getConnection()) {
            try(Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(
                    QUERY_GET_ALL_SITES.replace(CRAFTER_SCHEMA_NAME, crafterSchemaName))) {
                while (rs.next()) {
                    sites.put(rs.getLong(1), rs.getString(2));
                }
            } catch (SQLException e) {
                logger.error("Error getting all sites from DB", e);
            }
            // loop over all sites
            for (Map.Entry<Long, String> site : sites.entrySet()) {
                processSite(context, site.getKey(), site.getValue());
            }
        } catch (SQLException e) {
            logger.error("Error getting DB connection", e);
        }
    }

    private void processSite(final StudioUpgradeContext context, long siteId, String site) {
        // check if data exists
        try (Connection connection = context.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_CHECK_DATA_EXISTS.replace(CRAFTER_SCHEMA_NAME, crafterSchemaName)
                    .replace(QUERY_PARAM_SITE_ID, String.valueOf(siteId)))) {
                ResultSet rs = statement.executeQuery();
                if (!rs.next() || rs.getInt(1) < 1 || clearExistingData) {
                    populateDataFromDB(context, siteId);
                    populateDataFromRepo(context, site);
                }

            } catch (SQLException e) {
                logger.error("Error while checking if item data already exists for site " + site);
            } catch (UpgradeException e) {
                e.printStackTrace();
            }
        } catch (SQLException throwables) {
            logger.error("Error while getting db connection");
        }
    }

    private void populateDataFromDB(final StudioUpgradeContext context, long siteId) throws UpgradeException {
        try (Connection connection = context.getConnection()) {
            integrityValidator.validate(connection);
        } catch (SQLException e) {
            // for backwards compatibility
            logger.warn("Could not validate database integrity", e);
        } catch (Exception e) {
            throw new UpgradeNotSupportedException("The current database version can't be upgraded", e);
        }
        Resource scriptFile = new ClassPathResource(scriptFolder).createRelative(fileName);
        logger.info("Executing db script {0}", scriptFile.getFilename());
        try {
            String scriptContent = IOUtils.toString(scriptFile.getInputStream());
            try (Reader reader = new StringReader(scriptContent.replaceAll(CRAFTER_SCHEMA_NAME,
                    studioConfiguration.getProperty(DB_SCHEMA)));
                 Connection connection = context.getConnection()) {
                ScriptRunner scriptRunner = new ScriptRunner(connection);
                scriptRunner.setDelimiter(SQL_DELIMITER);
                scriptRunner.setStopOnError(true);
                scriptRunner.setLogWriter(null);
                scriptRunner.runScript(reader);
                connection.commit();
                if (updateIntegrity) {
                    integrityValidator.store(connection);
                }
            }
        } catch (Exception e) {
            logger.error("Error executing db script", e);
            throw new UpgradeException("Error executing sql script " + scriptFile.getFilename(), e);
        }
    }

    private void populateDataFromRepo(final StudioUpgradeContext context, final String site) {

    }
}
