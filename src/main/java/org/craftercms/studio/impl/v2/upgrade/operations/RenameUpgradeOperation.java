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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that renames/moves files and
 * folders in the repository.
 * @author Dejan Brkic
 */
public class RenameUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(RenameUpgradeOperation.class);

    public static final String CONFIG_KEY_OLD_PATH = "oldPath";
    public static final String CONFIG_KEY_NEW_PATH = "newPath";
    public static final String CONFIG_KEY_OVERWRITE = "overwrite";

    protected String oldPath;
    protected String newPath;
    protected boolean overwrite;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration<ImmutableNode> config) {
        oldPath = config.getString(CONFIG_KEY_OLD_PATH);
        newPath = config.getString(CONFIG_KEY_NEW_PATH);
        overwrite = config.getBoolean(CONFIG_KEY_OVERWRITE, false);
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        try {

            Path oldP = Paths.get(getRepositoryPath(site).getParent().toAbsolutePath().toString(), oldPath);
            Path newP = Paths.get(getRepositoryPath(site).getParent().toAbsolutePath().toString(), newPath);

            File oldF = oldP.toFile();
            File newF = newP.toFile();
            if (oldF.exists()) {
                if (newF.exists()) {
                    if (overwrite) {
                        FileUtils.forceDelete(newF);
                    } else {
                        logger.info("Rename operation not executed beacuse target path " + newP.toString() + " already " +
                                "exists.");
                        return;
                    }
                }
                if (oldF.isDirectory()) {
                    FileUtils.moveDirectory(oldF, newF);
                } else if (oldF.isFile()) {
                    FileUtils.moveFile(oldF, newF);
                }

                commitAllChanges(site);
            }
        } catch (Exception e) {
            throw new UpgradeException("Error moving path " + oldPath + " to path " + newPath + " for repo " +
                    (StringUtils.isEmpty(site) ? "global" : site), e);
        }
    }

}
