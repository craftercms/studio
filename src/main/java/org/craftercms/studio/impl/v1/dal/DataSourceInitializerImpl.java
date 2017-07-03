/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.dal;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.MariaDB4jService;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.studio.api.v1.dal.DataSourceInitializer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.beans.factory.DisposableBean;

import java.io.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

public class DataSourceInitializerImpl implements DataSourceInitializer, DisposableBean {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceInitializerImpl.class);

    @Override
    public void initDataSource() {
        if (isEnabled()) {
            String scriptPath = getScriptPath();

            DB db = mariaDB4jService.getDB();
            Connection conn = null;
            try {
                Class.forName(studioConfiguration.getProperty(DB_DRIVER));
                conn = DriverManager.getConnection(studioConfiguration.getProperty(DB_INITIALIZER_URL));
            } catch (SQLException | ClassNotFoundException e) {
                logger.error("Error while getting connection to DB", e);
            }

            ScriptRunner sr = new ScriptRunner(conn);

            sr.setDelimiter(delimiter);
            sr.setStopOnError(true);
            sr.setLogWriter(null);
            InputStream is = getClass().getClassLoader().getResourceAsStream(scriptPath);
            Reader reader = new InputStreamReader(is);
            try {
                sr.runScript(reader);
            } catch (RuntimeSqlException e) {
                logger.error("Error while running init DB script", e);
            }
            try {
                conn.close();
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
                logger.error("Failed to unregister driver " + driver.getClass().getCanonicalName() + " on shutdown", e);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    private String getScriptPath() {
        return studioConfiguration.getProperty(DB_INITIALIZER_SCRIPT_LOCATION);
    }


    public String getDelimiter() { return delimiter; }
    public void setDelimiter(String delimiter) { this.delimiter = delimiter; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public MariaDB4jService getMariaDB4jService() { return mariaDB4jService; }
    public void setMariaDB4jService(MariaDB4jService mariaDB4jService) { this.mariaDB4jService = mariaDB4jService; }

    protected String delimiter;
    protected StudioConfiguration studioConfiguration;

    protected MariaDB4jService mariaDB4jService;


}
