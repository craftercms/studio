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

package org.craftercms.studio.api.v1.service.monitor;

import org.craftercms.commons.monitoring.MemoryMonitor;
import org.craftercms.commons.monitoring.StatusMonitor;
import org.craftercms.commons.monitoring.VersionMonitor;

import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;

public interface MonitorService {

    /**
     * Get Studio version manifest.
     * @param manifest Manifest file to get the information.
     *                 on Webcontext manifest has to be loaded by the ServeltContext or a jar manifest will be loaded.
     * @return Version manifest
     */
    VersionMonitor getVersion(Manifest manifest) throws IOException;

    /**
     * Get Studio status.
     *
     * @return Studio status
     */
    StatusMonitor getStatus();

    /**
     * Get Studio memory status
     *
     * @return Memory status
     */
    List<MemoryMonitor> getMemory();
}
