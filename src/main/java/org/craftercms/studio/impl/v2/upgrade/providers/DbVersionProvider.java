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

package org.craftercms.studio.impl.v2.upgrade.providers;

import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.exception.UpgradeNotSupportedException;
import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.commons.upgrade.impl.providers.AbstractVersionProvider;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.ConstructorProperties;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.VERSION_3_0_0;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.VersionProvider} for the database.
 * @author joseross
 */
public class DbVersionProvider extends AbstractVersionProvider<String> {

    private static final Logger logger = LoggerFactory.getLogger(DbVersionProvider.class);

    public static final String SCHEMA = "{schema}";
    public static final String SQL_QUERY_META = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'{schema}' AND table_name = '_meta' LIMIT 1";
    public static final String SQL_QUERY_VERSION = "select version from _meta";
    public static final String SQL_QUERY_GROUP = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'{schema}' AND table_name = 'cstudio_group' LIMIT 1";
    public static final String SQL_UPDATE_VERSION = "UPDATE _meta SET version = ?";

    protected StudioConfiguration studioConfiguration;

    @ConstructorProperties({"studioConfiguration"})
    public DbVersionProvider(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    protected String doGetVersion(UpgradeContext<String> context) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(((StudioUpgradeContext) context).getDataSource());
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

    @Override
    protected void doSetVersion(UpgradeContext<String> context, String nextVersion) throws UpgradeException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(((StudioUpgradeContext) context).getDataSource());
        try {
            int updated = jdbcTemplate.update(SQL_UPDATE_VERSION, nextVersion);
            if (updated != 1) {
                throw new UpgradeException("Error updating the db version");
            }
            logger.info("Database version updated to {0}", nextVersion);
        } catch (Exception e) {
            throw new UpgradeException("Error updating the db version", e);
        }
    }

}
