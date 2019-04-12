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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.GLOBAL_REPO_PATH;

/**
 * Implementation of {@link UpgradeOperation} that updates files on the global repository.
 *
 * <p>Suported YAML properties:
 * <ul>
 *     <li>
 *         <strong>files</strong>: (required) list of paths to copy to the global repository. The format of each
 *         entry of this list is {CLASSPATH_SRC_PATH}:{GLOBAL_REPO_DEST_PATH}. The first component is the classpath
 *         of the source file to copy, and the second component is the destination path in the global repo.
 *     </li>
 * </ul>
 * </p>
 *
 * @author joseross
 */
public class GlobalRepoUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRepoUpgradeOperation.class);

    public static final String FILE_MAPPING_SEPARATOR = ":";
    public static final String CONFIG_KEY_FILES = "files";

    /**
     * List of paths to update.
     */
    protected Map<Resource, String> files;

    public GlobalRepoUpgradeOperation() {
        files = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final Configuration config) {
        String[] fileMappings = (String[]) config.getArray(String.class, CONFIG_KEY_FILES);
        for (String fileMapping : fileMappings) {
            String[] splitMapping = fileMapping.split(FILE_MAPPING_SEPARATOR);
            if (splitMapping.length == 2) {
                Resource srcFile = new ClassPathResource(splitMapping[0]);
                String destFile = splitMapping[1];

                files.put(srcFile, destFile);
            } else {
                throw new IllegalStateException("Configuration for global upgrade operation is invalid: format of " +
                                                "file mapping " + fileMapping + " should be {CLASSPATH_SRC_PATH}" +
                                                FILE_MAPPING_SEPARATOR + "{GLOBAL_REPO_DEST_PATH}");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        logger.info("Upgrading global repo files");

        for(Map.Entry<Resource, String> entry : files.entrySet()) {
            logger.debug("Upgrading global repo file: {0}", entry.getValue());

            try (InputStream is = entry.getKey().getInputStream()) {
                writeToRepo(site, entry.getValue(), is);
            } catch (IOException e) {
                throw new UpgradeException("Error while upgrading global repo file " + entry.getValue(), e);
            }
        }

    }
}
