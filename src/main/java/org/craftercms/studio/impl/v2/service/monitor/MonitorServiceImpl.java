package org.craftercms.studio.impl.v2.service.monitor;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.craftercms.studio.api.v2.service.monitor.MonitorService;

import java.util.List;
import java.util.Map;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_LOG_MONITOR;

/**
 * Default implementation for {@link MonitorService}.
 *
 * @author jmendeza
 */
public class MonitorServiceImpl implements MonitorService {
    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_LOG_MONITOR)
    public List<Map<String, Object>> getLogEvents(String siteId, long since) {
        return CircularQueueLogAppender.getLoggedEvents(siteId, since);
    }
}
