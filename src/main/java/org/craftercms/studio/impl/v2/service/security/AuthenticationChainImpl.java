/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.service.security.AuthenticationChain;
import org.craftercms.studio.api.v2.service.security.AuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

public class AuthenticationChainImpl implements AuthenticationChain {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticationChainImpl.class);

    private List<AuthenticationProvider> authentitcationChain;

    private UserServiceInternal userServiceInternal;
    private ActivityService activityService;
    private StudioConfiguration studioConfiguration;
    private UserDAO userDao;
    private GroupDAO groupDao;

    public void init() {
        List<HierarchicalConfiguration<ImmutableNode>> chainConfig =
            studioConfiguration.getSubConfigs(CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG);
        authentitcationChain = new ArrayList<AuthenticationProvider>();
        chainConfig.forEach(providerConfig -> {
            AuthenticationProvider provider = AuthenticationProviderFactory.getAuthenticationProvider(providerConfig);
            if (provider != null && provider.isEnabled()) {
                authentitcationChain.add(provider);
            }
        });
    }

    @Override
    public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response, String username,
                                  String password) throws Exception {
        boolean authenticated = false;
        Iterator<AuthenticationProvider> iterator = authentitcationChain.iterator();
        Exception lastError = null;
        while (iterator.hasNext()) {
            AuthenticationProvider authProvider = iterator.next();
            if (authProvider.isEnabled()) {
                try {
                    authenticated = authProvider.doAuthenticate(request, response, this, username, password);
                } catch (Exception e) {
                    lastError = e;
                }
                if (authenticated) break;
            }
        }
        String ipAddress = request.getRemoteAddr();
        if (authenticated) {

            ActivityService.ActivityType activityType = ActivityService.ActivityType.LOGIN;
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(getSystemSite(), username, ipAddress, activityType,
                    ActivityService.ActivitySource.API, extraInfo);

            logger.info("User " + username + " logged in from IP: " + ipAddress);
        } else {
            ActivityService.ActivityType activityType = ActivityService.ActivityType.LOGIN_FAILED;
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(getSystemSite(), StringUtils.isEmpty(username) ? StringUtils.EMPTY : username,
                    ipAddress, activityType, ActivityService.ActivitySource.API, extraInfo);

            logger.info("Failed to authenticate user " + username + " logging in from IP: " + ipAddress);

            if (lastError == null) {
                lastError = new AuthenticationSystemException("Unknown service error");
            }
            throw lastError;
        }
        return authenticated;
    }

    public String getSystemSite() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE);
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public ActivityService getActivityService() {
        return activityService;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public UserDAO getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }

    public GroupDAO getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDAO groupDao) {
        this.groupDao = groupDao;
    }
}
