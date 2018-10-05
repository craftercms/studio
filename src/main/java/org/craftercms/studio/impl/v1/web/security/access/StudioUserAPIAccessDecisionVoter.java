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

package org.craftercms.studio.impl.v1.web.security.access;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.User;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class StudioUserAPIAccessDecisionVoter extends StudioAbstractAccessDecisionVoter {

    private final static Logger logger = LoggerFactory.getLogger(StudioUserAPIAccessDecisionVoter.class);

    private final static String CHANGE_PASSWORD = "/api/1/services/api/1/user/change-password.json";
    private final static String CREATE = "/api/1/services/api/1/user/create.json";
    private final static String DELETE = "/api/1/services/api/1/user/delete.json";
    private final static String DISABLE = "/api/1/services/api/1/user/disable.json";
    private final static String ENABLE = "/api/1/services/api/1/user/enable.json";
    private final static String FORGOT_PASSWORD = "/api/1/services/api/1/user/forgot-password.json";
    private final static String GET = "/api/1/services/api/1/user/get.json";
    private final static String GET_ALL = "/api/1/services/api/1/user/get-all.json";
    private final static String GET_PER_SITE = "/api/1/services/api/1/user/get-per-site.json";
    private final static String LOGIN = "/api/1/services/api/1/security/login.json";
    private final static String LOGOUT = "/api/1/services/api/1/security/logout.json";
    private final static String RESET_PASSWORD = "/api/1/services/api/1/user/reset-password.json";
    private final static String SET_PASSWORD = "/api/1/services/api/1/user/set-password.json";
    private final static String STATUS = "/api/1/services/api/1/user/status.json";
    private final static String UPDATE = "/api/1/services/api/1/user/update.json";
    private final static String VALIDATE_TOKEN = "/api/1/services/api/1/user/validate-token.json";

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object o, Collection collection) {
        int toRet = ACCESS_ABSTAIN;
        String requestUri = "";
        if (o instanceof FilterInvocation) {
            FilterInvocation filterInvocation = (FilterInvocation)o;
            HttpServletRequest  request = filterInvocation.getRequest();
            requestUri = request.getRequestURI().replace(request.getContextPath(), "");
            String userParam = request.getParameter("username");
            String siteParam = request.getParameter("site_id");
            if (StringUtils.isEmpty(userParam)
                && StringUtils.equalsIgnoreCase(request.getMethod(), HttpMethod.POST.name())
                && !ServletFileUpload.isMultipartContent(request)) {
                try {
                    InputStream is = request.getInputStream();
                    is.mark(0);
                    String jsonString = IOUtils.toString(is);
                    if (StringUtils.isNoneEmpty(jsonString)) {
                        JSONObject jsonObject = JSONObject.fromObject(jsonString);
                        if (jsonObject.has("username")) {
                            userParam = jsonObject.getString("username");
                        }
                        if (jsonObject.has("site_id")) {
                            siteParam = jsonObject.getString("site_id");
                        }
                    }
                    is.reset();
                } catch (IOException | JSONException e) {
                    // TODO: ??
                    logger.debug("Failed to extract username from POST request");
                }
            }
            User currentUser = null;
            try {
                currentUser = (User) authentication.getPrincipal();
            } catch (ClassCastException e) {
                // anonymous user
                if (!authentication.getPrincipal().toString().equals("anonymousUser")) {
                    logger.info("Error getting current user", e);
                    return ACCESS_ABSTAIN;
                }
            }
            switch (requestUri) {
                case FORGOT_PASSWORD:
                case LOGIN:
                case LOGOUT:
                case SET_PASSWORD:
                case VALIDATE_TOKEN:
                    toRet = ACCESS_GRANTED;
                    break;
                case CHANGE_PASSWORD:
                    if (currentUser != null && isSelf(currentUser, userParam)) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                case CREATE:
                case DELETE:
                case DISABLE:
                case ENABLE:
                case RESET_PASSWORD:
                case STATUS:
                    if (currentUser != null && isAdmin(currentUser)) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                case GET_ALL:
                    if (currentUser != null) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                case GET:
                    if (currentUser != null && (isAdmin(currentUser) || isSelf(currentUser, userParam) || isSiteMember(currentUser, userParam))) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                case GET_PER_SITE:
                    if (currentUser != null && (isAdmin(currentUser)  || isSiteMember(currentUser, userParam))) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                case UPDATE:
                    if (currentUser != null && (isAdmin(currentUser) || isSelf(currentUser, userParam))) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                default:
                    toRet = ACCESS_ABSTAIN;
                    break;
            }
        }
        logger.debug("Request: " + requestUri + " - Access: " + toRet);
        return toRet;
    }


    @Override
    public boolean supports(Class aClass) {
        return true;
    }

}
