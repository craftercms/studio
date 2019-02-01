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
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that adds a new file to
 * a repository.
 *
 * <p>Supported YAML properties:
 * <ul>
 *     <li><strong>path</strong>: (required) the relative path to write the file in the repository</li>
 *     <li><strong>file</strong>: (required) the location of the file to copy</li>
 * </ul>
 * </p>
 *
 * @author joseross
 */
public class AddFileUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(AddFileUpgradeOperation.class);

    public static final String CONFIG_KEY_PATH = "path";
    public static final String CONFIG_KEY_FILE = "file";

    /**
     * The path to write the file.
     */
    protected String path;

    /**
     * The file to copy from the classpath.
     */
    protected Resource file;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final Configuration config) {
        path = config.getString(CONFIG_KEY_PATH);
        file = new ClassPathResource(config.getString(CONFIG_KEY_FILE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        try(InputStream is = file.getInputStream()) {
            if(contentRepository.contentExists(site, path)) {
                logger.info("File {0} already exist in site {1}, it will not be changed", path, site);
            } else {
                writeToRepo(site, path, is);
            }
        } catch (IOException e) {
            throw new UpgradeException("Error upgrading file " + path + " for site " + site, e);
        }
    }

}
