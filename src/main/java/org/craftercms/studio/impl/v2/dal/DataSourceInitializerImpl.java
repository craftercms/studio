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

package org.craftercms.studio.impl.v2.dal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.DataSourceInitializer;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_DRIVER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_INITIALIZER_CREATE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_INITIALIZER_CREATE_SCHEMA_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_INITIALIZER_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_CHARS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_LENGTH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_INITIALIZER_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_PASSWORD;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_USER;

public class DataSourceInitializerImpl implements DataSourceInitializer {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceInitializerImpl.class);

    /**
     * Database queries
     */
    private final static String SCHEMA = "{schema}";
    private final static String CRAFTER_SCHEMA_NAME = "@crafter_schema_name";
    private final static String CRAFTER_USER = "@crafter_user";
    private final static String CRAFTER_PASSWORD = "@crafter_password";
    private final static String DB_QUERY_CHECK_SCHEMA_EXISTS =
            "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '{schema}'";
    private final static String DB_QUERY_CHECK_TABLES = "SHOW TABLES FROM {schema}";
    private final static String DB_QUERY_SET_ADMIN_PASSWORD =
            "UPDATE {schema}.user SET password = '{password}' WHERE username = 'admin'";
    private final static String DB_QUERY_CHECK_ADMIN_PASSWORD_EMPTY =
            "SELECT CASE WHEN ISNULL(password) > 0 THEN 1 WHEN CHAR_LENGTH(password) = 0 THEN 1 ELSE 0 END FROM " +
            "{schema}.user WHERE username = 'admin' ";

    protected String delimiter;
    protected StudioConfiguration studioConfiguration;
    protected DbIntegrityValidator integrityValidator;

    @Override
    public void initDataSource() {
        if (isEnabled()) {
            try {
                Class.forName(studioConfiguration.getProperty(DB_DRIVER));
            } catch (Exception e) {
                logger.error("Error loading JDBC driver", e);
            }

            try (Connection conn = DriverManager.getConnection(studioConfiguration.getProperty(DB_INITIALIZER_URL))) {
                logger.debug("Check if database schema already exists");
                try(Statement statement = conn.createStatement();
                    ResultSet rs = statement.executeQuery(
                            DB_QUERY_CHECK_SCHEMA_EXISTS.replace(SCHEMA, studioConfiguration.getProperty(DB_SCHEMA)))) {

                    if (rs.next()) {
                        logger.debug("Database schema exists. Check if it is empty.");
                        try (ResultSet rs2 = statement.executeQuery(
                                DB_QUERY_CHECK_TABLES.replace(SCHEMA, studioConfiguration.getProperty(DB_SCHEMA)))) {
                            List<String> tableNames = new ArrayList<String>();
                            while (rs2.next()) {
                                tableNames.add(rs2.getString(1));
                            }
                            if (tableNames.size() == 0) {
                                createDatabaseTables(conn, statement);
                            } else {
                                logger.debug("Database already exists. Validate the integrity of the database");
                            }
                        }
                    } else {
                        // Database does not exist
                        createSchema(conn);
                        createDatabaseTables(conn, statement);
                    }

                    // Check for admin empty password
                    try (ResultSet rs3 = statement.executeQuery(
                            DB_QUERY_CHECK_ADMIN_PASSWORD_EMPTY.replace(SCHEMA, studioConfiguration.getProperty(DB_SCHEMA)))) {
                        if (rs3.next()) {
                            if (rs3.getInt(1) > 0) {
                                setRandomAdminPassword(conn, statement);
                            }
                        }
                    }

                } catch (SQLException | IOException e) {
                    logger.error("Error while initializing database", e);
                }
            } catch (SQLException e) {
                logger.error("Error while connecting to initialize DB", e);
            }
        }
    }

    private void createDatabaseTables(Connection conn, Statement statement) throws SQLException, IOException {
        String createDbScriptPath = getCreateDBScriptPath();
        // Database does not exist
        logger.info("Database tables do not exist.");
        logger.info("Creating database tables from script " + createDbScriptPath);
        ScriptRunner sr = new ScriptRunner(conn);

        sr.setDelimiter(delimiter);
        sr.setStopOnError(true);
        sr.setLogWriter(null);
        InputStream is = getClass().getClassLoader().getResourceAsStream(createDbScriptPath);
        String scriptContent = IOUtils.toString(is);
        Reader reader = new StringReader(
                scriptContent.replaceAll(CRAFTER_SCHEMA_NAME, studioConfiguration.getProperty(DB_SCHEMA)));
        try {
            sr.runScript(reader);

            if (isRandomAdminPasswordEnabled()) {
                setRandomAdminPassword(conn, statement);
            }

            integrityValidator.store(conn);
        } catch (RuntimeSqlException e) {
            logger.error("Error while running create DB script", e);
        }
    }

    private void setRandomAdminPassword(Connection conn, Statement statement) throws SQLException {
        String randomPassword = generateRandomPassword();
        String hashedPassword = CryptoUtils.hashPassword(randomPassword);
        String update = DB_QUERY_SET_ADMIN_PASSWORD.replace(
                SCHEMA, studioConfiguration.getProperty(DB_SCHEMA)).replace("{password}", hashedPassword);
        statement.executeUpdate(update);
        conn.commit();
        logger.info("*** Admin Account Password: \"" + randomPassword + "\" ***");
    }

    private void createSchema(Connection conn) throws IOException {
        String createSchemaScriptPath = getCreateSchemaScriptPath();
        // Database does not exist
        logger.info("Database schema does not exists.");
        logger.info("Creating database schema from script " + createSchemaScriptPath);
        ScriptRunner sr = new ScriptRunner(conn);

        sr.setDelimiter(delimiter);
        sr.setStopOnError(true);
        sr.setLogWriter(null);
        InputStream is = getClass().getClassLoader().getResourceAsStream(createSchemaScriptPath);
        String scriptContent = IOUtils.toString(is, UTF_8);
        Reader reader = new StringReader(
                scriptContent.replaceAll(CRAFTER_SCHEMA_NAME, studioConfiguration.getProperty(DB_SCHEMA))
                        .replaceAll(CRAFTER_USER, studioConfiguration.getProperty(DB_USER))
                        .replaceAll(CRAFTER_PASSWORD, studioConfiguration.getProperty(DB_PASSWORD)));
        try {
            sr.runScript(reader);
        } catch (RuntimeSqlException e) {
            logger.error("Error while running create DB script", e);
        }
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(studioConfiguration.getProperty(DB_INITIALIZER_ENABLED));
    }

    private String generateRandomPassword() {
        int passwordLength = Integer.parseInt(
                studioConfiguration.getProperty(DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_LENGTH));
        String passwordChars = studioConfiguration.getProperty(DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_CHARS);
        return RandomStringUtils.random(passwordLength, passwordChars);
    }

    private String getCreateDBScriptPath() {
        return studioConfiguration.getProperty(DB_INITIALIZER_CREATE_DB_SCRIPT_LOCATION);
    }

    private String getCreateSchemaScriptPath() {
        return studioConfiguration.getProperty(DB_INITIALIZER_CREATE_SCHEMA_SCRIPT_LOCATION);
    }

    private boolean isRandomAdminPasswordEnabled() {
        return Boolean.parseBoolean(studioConfiguration.getProperty(DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_ENABLED));
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
