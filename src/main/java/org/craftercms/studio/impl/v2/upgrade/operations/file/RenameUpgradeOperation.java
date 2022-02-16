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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that renames/moves files and
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

    public RenameUpgradeOperation(StudioConfiguration studioConfiguration) {
        super(studioConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration config) {
        oldPath = config.getString(CONFIG_KEY_OLD_PATH);
        newPath = config.getString(CONFIG_KEY_NEW_PATH);
        overwrite = config.getBoolean(CONFIG_KEY_OVERWRITE, false);
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        try {
            Path repo = context.getRepositoryPath();
            Path from = repo.resolve(oldPath);
            Path to = repo.resolve(newPath);

            if (renamePath(from, to)) {
                trackDeletedFiles(oldPath);
                trackChangedFiles(newPath);
            }
        } catch (Exception e) {
            throw new UpgradeException("Error moving path " + oldPath + " to path " + newPath + " for repo " +
                    (StringUtils.isEmpty(site) ? "global" : site), e);
        }
    }

    protected boolean renamePath(Path from, Path to) throws IOException {
        File fromFile = from.toFile();
        File toFile = to.toFile();
        if (fromFile.exists()) {
            if (toFile.exists()) {
                if (overwrite) {
                    FileUtils.forceDelete(toFile);
                } else {
                    logger.info("Rename operation not executed because target path {0} already exists.", to);
                    return false;
                }
            }
            if (fromFile.isDirectory()) {
                FileUtils.moveDirectory(fromFile, toFile);
            } else if (fromFile.isFile()) {
                FileUtils.moveFile(fromFile, toFile);
            }
            return true;
        }
        return false;
    }

}
