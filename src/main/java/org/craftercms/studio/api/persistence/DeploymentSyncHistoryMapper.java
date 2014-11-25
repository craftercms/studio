package org.craftercms.studio.api.persistence;

import org.craftercms.studio.api.domain.DeploymentSyncHistory;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DeploymentSyncHistoryMapper {

    List<DeploymentSyncHistory> getDeploymentHistory(Map params);
}