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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_WRITE;

public class StudioContentAPIAccessDecisionVoter extends StudioAbstractAccessDecisionVoter {

    private final static Logger logger = LoggerFactory.getLogger(StudioContentAPIAccessDecisionVoter.class);

    private final static String WRITE_CONTENT = "/api/1/services/api/1/content/write-content.json";

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public int voteInternal(Authentication authentication, Object o, Collection collection) {
        int toRet = ACCESS_ABSTAIN;
        String requestUri = "";
        if (o instanceof FilterInvocation) {
            FilterInvocation filterInvocation = (FilterInvocation) o;
            HttpServletRequest request = filterInvocation.getRequest();
            requestUri = request.getRequestURI().replace(request.getContextPath(), "");
            if (StringUtils.equals(requestUri, WRITE_CONTENT)) {
                String userParam = request.getParameter("username");
                String siteParam = request.getParameter("site_id");
                if (StringUtils.isEmpty(siteParam)) {
                    siteParam = request.getParameter("site");
                }
                String pathParam = request.getParameter("path");
                if (StringUtils.isEmpty(userParam)
                        && StringUtils.equalsIgnoreCase(request.getMethod(), HttpMethod.POST.name())
                        && !ServletFileUpload.isMultipartContent(request)) {
                    try {
                        InputStream is = request.getInputStream();
                        is.mark(0);
                        String jsonString = IOUtils.toString(is, StandardCharsets.UTF_8);
                        if (StringUtils.isNoneEmpty(jsonString)) {
                            JSONObject jsonObject = JSONObject.fromObject(jsonString);
                            if (jsonObject.has("site")) {
                                siteParam = jsonObject.getString("site");
                            }
                            if (jsonObject.has("site_id")) {
                                siteParam = jsonObject.getString("site_id");
                            }
                            if (jsonObject.has("path")) {
                                pathParam = jsonObject.getString("path");
                            }
                        }
                        is.reset();
                    } catch (IOException | JSONException e) {
                        logger.debug("Failed to extract username from POST request");
                    }
                }
                User currentUser = (User) authentication.getPrincipal();
                switch (requestUri) {
                    case WRITE_CONTENT:
                        if (siteService.exists(siteParam)) {
                            if (currentUser != null && isSiteMember(siteParam, currentUser) &&
                                    hasPermission(siteParam, pathParam, currentUser.getUsername(),
                                            PERMISSION_CONTENT_WRITE)) {
                                toRet = ACCESS_GRANTED;
                            } else {
                                toRet = ACCESS_DENIED;
                            }
                        } else {
                            toRet = ACCESS_ABSTAIN;
                        }
                        break;
                    default:
                        toRet = ACCESS_ABSTAIN;
                        break;
                }
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
