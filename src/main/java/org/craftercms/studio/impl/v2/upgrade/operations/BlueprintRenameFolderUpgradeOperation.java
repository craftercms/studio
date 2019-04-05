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

    public static final String CONFIG_KEY_BLUEPRINT_FOLDER_NAME = "blueprintFolderName";
    public static final String CONFIG_KEY_NEW_FOLDER_NAME = "newFolderName";

    protected String blueprintFolderName;
    protected String newFolderName;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final Configuration config) {
        blueprintFolderName = config.getString(CONFIG_KEY_BLUEPRINT_FOLDER_NAME);
        newFolderName = config.getString(CONFIG_KEY_NEW_FOLDER_NAME);
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        try {
            Path globalConfigPath = getRepositoryPath(StringUtils.EMPTY);
            Path blueprintPath = Paths.get(globalConfigPath.toAbsolutePath().toString(),
                    studioConfiguration.getProperty(BLUE_PRINTS_PATH), blueprintFolderName);
            File blueprintFolder = blueprintPath.toFile();
            if (blueprintFolder.exists()) {
                Path newBlueprintPath = Paths.get(globalConfigPath.toAbsolutePath().toString(),
                        studioConfiguration.getProperty(BLUE_PRINTS_PATH), newFolderName);
                File newBlueprintFolder = newBlueprintPath.toFile();
                FileUtils.deleteDirectory(newBlueprintFolder);
                blueprintFolder.renameTo(newBlueprintFolder);
            }

            commitAllChanges(StringUtils.EMPTY);
        } catch (Exception e) {
            throw new UpgradeException("Error upgrading blueprints in the global repo", e);
        }
    }

}
