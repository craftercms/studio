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

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.User;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public class StudioCmisDSAPIAccessDecisionVoter extends StudioAbstractAccessDecisionVoter {

    private final static Logger logger = LoggerFactory.getLogger(StudioCmisDSAPIAccessDecisionVoter.class);

    private final static String LIST = "/api/1/services/api/1/cmis/list.json";
    private final static String SEARCH = "/api/1/services/api/1/cmis/search.json";
    private final static String UPLOAD = "/api/1/services/api/1/cmis/upload.json";

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public int voteInternal(Authentication authentication, Object o, Collection collection) {
        int toRet = ACCESS_ABSTAIN;
        String requestUri = "";
        if (o instanceof FilterInvocation) {
            FilterInvocation filterInvocation = (FilterInvocation)o;
            HttpServletRequest  request = filterInvocation.getRequest();
            requestUri = request.getRequestURI().replace(request.getContextPath(), "");
            String siteParam = request.getParameter("site_id");
            User currentUser = (User) authentication.getPrincipal();

            switch (requestUri) {
                case UPLOAD:
                    if (currentUser != null) {
                        toRet = ACCESS_GRANTED;
                    } else {
                        toRet = ACCESS_DENIED;
                    }
                    break;
                case SEARCH:
                case LIST:
                    if (currentUser != null && isSiteMember(siteParam, currentUser)) {
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
