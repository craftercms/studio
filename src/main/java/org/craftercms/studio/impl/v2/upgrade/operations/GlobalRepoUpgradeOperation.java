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

package org.craftercms.studio.impl.v2.upgrade.operations;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.springframework.core.io.Resource;

import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.GLOBAL_REPO_PATH;

/**
 * Implementation of {@link UpgradeOperation} that updates files on the global repository.
 * @author joseross
 */
public class GlobalRepoUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRepoUpgradeOperation.class);

    public static final String CONFIG_KEY_FILES = "files";

    /**
     * List of paths to update.
     */
    protected String[] files;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final String sourceVersion, final String targetVersion, final Configuration config) {
        super.init(sourceVersion, targetVersion, config);
        files = (String[]) config.getArray(String.class, CONFIG_KEY_FILES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        Resource globalConfigurationBootstrap = getServletResource(UrlUtils.concat(
            FILE_SEPARATOR,
            BOOTSTRAP_REPO_PATH,
            getProperty(GLOBAL_REPO_PATH),
            FILE_SEPARATOR)
        );

        logger.info("Upgrading global repo files");

        for(String file : files) {
            logger.debug("Upgrading configuration file: {0}", file);
            try (InputStream is = globalConfigurationBootstrap.createRelative(file).getInputStream()) {

                writeToRepo(site, file, is, "Global Repo Upgrade v" + currentVersion + " to v" + nextVersion);

            } catch (IOException e) {
                throw new UpgradeException("Upgrade for global repo failed", e);
            }
        }

    }
}
