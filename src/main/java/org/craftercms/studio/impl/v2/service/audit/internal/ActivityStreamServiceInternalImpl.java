/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.audit.internal;

import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v2.dal.ActivityStreamDAO;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.model.rest.dashboard.Activity;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;

public class ActivityStreamServiceInternalImpl implements ActivityStreamServiceInternal {

    private SiteFeedMapper siteFeedMapper;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private ActivityStreamDAO activityStreamDAO;

    @Override
    public void insertActivity(long siteId, long userId, String action, ZonedDateTime actionTimestamp, Item item,
                               String packageId) {
        retryingDatabaseOperationFacade.insertActivity(siteId, userId, action, actionTimestamp, item,
                packageId);
    }

    @Override
    public int getActivitiesForUsersTotal(String siteId, List<String> usernames, List<String> actions,
                                          ZonedDateTime dateForm, ZonedDateTime dateTo) {
        return activityStreamDAO.getActivitiesForUsersTotal(getSiteId(siteId), usernames, actions, dateForm, dateTo);
    }

    @Override
    public List<Activity> getActivitiesForUsers(String siteId, List<String> usernames, List<String> actions,
                                                ZonedDateTime dateForm, ZonedDateTime dateTo, int offset, int limit) {
        return activityStreamDAO
                .getActivitiesForUsers(getSiteId(siteId), usernames, actions, dateForm, dateTo, offset, limit);
    }

    private long getSiteId(String site) {
        Map<String, Object> params = new HashMap<>();
        params.put(SITE_ID, site);
        SiteFeed siteFeed = siteFeedMapper.getSite(params);
        return siteFeed.getId();
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    public void setActivityStreamDAO(ActivityStreamDAO activityStreamDAO) {
        this.activityStreamDAO = activityStreamDAO;
    }
}
