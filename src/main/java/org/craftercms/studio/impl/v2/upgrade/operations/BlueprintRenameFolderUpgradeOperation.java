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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.BLUE_PRINTS_PATH;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that renames the blueprint in the
 * global repository.
 * @author Dejan Brkic
 */
public class BlueprintRenameFolderUpgradeOperation extends AbstractUpgradeOperation {

    public static final String CONFIG_KEY_SITE = "site";
    public static final String CONFIG_KEY_OLD_PATH = "oldPath";
    public static final String CONFIG_KEY_NEW_PATH = "newPath";

    protected String siteId;
    protected String oldPath;
    protected String newPath;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final Configuration config) {
        siteId = config.getString(CONFIG_KEY_SITE);
        oldPath = config.getString(CONFIG_KEY_OLD_PATH);
        newPath = config.getString(CONFIG_KEY_NEW_PATH);
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        try {

            Path oldP = Paths.get(getRepositoryPath(siteId).toAbsolutePath().toString(), oldPath);
            Path newP = Paths.get(getRepositoryPath(siteId).toAbsolutePath().toString(), newPath);

            File oldF = oldP.toFile();
            File newF = newP.toFile();
            if (oldF.isDirectory()) {
                FileUtils.moveDirectory(oldF, newF);
            } else if (oldF.isFile()) {
                FileUtils.moveFile(oldF, newF);
            }

            commitAllChanges(siteId);
        } catch (Exception e) {
            throw new UpgradeException("Error moving path " + oldPath + " to path " + newPath + " for repo " +
                    (StringUtils.isEmpty(siteId) ? "global" : siteId), e);
        }
    }

}
