/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;

import java.beans.ConstructorProperties;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;

public final class MigrateWorkflowUpgradeOperation extends DbScriptUpgradeOperation {

    public static final Logger logger = LoggerFactory.getLogger(MigrateWorkflowUpgradeOperation.class);

    public static final String CONFIG_KEY_STORED_PROCEDURE_NAME = "spName";
    public static final String QUERY_GET_ALL_SITES =
            "SELECT id, site_id FROM " + CRAFTER_SCHEMA_NAME + ".site WHERE system = 0 AND deleted = 0";
    public static final String STORED_PROCEDURE_NAME = "@spName";
    public static final String SP_PARAM_SITE = "@site";
    public static final String QUERY_CALL_STORED_PROCEDURE =
            "call @spName('@site')";


    private String crafterSchemaName;
    private String spName;

    @ConstructorProperties({"studioConfiguration", "scriptFolder", "integrityValidator"})
    public MigrateWorkflowUpgradeOperation(StudioConfiguration studioConfiguration,
                                           String scriptFolder,
                                           DbIntegrityValidator integrityValidator) {
        super(studioConfiguration, scriptFolder, integrityValidator);
    }

    @Override
    public void doInit(HierarchicalConfiguration config) {
        super.doInit(config);
        crafterSchemaName = studioConfiguration.getProperty(DB_SCHEMA);
        spName = config.getString(CONFIG_KEY_STORED_PROCEDURE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        // create stored procedure from script
        if (isNotEmpty(fileName)) {
            super.doExecute(context);
        }
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
                processSite(context, site.getValue());
            }
        } catch (SQLException e) {
            logger.error("Error getting DB connection", e);
        }
    }

    private void processSite(final StudioUpgradeContext context, String site) throws UpgradeException {
        logger.info("Processing site: " + site);
        try (Connection connection = context.getConnection()) {
            integrityValidator.validate(connection);
        } catch (SQLException e) {
            // for backwards compatibility
            logger.warn("Could not validate database integrity", e);
        } catch (Exception e) {
            throw new UpgradeNotSupportedException("The current database version can't be upgraded", e);
        }

        try (Connection connection = context.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall(
                    QUERY_CALL_STORED_PROCEDURE.replace(STORED_PROCEDURE_NAME, spName)
                            .replace(SP_PARAM_SITE, site));
            logger.debug("Calling " + spName + " for " + site);
            callableStatement.execute();
        } catch (SQLException e) {
            logger.error("Error populating data from DB", e);
        }
    }

}
