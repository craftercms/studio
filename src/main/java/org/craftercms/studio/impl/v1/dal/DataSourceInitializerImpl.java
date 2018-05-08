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
 */

package org.craftercms.studio.impl.v1.dal;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.MariaDB4jService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.studio.api.v1.dal.DataSourceInitializer;
import org.craftercms.studio.api.v1.exception.DatabaseUpgradeUnsupportedVersionException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.beans.factory.DisposableBean;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_DRIVER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_CONFIGURE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_CREATE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_CHARS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_LENGTH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_UPGRADE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_URL;


public class DataSourceInitializerImpl implements DataSourceInitializer, DisposableBean {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceInitializerImpl.class);
    private final static String CURRENT_DB_VERSION = "3.0.11.2";
    private final static String DB_VERSION_3_0_0 = "3.0.0";
    private final static String DB_VERSION_2_5_X = "2.5.x";

    /**
     * Database queries
     */
    private final static String DB_QUERY_CHECK_SCHEMA_EXISTS =
            "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'crafter'";
    private final static String DB_QUERY_CHECK_META_TABLE_EXISTS =
            "SELECT * FROM information_schema.tables WHERE table_schema = 'crafter' AND table_name = '_meta' LIMIT 1";
    private final static String DB_QUERY_GET_META_TABLE_VERSION =
            "SELECT _meta.version FROM _meta LIMIT 1";
    private final static String DB_QUERY_CHECK_GROUP_TABLE_EXISTS =
            "SELECT * FROM information_schema.tables WHERE table_schema = 'crafter' AND table_name = 'cstudio_group' LIMIT 1";
    private final static String DB_QUERY_USE_CRAFTER = "use crafter";
    private final static String DB_QUERY_SET_ADMIN_PASSWORD =
            "UPDATE user SET password = '{password}' WHERE username = 'admin'";

    protected String delimiter;
    protected StudioConfiguration studioConfiguration;

    protected MariaDB4jService mariaDB4jService;

    @Override
    public void initDataSource() throws DatabaseUpgradeUnsupportedVersionException {
        if (isEnabled()) {
            String configureDbScriptPath = getConfigureDBScriptPath();
            String createDbScriptPath = getCreateDBScriptPath();

            logger.debug("Get MariaDB service");
            DB db = mariaDB4jService.getDB();
            Connection conn = null;
            Statement statement = null;
            ResultSet rs = null;
            String dbVersion = StringUtils.EMPTY;

            try {
                logger.debug("Get DB connection");
                Class.forName(studioConfiguration.getProperty(DB_DRIVER));
                conn = DriverManager.getConnection(studioConfiguration.getProperty(DB_INITIALIZER_URL));
            } catch (SQLException | ClassNotFoundException e) {
                logger.error("Error while getting connection to DB", e);
            }

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

            if (conn != null) {
                try {
                    logger.debug("Check if database schema already exists");
                    statement = conn.createStatement();

                    rs = statement.executeQuery(DB_QUERY_CHECK_SCHEMA_EXISTS);
                    if (rs.next()) {
                        logger.debug("Database already exists. Determine version of database");
                        rs.close();
                        logger.debug("Check if _meta table exists.");
                        rs = statement.executeQuery(DB_QUERY_CHECK_META_TABLE_EXISTS);
                        if (rs.next()) {
                            logger.debug("_meta table exists.");
                            statement.execute(DB_QUERY_USE_CRAFTER);
                            rs.close();
                            logger.debug("Get version from _meta table.");
                            rs = statement.executeQuery(DB_QUERY_GET_META_TABLE_VERSION);
                            if (rs.next()) {
                                dbVersion = rs.getString(1);
                            } else {
                                // TODO: DB: Error ?
                                throw new DatabaseUpgradeUnsupportedVersionException(
                                        "Could not determine database version from _meta table");
                            }
                        } else {
                            logger.debug("Check if group table exists.");
                            rs = statement.executeQuery(DB_QUERY_CHECK_GROUP_TABLE_EXISTS);
                            if (rs.next()) {
                                // DB version 3.0.0
                                logger.debug("Detabase version is 3.0.0");
                                dbVersion = DB_VERSION_3_0_0;
                            } else {
                                // DB version 2.5.x
                                logger.debug("Detabase version is 2.5.X");
                                dbVersion = DB_VERSION_2_5_X;
                            }
                        }
                        switch (dbVersion) {
                            case CURRENT_DB_VERSION:
                                // DB up to date - nothing to upgrade
                                logger.info("Database is up to date.");
                                break;
                            case DB_VERSION_2_5_X:
                                // TODO: DB: Migration not supported yet
                                throw new DatabaseUpgradeUnsupportedVersionException(
                                        "Automated migration from 2.5.x DB is not supported yet.");
                            default:
                                logger.info("Database version is " + dbVersion + ", required version is " +
                                        CURRENT_DB_VERSION);
                                String upgradeScriptPath = getUpgradeDBScriptPath();
                                upgradeScriptPath = upgradeScriptPath.replace("{version}", dbVersion);
                                logger.info("Upgrading database from script " + upgradeScriptPath);
                                sr = new ScriptRunner(conn);

                                sr.setDelimiter(delimiter);
                                sr.setStopOnError(true);
                                sr.setLogWriter(null);
                                is = getClass().getClassLoader().getResourceAsStream(upgradeScriptPath);
                                reader = new InputStreamReader(is);
                                try {
                                    sr.runScript(reader);
                                } catch (RuntimeSqlException e) {
                                    logger.error("Error while running upgrade DB script", e);
                                }
                                break;
                        }
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
                        } catch (RuntimeSqlException e) {
                            logger.error("Error while running create DB script", e);
                        }
                    }

                    rs.close();
                    statement.close();
                } catch (SQLException e) {
                    logger.error("Error while initializing database", e);
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException e) {
                        logger.error("Error while closing database resources", e);
                    }
                }
            }


            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error while closing connection with database", e);
            }
        }
    }

    public boolean isEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(DB_INITIALIZER_ENABLED));
        return toReturn;
    }

    public void shutdown() {
        if (mariaDB4jService != null) {
            DB db = mariaDB4jService.getDB();
            if (db != null) {
                try {
                    db.stop();
                } catch (ManagedProcessException e) {
                    logger.error("Failed to stop database", e);
                }
            }
            try {
                mariaDB4jService.stop();
            } catch (ManagedProcessException e) {
                logger.error("Failed to stop database", e);
            }
        }
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                logger.error("Failed to unregister driver " + driver.getClass().getCanonicalName() +
                        " on shutdown", e);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
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

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public MariaDB4jService getMariaDB4jService() {
        return mariaDB4jService;
    }

    public void setMariaDB4jService(MariaDB4jService mariaDB4jService) {
        this.mariaDB4jService = mariaDB4jService;
    }
}
