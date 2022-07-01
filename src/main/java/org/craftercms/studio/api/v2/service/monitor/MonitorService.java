package org.craftercms.studio.api.v2.service.monitor;

import java.util.List;
import java.util.Map;

/**
 * Provides access to log monitoring
 *
 * @author jmendeza
 * @since 4.0.1
 */
public interface MonitorService {

    /**
     * Retrieves a list of events logged since the timestamp indicated by {@code since} parameter
     *
     * @param siteId the crafter site ID
     * @param since  timestamp. Events before this value will not be included.
     * @return a list of log events
     */
    List<Map<String, Object>> getLogEvents(final String siteId, final long since);
}
