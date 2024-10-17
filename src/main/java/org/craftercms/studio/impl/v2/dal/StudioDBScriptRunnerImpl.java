/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.dal.StudioDBScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class StudioDBScriptRunnerImpl implements StudioDBScriptRunner {

    private final static Logger logger = LoggerFactory.getLogger(StudioDBScriptRunnerImpl.class);

    protected DataSource dataSource;
    protected int scriptLinesBufferSize;
    protected Connection connection = null;
    protected boolean autoCommit;

    protected StudioDBScriptRunnerImpl(DataSource dataSource, int scriptLinesBufferSize) {
        this.dataSource = dataSource;
        this.scriptLinesBufferSize = scriptLinesBufferSize;
    }

    protected void openConnection() {
        if (Objects.isNull(connection)) {
            try {
                connection = dataSource.getConnection();
                autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                logger.error("Failed to open a connection to the DB", e);
            }
        }
    }

    protected void closeConnection() {
        if (!Objects.isNull(connection)) {
            try {
                connection.setAutoCommit(autoCommit);
                connection.close();
            } catch (SQLException e) {
                logger.error("Failed to close the connection to the DB", e);
            }
            connection = null;
        }
    }

    @Override
    @LogExecutionTime
    public void execute(final Path sqlScriptPath, final boolean sendFullFile) throws SQLException, IOException {
        File sqlScriptFile = sqlScriptPath.toFile();
        try {
            openConnection();
            if (sendFullFile) {
                logger.debug("Executing full SQL script '{}'", sqlScriptPath);
                runSql(Files.readString(sqlScriptPath));
            } else {
                logger.debug("Executing partitioned SQL script '{}'", sqlScriptPath);
                executePartitioned(sqlScriptFile);
            }
            logger.debug("Committing the DB transaction after executing the script '{}'", sqlScriptPath);
            connection.commit();
        } catch (SQLException | IOException e) {
            logger.error("Failed to execute the DB script '{}'", sqlScriptFile.getAbsolutePath(), e);
            try {
                connection.rollback();
            } catch (SQLException e2) {
                logger.error("Failed to rollback the DB transaction", e2);
            }
            throw e;
        } finally {
            closeConnection();
        }
    }

    private void executePartitioned(final File sqlScriptFile) throws IOException, SQLException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(sqlScriptFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            boolean moreWork = true;
            while (moreWork) {
                for (int i = 0; i < scriptLinesBufferSize && moreWork; i++) {
                    line = bufferedReader.readLine();
                    if (Objects.nonNull(line)) {
                        sb.append(line).append(System.lineSeparator());
                    } else {
                        moreWork = false;
                    }
                }
                if (!sb.isEmpty()) {
                    logger.debug("Executing chunk of SQL script file '{}'", sqlScriptFile);
                    runSql(sb.toString());
                    sb.setLength(0);
                }
            }
        }
    }

    private void runSql(final String command) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setEscapeProcessing(false);
            statement.execute(command);
        }
    }
}
