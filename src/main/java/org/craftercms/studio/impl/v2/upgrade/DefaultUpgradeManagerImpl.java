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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.YamlConfiguration;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.api.v2.upgrade.UpgradeManager;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.UpgradePipelineFactory;
import org.craftercms.studio.api.v2.upgrade.VersionProvider;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.*;

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

    /**
     * The name of the sandbox branch.
     */
    protected String siteSandboxBranch;

    /**
     * The name of the temporary branch used for upgrades.
     */
    protected String siteUpgradeBranch;

    /**
     * Message for the merge commit after upgrading.
     */
    protected String commitMessage;

    protected VersionProvider dbVersionProvider;
    protected UpgradePipelineFactory dbPipelineFactory;

    protected UpgradePipelineFactory bpPipelineFactory;

    protected Resource configurationFile;

    protected DataSource dataSource;
    protected ApplicationContext appContext;
    protected DbIntegrityValidator integrityValidator;
    protected ContentRepository contentRepository;
    protected StudioConfiguration studioConfiguration;
    protected SecurityProvider securityProvider;
    protected ServicesConfig servicesConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgradeSystem() throws UpgradeException {
        logger.info("Starting upgrade for the system");

        String currentDbVersion = dbVersionProvider.getCurrentVersion();
        UpgradePipeline pipeline = dbPipelineFactory.getPipeline(dbVersionProvider);
        pipeline.execute();

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
    @SuppressWarnings("unchecked")
    public void upgradeSite(final String site) throws UpgradeException {
        logger.info("Starting upgrade for site {0}", site);
        GitContentRepositoryHelper helper = new GitContentRepositoryHelper(studioConfiguration, securityProvider,
            servicesConfig);
        Repository repository = helper.getRepository(site, GitRepositories.SANDBOX);
        Git git = new Git(repository);
        try {
            boolean doMerge;
            logger.debug("Creating temporary branch {0} for site {1}", siteUpgradeBranch, site);
            git.branchCreate().setName(siteUpgradeBranch).call();
            logger.debug("Checking out temporary branch");
            git.checkout().setName(siteUpgradeBranch).call();

            VersionProvider versionProvider =
                (VersionProvider)appContext.getBean("fileVersionProvider", site, siteVersionFilePath);
            UpgradePipelineFactory pipelineFactory =
                (UpgradePipelineFactory)appContext.getBean("sitePipelineFactory");
            UpgradePipeline pipeline = pipelineFactory.getPipeline(versionProvider);
            doMerge = !pipeline.isEmpty();
            pipeline.execute(site);

            HierarchicalConfiguration config = loadUpgradeConfiguration();
            List<HierarchicalConfiguration> managedFiles = config.childConfigurationsAt(CONFIG_KEY_CONFIGURATIONS);

            for (HierarchicalConfiguration configFile : managedFiles) {
                versionProvider = (VersionProvider) appContext.getBean("fileVersionProvider", site,
                    configFile.getString(CONFIG_KEY_PATH));
                pipelineFactory = (UpgradePipelineFactory) appContext.getBean("filePipelineFactory",
                    configFile.getRootElementName() + CONFIG_PIPELINE_SUFFIX);
                pipeline = pipelineFactory.getPipeline(versionProvider);
                doMerge = doMerge || !pipeline.isEmpty();
                pipeline.execute(site);
            }

            logger.debug("Checking out sandbox branch");
            git.checkout().setName(siteSandboxBranch).call();
            if(doMerge) {
                logger.debug("Merging changes from upgrade branch");
                git.merge()
                    .include(repository.findRef(siteUpgradeBranch))
                    .setMessage(commitMessage)
                    .setCommit(true)
                    .call();
            }
            logger.debug("Removing temporary branch");
            git.branchDelete().setBranchNames(siteUpgradeBranch).call();
        } catch (GitAPIException | IOException e) {
            try {
                logger.debug("Checking out sandbox branch");
                git.checkout().setName(siteSandboxBranch).call();
            } catch (GitAPIException er) {
                logger.error("Error cleaning up repo for site " + site, e);
            }

            logger.error("Error branching or merging upgrade branch for site " + site, e);
            throw new UpgradeException("Error branching or merging upgrade branch for site " + site, e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgradeBlueprints() throws UpgradeException {
        logger.info("Starting upgrade for the blueprints");

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
        upgradeSystem();

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
    public void setSiteSandboxBranch(final String siteSandboxBranch) {
        this.siteSandboxBranch = siteSandboxBranch;
    }

    @Required
    public void setSiteUpgradeBranch(final String siteUpgradeBranch) {
        this.siteUpgradeBranch = siteUpgradeBranch;
    }

    @Required
    public void setCommitMessage(final String commitMessage) {
        this.commitMessage = commitMessage;
    }

    @Required
    public void setBpPipelineFactory(final UpgradePipelineFactory bpPipelineFactory) {
        this.bpPipelineFactory = bpPipelineFactory;
    }

    @Required
    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Required
    public void setSecurityProvider(final SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Required
    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

}
