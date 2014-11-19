/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.util;

import java.sql.Connection;
import java.sql.SQLException;

public class DaoServiceUtil {

    public static int setReadUncomittedTxIsolationLevel(Connection connection) {
        try {
            int oldval = connection.getTransactionIsolation();
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return oldval;
        } catch (SQLException e) {
            return -1;
        }
    }

    public static int setTransactionIsolationLevel(Connection connection, int isolationLevel) {
        try {
            int oldval = connection.getTransactionIsolation();
            connection.setTransactionIsolation(isolationLevel);
            return oldval;
        } catch (SQLException e) {
            return -1;
        }
    }
}
