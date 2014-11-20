package org.craftercms.studio.api.persistence;

import org.craftercms.studio.api.domain.DeploymentSyncHistory;

import java.util.Date;
import java.util.List;

public interface DeploymentSyncHistoryMapper {

    List<DeploymentSyncHistory> getDeploymentHistory(String site, Date fromDate, Date toDate, String filterType, int numberOfItems);
}