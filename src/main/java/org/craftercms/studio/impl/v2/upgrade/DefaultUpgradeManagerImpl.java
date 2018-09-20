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

import java.io.InputStream;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.YamlConfiguration;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.UpgradeContext;
import org.craftercms.studio.api.v2.upgrade.UpgradeManager;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
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

    protected Resource configurationFile;
    protected String latestVersion;

    protected DataSource dataSource;
    protected ApplicationContext appContext;
    protected JdbcTemplate jdbcTemplate;
    protected DbIntegrityValidator integrityValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes,unchecked")
    public UpgradePipeline buildUpgradePipeline(String currentVersion) throws UpgradeException {
        List<UpgradeOperation> operations = new LinkedList<>();
        HierarchicalConfiguration config = loadUpgradeConfiguration();
        List<HierarchicalConfiguration> pipeline = config.configurationsAt(CONFIG_KEY_PIPELINE);

        boolean versionFound = false;
        for(HierarchicalConfiguration version : pipeline) {
            if(!version.getString(CONFIG_KEY_VERSION).equals(currentVersion)) {
                if(versionFound) {
                    List<HierarchicalConfiguration> operationsConfig = version.configurationsAt(CONFIG_KEY_OPERATIONS);
                    operationsConfig.forEach(operationConfig -> {
                        UpgradeOperation operation =
                            appContext.getBean(operationConfig.getString(CONFIG_KEY_TYPE), UpgradeOperation.class);
                        operation.init(operationConfig);
                        operations.add(operation);
                    });
                }
            } else {
                versionFound = true;
            }
        }
        return new DefaultUpgradePipelineImpl(operations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HierarchicalConfiguration loadUpgradeConfiguration() throws UpgradeException {
        YamlConfiguration configuration = new YamlConfiguration();
        try (InputStream is = configurationFile.getInputStream()) {
            configuration.read(is);
        } catch (Exception e) {
            throw  new UpgradeException("Error reading configuration file", e);
        }
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpgradeContext buildUpgradeContext(String currentVersion) {
        DefaultUpgradeContext context = appContext.getBean(DefaultUpgradeContext.class);
        context.setCurrentVersion(currentVersion);
        context.setTargetVersion(latestVersion);
        List<String> sites;
        if(currentVersion.equals(VERSION_3_0_0)) {
            sites = jdbcTemplate.queryForList(SQL_QUERY_SITES_3_0_0, String.class);
        } else {
            sites = jdbcTemplate.queryForList(SQL_QUERY_SITES, String.class);
        }
        context.setSites(sites);
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgrade(String currentVersion) throws UpgradeException {
        UpgradePipeline pipeline = buildUpgradePipeline(currentVersion);
        pipeline.execute(buildUpgradeContext(currentVersion));
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
        if(currentVersion.equals(latestVersion)) {
            logger.info("Already at the latest versions, no upgrades are required");
        } else {
            logger.info("Starting upgrade to version {0}", latestVersion);
            upgrade(currentVersion);
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
    public void setConfigurationFile(final Resource configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Required
    public void setLatestVersion(final String latestVersion) {
        this.latestVersion = latestVersion;
    }

    @Required
    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setIntegrityValidator(final DbIntegrityValidator integrityValidator) {
        this.integrityValidator = integrityValidator;
    }

}
