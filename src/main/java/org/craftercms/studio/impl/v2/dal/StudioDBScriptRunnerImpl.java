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

import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.StudioDBScriptRunner;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class StudioDBScriptRunnerImpl implements StudioDBScriptRunner {

    private final static Logger logger = LoggerFactory.getLogger(StudioDBScriptRunnerImpl.class);

    protected String delimiter;
    protected DataSource dataSource;
    protected int scriptLinesBufferSize = 10000;
    protected Connection connection = null;
	protected boolean autoCommit;

    protected StudioDBScriptRunnerImpl(String delimiter, DataSource dataSource, int scriptLinesBufferSize) {
        this.delimiter = delimiter;
        this.dataSource = dataSource;
        this.scriptLinesBufferSize = scriptLinesBufferSize;
    }

    protected void openConnection() {
        if (Objects.isNull(connection)) {
            try {
                connection = dataSource.getConnection();
				autoCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
            } catch (SQLException throwables) {
                logger.error("Failed to open connection with DB", throwables);
            }
        }
    }

    protected void closeConnection() {
        if (!Objects.isNull(connection)) {
            try {
				connection.setAutoCommit(autoCommit);
                connection.close();
            } catch (SQLException throwables) {
                logger.error("Failed to close connection with DB", throwables);
            }
            connection = null;
        }
    }

    @Override
    public void execute(File sqlScriptFile) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(sqlScriptFile))) {
			openConnection();

            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setAutoCommit(false);
            scriptRunner.setDelimiter(delimiter);
            scriptRunner.setStopOnError(true);
            scriptRunner.setLogWriter(null);

            StringBuilder sb = new StringBuilder();
            String line = null;
            boolean moreWork = true;
            while (moreWork) {
                for (int i = 0; i < scriptLinesBufferSize && moreWork; i++) {
                    line = bufferedReader.readLine();
                    if (Objects.nonNull(line)) {
                        sb.append(line).append("\n");
                    } else {
                        moreWork = false;
                    }
                }

                if (sb.length() > 0) {
                    scriptRunner.runScript(new StringReader(sb.toString()));
                    sb.setLength(0);
                }
            }

            connection.commit();
        } catch (SQLException | IOException e) {
			logger.error("Error executing DB script", e);
			try {
				connection.rollback();
			} catch (SQLException throwables) {
				logger.error("Failed to rollback after error when running DB script", throwables);
			}
		} finally {
			closeConnection();
		}
    }

}
