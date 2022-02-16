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

import java.beans.ConstructorProperties;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

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
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that executes a database script.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>filename</strong>: (required) the name of the db script file</li>
 *     <li><strong>updateIntegrity</strong>: (optional) indicates if the db integrity should be updated, defaults to
 *     true</li>
 * </ul>
 *
 * @author joseross
 */
public class DbScriptUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(DbScriptUpgradeOperation.class);

    public static final String CONFIG_KEY_FILENAME = "filename";
    public static final String CONFIG_KEY_INTEGRITY = "updateIntegrity";
    public static final String SQL_DELIMITER = " ;";
    protected final static String CRAFTER_SCHEMA_NAME = "@crafter_schema_name";

    /**
     * Path of the folder to search the script file.
     */
    protected String scriptFolder;

    /**
     * Filename of the script.
     */
    protected String fileName;

    /**
     * Indicates if the integrity value should be updated after executing the script.
     */
    protected boolean updateIntegrity;

    /**
     * The database integrity validator.
     */
    protected DbIntegrityValidator integrityValidator;

    @ConstructorProperties({"studioConfiguration", "scriptFolder", "integrityValidator"})
    public DbScriptUpgradeOperation(StudioConfiguration studioConfiguration, String scriptFolder,
                                    DbIntegrityValidator integrityValidator) {
        super(studioConfiguration);
        this.scriptFolder = scriptFolder;
        this.integrityValidator = integrityValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration config) {
        fileName = config.getString(CONFIG_KEY_FILENAME);
        updateIntegrity = config.getBoolean(CONFIG_KEY_INTEGRITY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
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
            String scriptContent = IOUtils.toString(scriptFile.getInputStream(), UTF_8);
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

}
