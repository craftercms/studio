package org.craftercms.studio.impl.v2.upgrade;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.YamlConfiguration;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeUnsupportedException;
import org.craftercms.studio.api.v2.upgrade.UpgradeManager;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.Upgrader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

public class DefaultUpgradeManagerImpl implements UpgradeManager, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUpgradeManagerImpl.class);

    public static final String VERSION_3_0_0 = "3.0.0";
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

    @SuppressWarnings("rawtypes,unchecked")
    protected UpgradePipeline loadUpgradePipeline(String currentVersion) throws UpgradeException {
        List<Upgrader> upgraders = new LinkedList<>();
        HierarchicalConfiguration upgradeConfiguration = loadUpgradeConfiguration();
        List<HierarchicalConfiguration> pipelineConfigurations = upgradeConfiguration.configurationsAt("pipeline");

        boolean versionFound = false;
        for(HierarchicalConfiguration versionConfig : pipelineConfigurations) {
            if(!versionConfig.getString("version").equals(currentVersion)) {
                if(versionFound) {
                    List<HierarchicalConfiguration> upgradersConfig = versionConfig.configurationsAt("upgraders");
                    upgradersConfig.forEach(upgraderConfig -> {
                        Upgrader upgrader = appContext.getBean(upgraderConfig.getString("type"), Upgrader.class);
                        upgrader.init(upgraderConfig);
                        upgraders.add(upgrader);
                    });
                }
            } else {
                versionFound = true;
            }
        }

        return new DefaultUpgradePipelineImpl(upgraders);
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

    public void upgrade(String currentVersion) throws UpgradeException {
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
        UpgradePipeline pipeline = loadUpgradePipeline(currentVersion);
        pipeline.execute(context);
    }

    public String getCurrentVersion() throws UpgradeUnsupportedException {
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
                throw new UpgradeUnsupportedException(
                    "Automated migration from 2.5.x DB is not supported yet.");
            }
        }
    }

    public void init() throws UpgradeException {
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

}
