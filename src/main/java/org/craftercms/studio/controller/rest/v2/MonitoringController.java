package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.exceptions.InvalidMonitoringTokenException;
import org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase;
import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResultList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase.ROOT_URL;
import static org.craftercms.engine.controller.rest.MonitoringController.LOG_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_MONITORING_AUTHORIZATION_TOKEN;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_EVENTS;

/**
 * Rest controller to provide monitoring information
 * @author joseross
 */
@RestController
@RequestMapping("/api/2")
public class MonitoringController extends MonitoringRestControllerBase {

    private StudioConfiguration studioConfiguration;

    @GetMapping(ROOT_URL + LOG_URL)
    public ResultList<Map<String,Object>> getLogEvents(@RequestParam long since, @RequestParam String token)
            throws InvalidMonitoringTokenException {
        if (StringUtils.isNotEmpty(token) && StringUtils.equals(token, getConfiguredToken())) {
            ResultList<Map<String, Object>> result = new ResultList<>();
            result.setResponse(ApiResponse.OK);
            result.setEntities(RESULT_KEY_EVENTS, CircularQueueLogAppender.getLoggedEvents("craftercms", since));
            return result;
        } else {
            throw new InvalidMonitoringTokenException("Invalid token for monitoring authorization");
        }
    }

    @Override
    protected String getConfiguredToken() {
        return studioConfiguration.getProperty(CONFIGURATION_MONITORING_AUTHORIZATION_TOKEN);
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
