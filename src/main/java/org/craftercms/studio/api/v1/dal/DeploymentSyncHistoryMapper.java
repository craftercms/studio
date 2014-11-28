package org.craftercms.studio.api.v1.dal;

import java.util.List;
import java.util.Map;

public interface DeploymentSyncHistoryMapper {

    List<DeploymentSyncHistory> getDeploymentHistory(Map params);
}