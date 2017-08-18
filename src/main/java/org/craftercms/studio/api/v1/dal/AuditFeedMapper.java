package org.craftercms.studio.api.v1.dal;

import java.util.List;
import java.util.Map;

public interface AuditFeedMapper {

    AuditFeed getDeletedActivity(Map params);

    List<AuditFeed> selectUserFeedEntries(Map params);

    List<AuditFeed> selectUserFeedEntriesHideLive(Map params);

    int getCountUserContentFeedEntries(Map params);

    long insertActivityFeed(AuditFeed feed);

    void updateActivityFeed(AuditFeed feed);

    void renameContent(Map params);

    void deleteActivitiesForSite(Map params);

    List<AuditFeed> getAuditLogForSite(Map params);

    long getAuditLogForSiteTotal(Map params);
}
