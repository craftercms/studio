/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.upgrade;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.UpgradeManager;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.UpgradePipelineFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.*;

public class DefaultUpgradeManagerImpl implements UpgradeManager, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUpgradeManagerImpl.class);

    public static final String SQL_QUERY_META = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'crafter' AND table_name = '_meta' LIMIT 1";
    public static final String SQL_QUERY_VERSION = "select version from _meta";
    public static final String SQL_QUERY_GROUP = "SELECT count(*) FROM information_schema.tables WHERE table_schema = "
        + "'crafter' AND table_name = 'cstudio_group' LIMIT 1";
    public static final String SQL_QUERY_SITES_3_0_0 = "select site_id from cstudio_site where system = 0";
    public static final String SQL_QUERY_SITES = "select site_id from site where system = 0";

    protected String latestDbVersion;
    protected String latestSiteVersion;

    protected UpgradePipelineFactory dbPipelineFactory;
    protected UpgradePipelineFactory sitePipelineFactory;

    protected DataSource dataSource;
    protected ApplicationContext appContext;
    protected JdbcTemplate jdbcTemplate;
    protected DbIntegrityValidator integrityValidator;
    protected ContentRepository contentRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgradeSystem(String currentDbVersion) throws UpgradeException {
        UpgradePipeline pipeline = dbPipelineFactory.getPipeline(currentDbVersion);
        pipeline.execute(null);

        List<String> sites;
        if(currentDbVersion.equals(VERSION_3_0_0)) {
            sites = jdbcTemplate.queryForList(SQL_QUERY_SITES_3_0_0, String.class);
        } else {
            sites = jdbcTemplate.queryForList(SQL_QUERY_SITES, String.class);
        }

        for(String site : sites) {
            upgradeSite(site);
        }
    }

    @Override
    public void upgradeSite(final String site) throws UpgradeException {
        // check site version
        String currentSiteVersion = "3.0";
        if(contentRepository.contentExists(site, "/config/studio/studio_version.xml")) {
            // read the version from file ...
        }
        // get pipeline
        UpgradePipeline pipeline = sitePipelineFactory.getPipeline(currentSiteVersion);
        pipeline.execute(site);
        // update files?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentVersion() throws UpgradeNotSupportedException {
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

    /**
     * Obtains the current version and starts the upgrade process.
     * @throws UpgradeException if there is any error in the upgrade process
     * @throws EntitlementException if there is any validation error after the upgrade process
     */
    public void init() throws UpgradeException, EntitlementException {
        jdbcTemplate = new JdbcTemplate(dataSource);
        logger.info("Checking for pending upgrades");
        String currentVersion = getCurrentVersion();
        logger.info("Current version is {0}", currentVersion);
        if(currentVersion.equals(latestDbVersion)) {
            logger.info("Already at the latest versions, no upgrades are required");
        } else {
            logger.info("Starting upgradeSystem to version {0}", latestDbVersion);
            upgradeSystem(currentVersion);
        }
        try {
            integrityValidator.validate(dataSource.getConnection());
        } catch (SQLException e) {
            logger.error("Could not connect to database for integrity validation", e);
            throw new UpgradeException("Could not connect to database for integrity validation", e);
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    @Required
    public void setLatestDbVersion(final String latestDbVersion) {
        this.latestDbVersion = latestDbVersion;
    }

    @Required
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setIntegrityValidator(final DbIntegrityValidator integrityValidator) {
        this.integrityValidator = integrityValidator;
    }

    @Required
    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }


}
