/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v1.db;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

public class EmbededDbContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(EmbededDbContextListener.class);

    public void contextDestroyed(ServletContextEvent sce) {
      try {
        DriverManager.getConnection("jdbc:derby:;shutdown=true");
      }
      catch(SQLException err) {
          logger.error("error shutting down embedded database:",err);
      }
    }

    public void contextInitialized(ServletContextEvent sce) {

    } 
}
