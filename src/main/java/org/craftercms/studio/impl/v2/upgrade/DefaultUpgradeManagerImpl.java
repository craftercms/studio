package org.craftercms.studio.impl.v2.upgrade;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
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

    protected Resource configurationFile;
    protected String latestVersion;

    protected DataSource dataSource;
    protected ContentRepository contentRepository;

    protected ApplicationContext appContext;

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
        YAMLConfiguration configuration = new YAMLConfiguration();
        try (InputStream is = configurationFile.getInputStream()) {
            configuration.read(is);
        } catch (Exception e) {
            throw  new UpgradeException("Error reading configuration file", e);
        }
        return configuration;
    }

    public void upgrade(String currentVersion) throws UpgradeException {
        DefaultUpgradeContext context = new DefaultUpgradeContext();
        context.setCurrentVersion(currentVersion);
        context.setTargetVersion(latestVersion);
        context.setDataSource(dataSource);
        context.setContentRepository(contentRepository);
        context.setSites(new JdbcTemplate(dataSource).queryForList("select site_id from site where system = 0", String.class));
        UpgradePipeline pipeline = loadUpgradePipeline(currentVersion);
        pipeline.execute(context);
    }

    public String getCurrentVersion() {
        return new JdbcTemplate(dataSource).queryForObject("select version from _meta", String.class);
    }

    public void init() throws UpgradeException {
        logger.info("Checking for pending upgrades");
        String currentVersion = getCurrentVersion();
        logger.info("Current version is {0}", currentVersion);
        // TODO: If version is 2.x throw exception
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

    @Required
    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

}
