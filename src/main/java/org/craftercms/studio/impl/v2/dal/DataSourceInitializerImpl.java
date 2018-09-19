/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.dal;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.DataSourceInitializer;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_DRIVER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_CONFIGURE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_CREATE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_CHARS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_LENGTH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_UPGRADE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_URL;

public class DataSourceInitializerImpl implements DataSourceInitializer {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceInitializerImpl.class);

    /**
     * Database queries
     */
    private final static String DB_QUERY_CHECK_SCHEMA_EXISTS =
            "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'crafter'";
    private final static String DB_QUERY_USE_CRAFTER = "use crafter";
    private final static String DB_QUERY_SET_ADMIN_PASSWORD =
            "UPDATE user SET password = '{password}' WHERE username = 'admin'";

    protected String delimiter;
    protected StudioConfiguration studioConfiguration;

    protected DbIntegrityValidator integrityValidator;

    @Override
    public void initDataSource()  throws EntitlementException {
        if (isEnabled()) {
            String configureDbScriptPath = getConfigureDBScriptPath();
            String createDbScriptPath = getCreateDBScriptPath();

            try {
                Class.forName(studioConfiguration.getProperty(DB_DRIVER));
            } catch (Exception e) {
                logger.error("Error connecting to database", e);
            }

            try(Connection conn = DriverManager.getConnection(studioConfiguration.getProperty(DB_INITIALIZER_URL))) {
                // Configure DB
                logger.info("Configure database from script " + configureDbScriptPath);
                ScriptRunner sr = new ScriptRunner(conn);

                sr.setDelimiter(delimiter);
                sr.setStopOnError(true);
                sr.setLogWriter(null);
                InputStream is = getClass().getClassLoader().getResourceAsStream(configureDbScriptPath);
                Reader reader = new InputStreamReader(is);
                try {
                    sr.runScript(reader);
                } catch (RuntimeSqlException e) {
                    logger.error("Error while running configure DB script", e);
                }

                logger.debug("Check if database schema already exists");
                try(Statement statement = conn.createStatement();
                    ResultSet rs = statement.executeQuery(DB_QUERY_CHECK_SCHEMA_EXISTS)) {

                    if (rs.next()) {
                        logger.debug("Database already exists. Validate the integrity of the database");
//                        statement.execute(DB_QUERY_USE_CRAFTER);
//                        TODO: Make this execute after all upgrades are completed
//                        integrityValidator.validate(conn);

                    } else {
                        // Database does not exist
                        logger.info("Database does not exists.");
                        logger.info("Creating database from script " + createDbScriptPath);
                        sr = new ScriptRunner(conn);

                        sr.setDelimiter(delimiter);
                        sr.setStopOnError(true);
                        sr.setLogWriter(null);
                        is = getClass().getClassLoader().getResourceAsStream(createDbScriptPath);
                        reader = new InputStreamReader(is);
                        try {
                            sr.runScript(reader);

                            if (isRandomAdminPasswordEnabled()) {
                                String randomPassword = generateRandomPassword();
                                String hashedPassword = CryptoUtils.hashPassword(randomPassword);
                                String update = DB_QUERY_SET_ADMIN_PASSWORD.replace("{password}", hashedPassword);
                                statement.executeUpdate(update);
                                conn.commit();
                                logger.info("*** Admin Account Password: \"" + randomPassword + "\" ***");
                            }

                            integrityValidator.store(conn);
                        } catch (RuntimeSqlException e) {
                            logger.error("Error while running create DB script", e);
                        }
                    }
                } catch (SQLException e) {
                    logger.error("Error while initializing database", e);
                }
            } catch (SQLException e) {
                logger.error("Error while getting connection to DB", e);
            }
        }
    }

    public boolean isEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(DB_INITIALIZER_ENABLED));
        return toReturn;
    }

    private String generateRandomPassword() {
        int passwordLength = Integer.parseInt(
                studioConfiguration.getProperty(DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_LENGTH));
        String passwordChars = studioConfiguration.getProperty(DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_CHARS);
        return RandomStringUtils.random(passwordLength, passwordChars);
    }

    private String getConfigureDBScriptPath() {
        return studioConfiguration.getProperty(DB_INITIALIZER_CONFIGURE_DB_SCRIPT_LOCATION);
    }

    private String getCreateDBScriptPath() {
        return studioConfiguration.getProperty(DB_INITIALIZER_CREATE_DB_SCRIPT_LOCATION);
    }

    private String getUpgradeDBScriptPath() {
        return studioConfiguration.getProperty(DB_INITIALIZER_UPGRADE_DB_SCRIPT_LOCATION);
    }

    private boolean isRandomAdminPasswordEnabled() {
        boolean toRet = Boolean.parseBoolean(
                studioConfiguration.getProperty(DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_ENABLED));
        return toRet;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setIntegrityValidator(final DbIntegrityValidator integrityValidator) {
        this.integrityValidator = integrityValidator;
    }

}
