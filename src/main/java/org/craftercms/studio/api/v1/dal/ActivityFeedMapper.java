package org.craftercms.studio.api.v1.dal;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ActivityFeedMapper {

    ActivityFeed getDeletedActivity(Map params);

    List<ActivityFeed> selectUserFeedEntries(Map params);

    List<ActivityFeed> selectUserFeedEntriesHideLive(Map params);

    int getCountUserContentFeedEntries(Map params);

    long insertActivityFeed(ActivityFeed feed);

    void updateActivityFeed(ActivityFeed feed);

    void renameContent(Map params);

    void deleteActivitiesForSite(Map params);

    List<ActivityFeed> getAuditLogForSite(Map params);

    long getAuditLogForSiteTotal(Map params);
}
