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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.dal.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_PUBLISH_BY_COMMITS;

public class StudioPublishingAPIAccessDecisionVoter extends StudioAbstractAccessDecisionVoter {

    private final static Logger logger = LoggerFactory.getLogger(StudioPublishingAPIAccessDecisionVoter.class);
    private final static String COMMITS = "/api/1/services/api/1/publish/commits.json";

    protected final ObjectMapper objectMapper = new ObjectMapper();

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
            String userParam = request.getParameter("username");
            String siteParam = request.getParameter("site_id");
            if (StringUtils.isEmpty(userParam)
                    && StringUtils.equalsIgnoreCase(request.getMethod(), HttpMethod.POST.name())
                    && !JakartaServletFileUpload.isMultipartContent(request)) {
                try (InputStream is = request.getInputStream()) {
                    String jsonString = IOUtils.toString(is, StandardCharsets.UTF_8);
                    if (StringUtils.isNoneEmpty(jsonString)) {
                        ApiParams apiParams = objectMapper.readValue(jsonString, ApiParams.class);
                        siteParam = defaultIfEmpty(apiParams.getSite(), siteParam);
                    }
                } catch (JsonParseException e) {
                    logger.info("Failed to parse request as JSON", e);
                } catch (IOException e) {
                    logger.info("Failed to extract the fields from the POST request", e);
                }
            }
            User currentUser = (User) authentication.getPrincipal();
            switch (requestUri) {
                case COMMITS:
                    if (siteService.exists(siteParam)) {
                        if (currentUser != null &&
                                (isSiteAdmin(siteParam, currentUser) || hasPermission(siteParam, DEFAULT_PERMISSION_VOTER_PATH,
                                        currentUser.getUsername(), PERMISSION_PUBLISH_BY_COMMITS))) {
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
        logger.trace("The request with URL '{}' has access '{}'", requestUri, toRet);
        return toRet;
    }


    @Override
    public boolean supports(Class aClass) {
        return true;
    }

    /**
     * Simple POJO to read JSON parameters
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiParams {
        private String site;

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }

        public void setSite_id(String site_id) {
            this.site = site_id;
        }
    }
}
