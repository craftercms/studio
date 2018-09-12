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

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.impl.v2.dal.RepositoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResource;

import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.GLOBAL_REPO_PATH;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.DB_VERSION_3_0_X;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.DB_VERSION_3_1_X;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.RepositoryUpgrade} that upgrades the
 * global configuration files.
 * @author joseross
 */
public class GlobalConfigurationUpgrade extends AbstractRepositoryUpgrade {

    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigurationUpgrade.class);

    /**
     * Location of the global configuration files.
     */
    protected String globalConfigurationPath;

    /**
     * Location of the global configuration files to bootstrap.
     */
    protected Resource globalConfigurationBootstrap;

    /**
     * List of configuration files to upgrade.
     */
    protected String[] configFiles;

    @Autowired
    protected ServletContext servletContext;

    @Required
    public void setConfigFiles(final String[] configFiles) {
        this.configFiles = configFiles;
    }

    public void init() {
        globalConfigurationPath = studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);

        globalConfigurationBootstrap = new ServletContextResource(servletContext, UrlUtils.concat(
            FILE_SEPARATOR,
            BOOTSTRAP_REPO_PATH,
            studioConfiguration.getProperty(GLOBAL_REPO_PATH),
            globalConfigurationPath,
            FILE_SEPARATOR)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldUpgradeGlobalRepo(String currentVersion) {
        return currentVersion.startsWith(DB_VERSION_3_0_X) || currentVersion.startsWith(DB_VERSION_3_1_X);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upgradeGlobalRepo(String currentVersion) {
        logger.info("Upgrading global configuration files");

        for(String configFile : configFiles) {
            logger.debug("Upgrading configuration file: {0}", configFile);
            try (InputStream menuIs = globalConfigurationBootstrap.createRelative(configFile).getInputStream()) {

                RepositoryUtils.writeToRepo(studioConfiguration, StringUtils.EMPTY,
                    UrlUtils.concat(globalConfigurationPath, configFile), menuIs, "Global Configuration Upgrade");

            } catch (IOException e) {
                logger.error("Upgrade for global configuration failed", e);
            }
        }
    }

}
