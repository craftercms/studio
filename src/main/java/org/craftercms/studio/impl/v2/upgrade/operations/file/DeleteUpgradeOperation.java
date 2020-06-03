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

package org.craftercms.studio.impl.v2.upgrade.operations.file;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that deletes files and folders in the
 * repository
 * @author Dejan Brkic
 */
public class DeleteUpgradeOperation extends AbstractUpgradeOperation {

    public static final String CONFIG_KEY_PATHS = "paths";

    protected String[] paths;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration<ImmutableNode> config) {
        paths = (String[]) config.getArray(String.class, CONFIG_KEY_PATHS);
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        for (String path : paths) {
            try {
                Path pathToDelete = Paths.get(getRepositoryPath(site).getParent().toAbsolutePath().toString(), path);
                File f = pathToDelete.toFile();
                if (f.exists()) {
                    FileUtils.forceDelete(f);
                }
            } catch (Exception e) {
                throw new UpgradeException("Error deleting path " + path + " to path for repo " +
                        (StringUtils.isEmpty(site) ? "global" : site), e);
            }
        }

        commitAllChanges(site);

    }

}
