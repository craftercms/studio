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
import java.util.LinkedList;
import java.util.List;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.MariaDB4jService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.exception.DatabaseUpgradeUnsupportedVersionException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.DataSourceInitializer;
import org.craftercms.studio.api.v2.upgrade.RepositoryUpgrade;
import org.springframework.context.ApplicationContextException;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_DRIVER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_CONFIGURE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_CREATE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_CHARS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_RANDOM_ADMIN_PASSWORD_LENGTH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_UPGRADE_DB_SCRIPT_LOCATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.DB_INITIALIZER_URL;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.*;


public class DataSourceInitializerImpl implements DataSourceInitializer {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceInitializerImpl.class);

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

    private final static String DB_QUERY_GET_ALL_SITES = "SELECT site_id FROM site WHERE system = 0";

    protected String delimiter;
    protected StudioConfiguration studioConfiguration;
    protected ContentRepository contentRepository;

    protected MariaDB4jService mariaDB4jService;

    protected DbIntegrityValidator integrityValidator;

    protected List<RepositoryUpgrade> repositoryUpgrades;

    @Override
    public void initDataSource()  throws DatabaseUpgradeUnsupportedVersionException, EntitlementException {
        if (isEnabled()) {
            String configureDbScriptPath = getConfigureDBScriptPath();
            String createDbScriptPath = getCreateDBScriptPath();

            logger.debug("Get MariaDB service");
            DB db = mariaDB4jService.getDB();
            Connection conn = null;
            Statement statement = null;
            ResultSet rs = null;
            String dbVersion = StringUtils.EMPTY;
            String initialDbVersion = StringUtils.EMPTY;

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

                        initialDbVersion = dbVersion;

                        switch (dbVersion) {
                            case CURRENT_DB_VERSION:
                                // DB up to date - nothing to upgrade
                                logger.info("Database is up to date.");
                                // Validate database against license being used
                                integrityValidator.validate(conn);
                                break;
                            case DB_VERSION_2_5_X:
                                // TODO: DB: Migration not supported yet
                                throw new DatabaseUpgradeUnsupportedVersionException(
                                        "Automated migration from 2.5.x DB is not supported yet.");
                            default:
                                String upgradeScriptPath = StringUtils.EMPTY;
                                while (!dbVersion.equals(CURRENT_DB_VERSION)) {
                                    logger.info("Database version is " + dbVersion + ", required version is " +
                                            CURRENT_DB_VERSION);
                                    upgradeScriptPath = getUpgradeDBScriptPath();
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
                                        throw new ApplicationContextException("Error while running upgrade DB script "
                                                + upgradeScriptPath );
                                    }
                                    statement = conn.createStatement();
                                    rs = statement.executeQuery(DB_QUERY_GET_META_TABLE_VERSION);
                                    if (rs.next()) {
                                        dbVersion = rs.getString(1);
                                    } else {
                                        // TODO: DB: Error ?
                                        throw new DatabaseUpgradeUnsupportedVersionException(
                                                "Could not determine database version from _meta table");
                                    }
                                }

                                statement = conn.createStatement();
                                rs = statement.executeQuery(DB_QUERY_GET_ALL_SITES);
                                List<String> sites = new LinkedList<>();
                                while(rs.next()) {
                                    sites.add(rs.getString(1));
                                }

                                runRepositoryUpgrades(sites, initialDbVersion);

                                break;
                        }

                        try {
                            integrityValidator.store(conn);
                        } catch (RuntimeSqlException e) {
                            logger.error("Integrity validator error after running checkRepo DB scripts", e);
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

                            integrityValidator.store(conn);
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

    private void runRepositoryUpgrades(final List<String> sites, String currentVersion) {
        logger.info("Starting content repository upgrades");
        repositoryUpgrades.forEach(upgrade -> {
            upgrade.checkGlobalRepo(currentVersion);
            sites.forEach(site -> upgrade.checkRepo(site, currentVersion));
        });
        logger.info("Content repository upgrades completed");
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

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setIntegrityValidator(final DbIntegrityValidator integrityValidator) {
        this.integrityValidator = integrityValidator;
    }

    public void setRepositoryUpgrades(final List<RepositoryUpgrade> repositoryUpgrades) {
        this.repositoryUpgrades = repositoryUpgrades;
    }

    public MariaDB4jService getMariaDB4jService() {
        return mariaDB4jService;
    }

    public void setMariaDB4jService(MariaDB4jService mariaDB4jService) {
        this.mariaDB4jService = mariaDB4jService;
    }
}
