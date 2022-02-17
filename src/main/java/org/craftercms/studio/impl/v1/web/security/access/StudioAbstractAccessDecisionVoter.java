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

package org.craftercms.studio.impl.v1.web.security.access;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEFAULT_ADMIN_GROUP;

public abstract class StudioAbstractAccessDecisionVoter implements AccessDecisionVoter {

    private final static Logger logger = LoggerFactory.getLogger(StudioAbstractAccessDecisionVoter.class);

    protected SecurityService securityService;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected UserServiceInternal userServiceInternal;

    @Override
    public int vote(Authentication authentication, Object object, Collection collection) {
        // Don't vote for any unauthenticated request, those are handled by spring's voters
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ACCESS_ABSTAIN;
        }

        return voteInternal(authentication, object, collection);
    }

    protected abstract int voteInternal(Authentication authentication, Object object, Collection collection);

    protected boolean isSiteMember(User currentUser, String userParam) {
        try {
            int total1 = siteService.getSitesPerUserTotal(userParam);
            List<SiteFeed> sitesFeed1 = siteService.getSitesPerUser(userParam, 0, total1);
            int total2 = siteService.getSitesPerUserTotal(currentUser.getUsername());
            List<SiteFeed> sitesFeed2 = siteService.getSitesPerUser(currentUser.getUsername(), 0, total2);

            Set<String> sites1 = new HashSet<String>();
            Set<String> sites2 = new HashSet<String>();
            for (SiteFeed site : sitesFeed1) {
                sites1.add(site.getSiteId());
            }
            for (SiteFeed site : sitesFeed2) {
                sites2.add(site.getSiteId());
            }

            Collection intersection = CollectionUtils.intersection(sites1, sites2);
            return CollectionUtils.isNotEmpty(intersection);
        } catch (UserNotFoundException e) {
            logger.info("User is not site member", e);
            return false;
        } catch (ServiceLayerException e) {
            logger.warn("Error getting user membership", e);
            return false;
        }
    }

    protected boolean isSiteMember(String siteId, User currentUser) {
        try {
            int total = siteService.getSitesPerUserTotal(currentUser.getUsername());
            List<SiteFeed> sitesFeed = siteService.getSitesPerUser(currentUser.getUsername(), 0, total);

            Set<String> sites = new HashSet<String>();
            for (SiteFeed site : sitesFeed) {
                sites.add(site.getSiteId());
            }

            return sites.contains(siteId);
        } catch (UserNotFoundException e) {
            logger.info("User is not site member", e);
            return false;
        } catch (ServiceLayerException e) {
            logger.warn("Error getting user membership", e);
            return false;
        }
    }

    protected boolean isSiteAdmin(String siteId, User currentUser) {
        try {
            int total = siteService.getSitesPerUserTotal(currentUser.getUsername());
            List<SiteFeed> sitesFeed = siteService.getSitesPerUser(currentUser.getUsername(), 0, total);

            Map<String, Long> sites = new HashMap<String, Long>();
            for (SiteFeed site : sitesFeed) {
                sites.put(site.getSiteId(), site.getId());
            }

            boolean toRet = sites.containsKey(siteId);
            if (toRet) {
                List<Group> userGroups = userServiceInternal.getUserGroups(sites.get(siteId), currentUser.getUsername());
                for (Group g : userGroups) {
                    if (g.getGroupName().equals(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_ADMIN_GROUP))) {
                        toRet = true;
                        break;
                    }
                }
                toRet = userGroups.contains(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_ADMIN_GROUP));
            }
            return toRet;
        } catch (UserNotFoundException e) {
            logger.info("User is not site member", e);
            return false;
        } catch (ServiceLayerException e) {
            logger.error("Error getting user memberships", e);
            return false;
        }
    }

    protected boolean isSelf(User currentUser, String userParam) {
        return StringUtils.equals(userParam, currentUser.getUsername());
    }

    protected boolean isAdmin(User user) {
        List<Group> userGroups = null;
        try {
            userGroups = userServiceInternal.getUserGroups(-1, user.getUsername());
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Error getting user memberships", e);
            return false;
        }
        boolean toRet = false;
        if (CollectionUtils.isNotEmpty(userGroups)) {
            for (Group group : userGroups) {
                if (StringUtils.equalsIgnoreCase(group.getGroupName(), SYSTEM_ADMIN_GROUP)) {
                    toRet = true;
                    break;
                }
            }
        }
        return toRet;
    }

    protected boolean hasPermission(String siteId, String path, String user, String permission) {
        Set<String> userPermissions = securityService.getUserPermissions(siteId, path, user, null);
        return StringUtils.isEmpty(permission) ||
                (CollectionUtils.isNotEmpty(userPermissions) && userPermissions.contains(permission));
    }

    protected boolean hasAnyPermission(String siteId, String path, String user, Set<String> permissions) {
        Set<String> userPermissions = securityService.getUserPermissions(siteId, path, user, null);
        return CollectionUtils.isEmpty(permissions) ||
                (CollectionUtils.isNotEmpty(userPermissions)
                        && CollectionUtils.containsAny(userPermissions, permissions));
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }
}
