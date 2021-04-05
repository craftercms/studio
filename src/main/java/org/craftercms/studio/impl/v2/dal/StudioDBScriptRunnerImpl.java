/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

public class StudioDBScriptRunnerImpl implements StudioDBScriptRunner {

    private final static Logger logger = LoggerFactory.getLogger(StudioDBScriptRunnerImpl.class);

    protected String delimiter;
    protected DataSource dataSource;

    @Override
    public void execute(String sql) {
        try (Reader reader = new StringReader(sql)) {
            Connection connection = dataSource.getConnection();
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setDelimiter(delimiter);
            scriptRunner.setStopOnError(true);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(reader);
        } catch (SQLException | IOException e) {
            logger.error("Error executing db script", e);
        }
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
