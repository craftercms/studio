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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;

/**
 * Implementation of {@link UpgradeOperation} that executes a database script.
 *
 * <p>Supported YAML properties:
 * <ul>
 *     <li><strong>filename</strong>: (required) the name of the db script file</li>
 *     <li><strong>updateIntegrity</strong>: (optional) indicates if the db integrity should be updated, defaults to
 *     true</li>
 * </ul>
 * </p>
 *
 * @author joseross
 */
public class DbScriptUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(DbScriptUpgradeOperation.class);

    public static final String CONFIG_KEY_FILENAME = "filename";
    public static final String CONFIG_KEY_INTEGRITY = "updateIntegrity";
    public static final String SQL_DELIMITER = " ;";
    private final static String CRAFTER_SCHEMA_NAME = "@crafter_schema_name";

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

    public void setUpdateIntegrity(final boolean updateIntegrity) {
        this.updateIntegrity = updateIntegrity;
    }

    @Required
    public void setScriptFolder(final String scriptFolder) {
        this.scriptFolder = scriptFolder;
    }

    @Required
    public void setIntegrityValidator(final DbIntegrityValidator integrityValidator) {
        this.integrityValidator = integrityValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration<ImmutableNode> config) {
        fileName = config.getString(CONFIG_KEY_FILENAME);
        updateIntegrity = config.getBoolean(CONFIG_KEY_INTEGRITY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        try {
            integrityValidator.validate(getConnection());
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
                    studioConfiguration.getProperty(DB_SCHEMA)))) {
                Connection connection = getConnection();
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
