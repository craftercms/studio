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
import java.util.List;
import javax.sql.DataSource;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.YamlConfiguration;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeManager;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.UpgradePipelineFactory;
import org.craftercms.studio.api.v2.upgrade.VersionProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.CONFIG_KEY_CONFIGURATIONS;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.CONFIG_KEY_PATH;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.VERSION_3_0_0;

/**
 * Default implementation for {@link UpgradeManager}.
 * @author joseross
 */
public class DefaultUpgradeManagerImpl implements UpgradeManager, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUpgradeManagerImpl.class);

    public static final String SQL_QUERY_SITES_3_0_0 = "select site_id from cstudio_site where system = 0";
    public static final String SQL_QUERY_SITES = "select site_id from site where system = 0";

    public static final String CONFIG_PIPELINE_SUFFIX = ".pipeline";

    /**
     * The git path of the version file.
     */
    protected String siteVersionFilePath;

    protected VersionProvider dbVersionProvider;
    protected UpgradePipelineFactory dbPipelineFactory;

    protected UpgradePipelineFactory bpPipelineFactory;

    protected Resource configurationFile;

    protected DataSource dataSource;
    protected ApplicationContext appContext;
    protected DbIntegrityValidator integrityValidator;
    protected ContentRepository contentRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgradeDatabaseAndConfiguration() throws UpgradeException {
        logger.info("Checking upgrades for the database and configuration");

        UpgradePipeline pipeline = dbPipelineFactory.getPipeline(dbVersionProvider);
        pipeline.execute();

    }

    protected VersionProvider getVersionProvider(String name, Object... args) {
        return (VersionProvider) appContext.getBean(name, args);
    }

    protected UpgradePipeline getPipeline(VersionProvider versionProvider, String factoryName, Object... args)
        throws UpgradeException {
        UpgradePipelineFactory pipelineFactory =
            (UpgradePipelineFactory) appContext.getBean(factoryName, args);
        return pipelineFactory.getPipeline(versionProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void upgradeSite(final String site) throws UpgradeException {
        logger.info("Checking upgrades for site {0}", site);

        VersionProvider versionProvider = getVersionProvider("siteVersionProvider", site, siteVersionFilePath);
        UpgradePipeline pipeline = getPipeline(versionProvider, "sitePipelineFactory");

        pipeline.execute(site);

        upgradeSiteConfiguration(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void upgradeSiteConfiguration(final String site) throws UpgradeException {
        logger.info("Checking upgrades for configuration in site {0}", site);

        HierarchicalConfiguration config = loadUpgradeConfiguration();
        List<HierarchicalConfiguration> managedFiles = config.childConfigurationsAt(CONFIG_KEY_CONFIGURATIONS);

        for (HierarchicalConfiguration configFile : managedFiles) {
            String path = configFile.getString(CONFIG_KEY_PATH);
            logger.info("Checking upgrades for file {0}", path);

            VersionProvider versionProvider = getVersionProvider("fileVersionProvider", site, path);
            UpgradePipeline pipeline = getPipeline(versionProvider, "filePipelineFactory",
                configFile.getRootElementName() + CONFIG_PIPELINE_SUFFIX);

            pipeline.execute(site);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgradeExistingSites() throws UpgradeException {
        String currentDbVersion = dbVersionProvider.getCurrentVersion();

        List<String> sites;
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        if(currentDbVersion.equals(VERSION_3_0_0)) {
            sites = jdbcTemplate.queryForList(SQL_QUERY_SITES_3_0_0, String.class);
        } else {
            sites = jdbcTemplate.queryForList(SQL_QUERY_SITES, String.class);
        }

        for(String site : sites) {
            upgradeSite(site);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgradeBlueprints() throws UpgradeException {
        logger.info("Checking upgrades for the blueprints");

        // The version is fixed for now so bp are always updates, in the future this should be replaced with a proper
        // version provider
        UpgradePipeline pipeline = bpPipelineFactory.getPipeline(() -> VERSION_3_0_0);
        pipeline.execute();
    }

    /**
     * Obtains the current version and starts the upgrade process.
     * @throws UpgradeException if there is any error in the upgrade process
     * @throws EntitlementException if there is any validation error after the upgrade process
     */
    public void init() throws UpgradeException, EntitlementException {

        upgradeBlueprints();
        upgradeDatabaseAndConfiguration();
        upgradeExistingSites();

        try {
            integrityValidator.validate(dataSource.getConnection());
        } catch (SQLException e) {
            logger.error("Could not connect to database for integrity validation", e);
            throw new UpgradeException("Could not connect to database for integrity validation", e);
        }
    }

    protected HierarchicalConfiguration loadUpgradeConfiguration() throws UpgradeException {
        YamlConfiguration configuration = new YamlConfiguration();
        try (InputStream is = configurationFile.getInputStream()) {
            configuration.read(is);
        } catch (Exception e) {
            throw  new UpgradeException("Error reading configuration file", e);
        }
        return configuration;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
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

    @Required
    public void setDbPipelineFactory(final UpgradePipelineFactory dbPipelineFactory) {
        this.dbPipelineFactory = dbPipelineFactory;
    }

    @Required
    public void setDbVersionProvider(final VersionProvider dbVersionProvider) {
        this.dbVersionProvider = dbVersionProvider;
    }

    @Required
    public void setConfigurationFile(final Resource configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Required
    public void setSiteVersionFilePath(final String siteVersionFilePath) {
        this.siteVersionFilePath = siteVersionFilePath;
    }

    @Required
    public void setBpPipelineFactory(final UpgradePipelineFactory bpPipelineFactory) {
        this.bpPipelineFactory = bpPipelineFactory;
    }

}
