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

package org.craftercms.studio.impl.v2.upgrade.providers;

import javax.sql.DataSource;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.VersionProvider;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.VERSION_3_0_0;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;

/**
 * Implementation of {@link VersionProvider} for the database.
 * @author joseross
 */
public class DbVersionProvider implements VersionProvider {

    private static final Logger logger = LoggerFactory.getLogger(DbVersionProvider.class);

    public static final String SCHEMA = "{schema}";
    public static final String SQL_QUERY_META = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'{schema}' AND table_name = '_meta' LIMIT 1";
    public static final String SQL_QUERY_VERSION = "select version from _meta";
    public static final String SQL_QUERY_GROUP = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'{schema}' AND table_name = 'cstudio_group' LIMIT 1";

    protected DataSource dataSource;
    protected StudioConfiguration studioConfiguration;

    @Required
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentVersion() throws UpgradeException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        logger.debug("Check if _meta table exists.");
        int count = jdbcTemplate.queryForObject(
                SQL_QUERY_META.replace(SCHEMA, studioConfiguration.getProperty(DB_SCHEMA)), Integer.class);
        if(count != 0) {
            logger.debug("_meta table exists.");
            logger.debug("Get version from _meta table.");
            return jdbcTemplate.queryForObject(SQL_QUERY_VERSION, String.class);

        } else {
            logger.debug("Check if group table exists.");
            count = jdbcTemplate.queryForObject(
                    SQL_QUERY_GROUP.replace(SCHEMA, studioConfiguration.getProperty(DB_SCHEMA)), Integer.class);
            if(count != 0) {
                logger.debug("Database version is 3.0.0");
                return VERSION_3_0_0;
            } else {
                throw new UpgradeNotSupportedException("Automated migration from 2.5.x DB is not supported yet.");
            }
        }
    }

}
