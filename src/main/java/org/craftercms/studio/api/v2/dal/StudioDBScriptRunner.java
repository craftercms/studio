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

package org.craftercms.studio.api.v2.dal;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Interface for running SQL scripts in the Studio database.
 */
public interface StudioDBScriptRunner {
    /**
     * Execute the given SQL script file.
     *
     * @param sqlScriptPath Path to the SQL script file to execute
     * @param sendFullFile  if true, the whole script will be sent to the DB, otherwise it will be partitioned into chunks
     * @throws SQLException if an error occurs while executing the script
     * @throws IOException  if an error occurs while reading the script file
     */
    void execute(Path sqlScriptPath, boolean sendFullFile) throws SQLException, IOException;

    /**
     * Execute the given SQL script file.
     *
     * @param sqlScriptPath Path to the SQL script file to execute
     * @throws SQLException if an error occurs while executing the script
     * @throws IOException  if an error occurs while reading the script file
     */
    default void execute(Path sqlScriptPath) throws SQLException, IOException {
        execute(sqlScriptPath, false);
    }
}
