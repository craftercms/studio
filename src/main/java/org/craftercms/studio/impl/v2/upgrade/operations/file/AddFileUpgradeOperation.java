/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.upgrade.operations.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that adds a new file to
 * a repository.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>path</strong>: (required) the relative path to write the file in the repository</li>
 *     <li><strong>file</strong>: (required) the location of the file to copy</li>
 * </ul>
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

    public AddFileUpgradeOperation(StudioConfiguration studioConfiguration) {
        super(studioConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration config) {
        path = config.getString(CONFIG_KEY_PATH);
        file = new ClassPathResource(config.getString(CONFIG_KEY_FILE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        var newFile = context.getFile(path);
        if(Files.exists(newFile)) {
            logger.info("File {0} already exist in site {1}, it will not be changed", path, site);
        } else {
            try(InputStream in = file.getInputStream();
                OutputStream out = Files.newOutputStream(newFile)) {
                IOUtils.copy(in, out);
                trackChangedFiles(path);
            } catch (IOException e) {
                throw new UpgradeException("Error upgrading file " + path + " for site " + site, e);
            }
        }
    }

}
