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
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;

import java.io.File;
import java.nio.file.Path;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that deletes files and folders in the
 * repository
 * @author Dejan Brkic
 */
public class DeleteUpgradeOperation extends AbstractUpgradeOperation {

    public static final String CONFIG_KEY_PATHS = "paths";

    protected String[] paths;

    public DeleteUpgradeOperation(StudioConfiguration studioConfiguration) {
        super(studioConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration config) {
        paths = (String[]) config.getArray(String.class, CONFIG_KEY_PATHS);
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        for (String path : paths) {
            try {
                Path pathToDelete = context.getFile(path);
                File f = pathToDelete.toFile();
                if (f.exists()) {
                    FileUtils.forceDelete(f);
                    trackDeletedFiles(path);
                }
            } catch (Exception e) {
                throw new UpgradeException("Error deleting path " + path + " to path for repo " +
                        (StringUtils.isEmpty(site) ? "global" : site), e);
            }
        }
    }

}
