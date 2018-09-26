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

import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.service.security.AuthenticationChain;
import org.craftercms.studio.api.v2.service.security.AuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.BaseAuthenticationProvider;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.craftercms.studio.model.AuthenticationType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.HTTP_SESSION_ATTRIBUTE_AUTHENTICATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_SESSION_TIMEOUT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;

public class DbAuthenticationProvider extends BaseAuthenticationProvider {

    private final static Logger logger = LoggerFactory.getLogger(DbAuthenticationProvider.class);

    private UserDAO userDao;
    private StudioConfiguration studioConfiguration;

    @Override
    public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationChain authenticationChain) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        UserTO user = null;
        try {
            user = userDao.getUserByIdOrUsername(params);
        } catch (Exception e) {
            logger.error("Unknown database errot", e);
        }
        if (user != null && user.isEnabled() && CryptoUtils.matchPassword(user.getPassword(), password)) {
            String token = createToken(user);

            storeAuthentication(new Authentication(username, token, AuthenticationType.DB));

            return true;
        }
        return authenticationChain.doAuthenticate(request, response);
    }

    protected String createToken(UserTO user) {
        int timeout = Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
        String token = SessionTokenUtils.createToken(user.getUsername(), timeout);
        return token;
    }

    protected void storeAuthentication(Authentication authentication) {
        RequestContext context = RequestContext.getCurrent();
        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION, authentication);
        }
    }

    public UserDAO getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
