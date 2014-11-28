package org.craftercms.studio.api.v1.dal;

import java.util.List;
import java.util.Map;

public interface ActivityFeedMapper {

    ActivityFeed getDeletedActivity(Map params);

    List<ActivityFeed> selectUserFeedEntries(Map params);

    List<ActivityFeed> selectUserFeedEntriesHideLive(Map params);
}
