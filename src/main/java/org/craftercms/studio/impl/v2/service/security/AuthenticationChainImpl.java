/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.AuthenticationChain;
import org.craftercms.studio.api.v2.service.security.AuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGIN;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGIN_FAILED;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_USER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

public class AuthenticationChainImpl implements AuthenticationChain {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticationChainImpl.class);

    private List<AuthenticationProvider> authenticationChain;

    private UserServiceInternal userServiceInternal;
    private StudioConfiguration studioConfiguration;
    private UserDAO userDao;
    private GroupDAO groupDao;
    private AuditServiceInternal auditServiceInternal;
    private SiteService siteService;

    public void init() {
        List<HierarchicalConfiguration<ImmutableNode>> chainConfig =
            studioConfiguration.getSubConfigs(CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG);
        authenticationChain = new ArrayList<AuthenticationProvider>();
        chainConfig.forEach(providerConfig -> {
            AuthenticationProvider provider = AuthenticationProviderFactory.getAuthenticationProvider(providerConfig);
            if (provider != null && provider.isEnabled()) {
                authenticationChain.add(provider);
            }
        });
    }

    @Override
    public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response, String username,
                                  String password) throws Exception {
        boolean authenticated = false;
        Iterator<AuthenticationProvider> iterator = authenticationChain.iterator();
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
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        if (authenticated) {
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_LOGIN);
            auditLog.setActorId(username);
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setPrimaryTargetId(username);
            auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
            auditLog.setPrimaryTargetValue(username);
            auditServiceInternal.insertAuditLog(auditLog);

            logger.info("User " + username + " logged in from IP: " + ipAddress);
        } else {

            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_LOGIN_FAILED);
            auditLog.setActorId(username);
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setPrimaryTargetId(StringUtils.isEmpty(username) ? StringUtils.EMPTY : username);
            auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
            auditLog.setPrimaryTargetValue(username);
            auditServiceInternal.insertAuditLog(auditLog);

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

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
