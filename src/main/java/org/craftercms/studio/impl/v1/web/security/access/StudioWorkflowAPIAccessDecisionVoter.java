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

import net.sf.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudioWorkflowAPIAccessDecisionVoter extends StudioAbstractAccessDecisionVoter {

    private final static Logger logger = LoggerFactory.getLogger(StudioWorkflowAPIAccessDecisionVoter.class);

    private final static String GO_LIVE = "/api/1/services/api/1/workflow/go-live.json";
    private final static String REJECT = "/api/1/services/api/1/workflow/reject.json";
    private final static String GO_DELETE = "/api/1/services/api/1/workflow/go-delete.json";

    private final static Set<String> URIS_TO_VOTE = new HashSet<String>() {{
        add(GO_LIVE);
        add(REJECT);
        add(GO_DELETE);
    }};

    private final static String PUBLISH_PERMISSION = "publish";
    private final static String DELETE_PERMISSION = "delete";
    private final static String DELETE_CONTENT_PERMISSION = "delete_content";
    private final static String CANCEL_PUBLISH_PERMISSION = "cancel_publish";

    private final static Set<String> DELETE_PERMISSIONS = new HashSet<String>() {{
        add(DELETE_PERMISSION);
        add(DELETE_CONTENT_PERMISSION);
    }};
    private final static Set<String> REJECT_PERMISSIONS = new HashSet<String>() {{
        add(PUBLISH_PERMISSION);
        add(CANCEL_PUBLISH_PERMISSION);
    }};


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
            if (URIS_TO_VOTE.contains(requestUri)) {
                String userParam = request.getParameter("username");
                String siteParam = request.getParameter("site_id");
                List<String> paths = new ArrayList<String>();
                if (StringUtils.isEmpty(siteParam)) {
                    siteParam = request.getParameter("site");
                }
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
                            if (jsonObject.has("items")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("items");
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    paths.add(jsonArray.optString(i));
                                }
                            }
                        }
                        is.reset();
                    } catch (IOException | JSONException e) {
                        logger.debug("Failed to extract username from POST request");
                    }
                }
                User currentUser = (User) authentication.getPrincipal();
                switch (requestUri) {
                    case GO_LIVE:
                        if (siteService.exists(siteParam)) {
                            for (String path : paths) {
                                if (currentUser != null && isSiteMember(siteParam, currentUser) &&
                                        hasPermission(siteParam, path, currentUser.getUsername(), PUBLISH_PERMISSION)) {
                                    toRet = ACCESS_GRANTED;
                                } else {
                                    toRet = ACCESS_DENIED;
                                    break;
                                }
                            }
                        }
                        break;
                    case REJECT:
                        if (siteService.exists(siteParam)) {
                            for (String path : paths) {
                                if (currentUser != null && isSiteMember(siteParam, currentUser) &&
                                        hasAnyPermission(siteParam, path, currentUser.getUsername(),
                                                REJECT_PERMISSIONS)) {
                                    toRet = ACCESS_GRANTED;
                                } else {
                                    toRet = ACCESS_DENIED;
                                    break;
                                }
                            }
                        } else {
                            toRet = ACCESS_ABSTAIN;
                        }
                        break;
                    case GO_DELETE:
                        if (siteService.exists(siteParam)) {
                            for (String path : paths) {
                                if (currentUser != null && isSiteMember(siteParam, currentUser) &&
                                        hasAnyPermission(siteParam, path, currentUser.getUsername(),
                                                DELETE_PERMISSIONS)) {
                                    toRet = ACCESS_GRANTED;
                                } else {
                                    toRet = ACCESS_DENIED;
                                    break;
                                }
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
