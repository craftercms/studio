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

package org.craftercms.studio.impl.v2.upgrade.operations.global;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that updates files on the global repository.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li>
 *         <strong>files</strong>: (required) list of paths to copy to the global repository. The format of each
 *         entry of this list is {SRC_PATH}:{GLOBAL_REPO_DEST_PATH}. The first component is the path
 *         of the source file to copy, and the second component is the destination path in the global repo.
 *     </li>
 *     <li>
 *         <strong>overwrite</strong>: (optional, defaults to true) Indicates if existing files should be overwritten.
 *     </li>
 * </ul>
 *
 *
 * @author joseross
 */
public class GlobalRepoUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRepoUpgradeOperation.class);

    public static final String CONFIG_KEY_FILES = "files";
    public static final String CONFIG_KEY_SRC = "src";
    public static final String CONFIG_KEY_DEST = "dest";
    public static final String CONFIG_KEY_OVERWRITE = "overwrite";

    /**
     * List of paths to update.
     */
    protected Map<Resource, String> files = new HashMap<>();

    /**
     * Indicates if existing files should be overwritten
     */
    protected boolean overwrite;

    public GlobalRepoUpgradeOperation(StudioConfiguration studioConfiguration) {
        super(studioConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doInit(final HierarchicalConfiguration config) {
        overwrite = config.getBoolean(CONFIG_KEY_OVERWRITE, true);
        List<HierarchicalConfiguration<ImmutableNode>> fileMappings = config.configurationsAt(CONFIG_KEY_FILES);
        for (HierarchicalConfiguration<ImmutableNode> fileMapping : fileMappings) {
            String src = fileMapping.getString(CONFIG_KEY_SRC);
            String dest = fileMapping.getString(CONFIG_KEY_DEST);

            if (StringUtils.isEmpty(src)) {
                throw new IllegalStateException("'" + CONFIG_KEY_SRC + "' config key not specified");
            }
            if (StringUtils.isEmpty(dest)) {
                throw new IllegalStateException("'" + CONFIG_KEY_DEST + "' config key not specified");
            }

            files.put(loadResource(src), dest);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        logger.debug("Upgrading global repo files");
        for(Map.Entry<Resource, String> entry : files.entrySet()) {
            var path = entry.getValue();
            var file = context.getFile(path);
            if (overwrite || !Files.exists(file)) {
                logger.debug("Upgrading global repo file: {0}", path);
                try (InputStream in = entry.getKey().getInputStream();
                     OutputStream out = Files.newOutputStream(file)) {
                    IOUtils.copy(in, out);
                    trackChangedFiles(path);
                } catch (IOException e) {
                    throw new UpgradeException("Error while upgrading global repo file " + path, e);
                }
            } else {
                logger.debug("File {0} already exists in global repo, it will not be changed", path);
            }
        }
    }
}
