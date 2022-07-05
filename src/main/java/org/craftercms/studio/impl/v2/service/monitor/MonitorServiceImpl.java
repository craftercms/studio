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
package org.craftercms.studio.impl.v2.service.monitor;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.craftercms.studio.api.v2.service.monitor.MonitorService;

import java.util.List;
import java.util.Map;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_VIEW_LOGS;

/**
 * Default implementation for {@link MonitorService}.
 *
 * @author jmendeza
 */
public class MonitorServiceImpl implements MonitorService {
    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_VIEW_LOGS)
    public List<Map<String, Object>> getLogEvents(final String siteId, final long since) {
        return CircularQueueLogAppender.getLoggedEvents(siteId, since);
    }
}
