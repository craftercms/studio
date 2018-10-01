package org.craftercms.studio.impl.v2.upgrade.providers;

import javax.sql.DataSource;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.VERSION_3_0_0;

public class DbVersionProvider implements VersionProvider {

    private static final Logger logger = LoggerFactory.getLogger(DbVersionProvider.class);

    public static final String SQL_QUERY_META = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'crafter' AND table_name = '_meta' LIMIT 1";
    public static final String SQL_QUERY_VERSION = "select version from _meta";
    public static final String SQL_QUERY_GROUP = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'crafter' AND table_name = 'cstudio_group' LIMIT 1";

    protected DataSource dataSource;

    @Required
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getCurrentVersion() throws UpgradeException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        logger.debug("Check if _meta table exists.");
        int count = jdbcTemplate.queryForObject(SQL_QUERY_META, Integer.class);
        if(count != 0) {
            logger.debug("_meta table exists.");
            logger.debug("Get version from _meta table.");
            return jdbcTemplate.queryForObject(SQL_QUERY_VERSION, String.class);

        } else {
            logger.debug("Check if group table exists.");
            count = jdbcTemplate.queryForObject(SQL_QUERY_GROUP, Integer.class);
            if(count != 0) {
                logger.debug("Database version is 3.0.0");
                return VERSION_3_0_0;
            } else {
                throw new UpgradeNotSupportedException("Automated migration from 2.5.x DB is not supported yet.");
            }
        }
    }

}
