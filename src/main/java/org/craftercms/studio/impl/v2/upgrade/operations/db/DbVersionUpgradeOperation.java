/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.upgrade.operations.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that updates the
 * version in the database.
 *
 * @author joseross
 * @since 3.1.1
 */
public class DbVersionUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(DbVersionUpgradeOperation.class);

    private static final String DEFAULT_SQL_COMMAND = "UPDATE _meta SET version = ?";

    protected String sqlCommand = DEFAULT_SQL_COMMAND;

    public void setSqlCommand(final String sqlCommand) {
        this.sqlCommand = sqlCommand;
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        try(Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
            statement.setString(1, nextVersion);
            int updated = statement.executeUpdate();
            connection.commit();
            if (updated != 1) {
                throw new UpgradeException("Error updating the db version");
            }
            logger.info("Database version updated to {0}", nextVersion);
        } catch (SQLException e) {
            throw new UpgradeException("Error updating the db version", e);
        }
    }

}
