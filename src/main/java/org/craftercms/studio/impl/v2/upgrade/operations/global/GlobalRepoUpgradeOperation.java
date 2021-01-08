/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.craftercms.studio.impl.v2.job.StudioClusterGlobalRepoSyncTask;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.springframework.core.io.Resource;

/**
 * Implementation of {@link UpgradeOperation} that updates files on the global repository.
 *
 * <p>Suported YAML properties:
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
 * </p>
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
    protected Map<Resource, String> files;

    /**
     * Indicates if existing files should be overwritten
     */
    protected boolean overwrite;

    protected StudioClusterGlobalRepoSyncTask clusterGlobalRepoSyncTask;

    public GlobalRepoUpgradeOperation() {
        files = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration<ImmutableNode> config) {
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
    public void execute(final String site) throws UpgradeException {
        logger.debug("Upgrading global repo files");
        clusterGlobalRepoSyncTask.execute();
        for(Map.Entry<Resource, String> entry : files.entrySet()) {
            if (overwrite || !contentRepository.contentExists(site, entry.getValue())) {
                logger.debug("Upgrading global repo file: {0}", entry.getValue());
                try (InputStream is = entry.getKey().getInputStream()) {
                    writeToRepo(site, entry.getValue(), is);
                } catch (IOException e) {
                    throw new UpgradeException("Error while upgrading global repo file " + entry.getValue(), e);
                }
            } else {
                logger.debug("File {0} already exists in global repo, it will not be changed", entry.getValue());
            }
        }

    }

    public StudioClusterGlobalRepoSyncTask getClusterGlobalRepoSyncTask() {
        return clusterGlobalRepoSyncTask;
    }

    public void setClusterGlobalRepoSyncTask(StudioClusterGlobalRepoSyncTask clusterGlobalRepoSyncTask) {
        this.clusterGlobalRepoSyncTask = clusterGlobalRepoSyncTask;
    }
}
