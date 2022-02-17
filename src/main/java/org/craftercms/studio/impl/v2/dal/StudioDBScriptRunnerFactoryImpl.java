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

import org.craftercms.studio.api.v2.dal.StudioDBScriptRunner;
import org.craftercms.studio.api.v2.dal.StudioDBScriptRunnerFactory;

import javax.sql.DataSource;

public class StudioDBScriptRunnerFactoryImpl implements StudioDBScriptRunnerFactory {

    protected String delimiter;
    protected DataSource dataSource;
    protected int scriptLinesBufferSize = 10000;

    @Override
    public StudioDBScriptRunner getDBScriptRunner() {
        return new StudioDBScriptRunnerImpl(delimiter, dataSource, scriptLinesBufferSize);
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

    public int getScriptLinesBufferSize() {
        return scriptLinesBufferSize;
    }

    public void setScriptLinesBufferSize(int scriptLinesBufferSize) {
        this.scriptLinesBufferSize = scriptLinesBufferSize;
    }
}
