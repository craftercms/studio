package org.craftercms.studio.api.persistence;

import org.craftercms.studio.api.domain.ActivityFeed;

import java.util.Map;

public interface ActivityFeedMapper {

    ActivityFeed getDeletedActivity(Map params);
}
