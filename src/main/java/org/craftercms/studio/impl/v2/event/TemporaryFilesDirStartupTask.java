/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.event;

import org.apache.commons.io.FileUtils;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v2.utils.spring.event.CleanupTemporaryFilesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * This task sets up the temporary files directory for Crafter Studio. <br/>
 * It will remove any contents of the TEMP_DIR/studio directory if it exists,
 * otherwise it will just create the directory.
 */
public class TemporaryFilesDirStartupTask {
    private static final Logger logger = LoggerFactory.getLogger(TemporaryFilesDirStartupTask.class);

    @Order(HIGHEST_PRECEDENCE)
    @EventListener(CleanupTemporaryFilesEvent.class)
    public void cleanup() throws IOException {
        logger.debug("Cleanup studio temporary files");
        Path studioTempDir = StudioUtils.getStudioTemporaryFilesRoot();
        logger.debug("Studio temporary directory '{}'", studioTempDir);
        File studioTempDirFile = studioTempDir.toFile();
        if (studioTempDirFile.exists()) {
            FileUtils.cleanDirectory(studioTempDirFile);
        } else {
            FileUtils.forceMkdir(studioTempDirFile);
        }
    }
}
