/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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
 */

package org.craftercms.studio.impl.v1.web.security.access;

import org.craftercms.studio.api.v1.dal.User;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public class StudioGroupAPIAccessDecisionVoter extends StudioAbstractAccessDecisionVoter {

    private final static Logger logger = LoggerFactory.getLogger(StudioGroupAPIAccessDecisionVoter.class);

    private final static String ADD_USER = "/api/1/services/api/1/group/add-user.json";
    private final static String CREATE = "/api/1/services/api/1/group/create.json";
    private final static String DELETE = "/api/1/services/api/1/group/delete.json";
    private final static String GET = "/api/1/services/api/1/group/get.json";
    private final static String GET_ALL = "/api/1/services/api/1/group/get-all.json";
    private final static String GET_PER_SITE = "/api/1/services/api/1/group/get-per-site.json";
    private final static String REMOVE_USER = "/api/1/services/api/1/group/remove-user.json";
    private final static String UPDATE = "/api/1/services/api/1/group/update.json";
    private final static String USERS = "/api/1/services/api/1/group/users.json";

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
            HttpServletRequest request = filterInvocation.getRequest();
            requestUri = request.getRequestURI().replace(request.getContextPath(), "");
            String requsetUrl = filterInvocation.getRequestUrl();
            String userParam = request.getParameter("username");
            String siteParam = request.getParameter("site_id");
            User currentUser = null;
            try {
                currentUser = (User)authentication.getPrincipal();
            } catch (ClassCastException e) {
                // anonymous user
                if (!authentication.getPrincipal().toString().equals("anonymousUser")) {
                    logger.error("Error getting current user", e);
                    return ACCESS_ABSTAIN;
                }
            }
            switch (requestUri) {
                case ADD_USER:
                case CREATE:
                case DELETE:
                case GET_ALL:
                case REMOVE_USER:
                case UPDATE:
                    if (currentUser != null && isAdmin(currentUser)) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                case GET:
                case GET_PER_SITE:
                case USERS:
                    if (currentUser != null && (isAdmin(currentUser) || isSiteMember(siteParam, currentUser))) {
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
