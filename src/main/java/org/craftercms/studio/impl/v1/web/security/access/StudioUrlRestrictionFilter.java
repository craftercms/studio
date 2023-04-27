/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class StudioUrlRestrictionFilter extends OncePerRequestFilter {

    private final static Logger logger = LoggerFactory.getLogger(StudioUrlRestrictionFilter.class);

    public static final String STUDIO_SECURITY_RESTRICTION_URLS = "studio.security.restrictedUrls";
    public static final String STUDIO_SECURITY_RESTRICTION_CONFIG_KEY_URL = "url";
    public static final String STUDIO_SECURITY_RESTRICTION_CONFIG_KEY_PATH = "path";
    public static final String STUDIO_SECURITY_RESTRICTION_CONFIG_KEY_ROLES = "roles";
    private SecurityService securityService;
    private List<StudioRestrictionRule> restrictionList;

    public StudioUrlRestrictionFilter(StudioConfiguration studioConfiguration, SecurityService securityService) {
        this.securityService = securityService;
        this.restrictionList = getRestrictionRule(studioConfiguration);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        try {
            String currentUser = securityService.getCurrentUser();
            if (currentUser != null && securityService.isSystemAdmin(currentUser)) {
                return  true;
            }
        } catch (Exception e) {
            logger.warn("Error while checking logging user permissions.", e);
        }

        String requestUri = getRequestUri(request);
        return restrictionList.stream().noneMatch(rule -> rule.url.equalsIgnoreCase(requestUri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = getRequestUri(request);
        String siteParam = getSiteParam(request);
        String requestPath = getPathParam(request);
        String currentUser = securityService.getCurrentUser();
        List<StudioRestrictionRule> filterRules = restrictionList.stream()
                .filter(rule -> rule.url.equalsIgnoreCase(requestUri)).collect(Collectors.toList());
        for (StudioRestrictionRule filterRule : filterRules) {
            // If the rule has a path and this does not match the request path, ignore the rule
            if (filterRule.path != null && requestPath!= null && !filterRule.path.equalsIgnoreCase(requestPath)) {
                continue;
            }

            // If there is no logging user or no site params and the url is restricted, response with error
            if ((currentUser == null || StringUtils.isEmpty(siteParam)) && filterRule.roles.size() > 0) {
                logger.info("Restricted url '{}' for non authenticated user", requestUri);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
                return;
            }

            Set<String> userRoles = securityService.getUserRoles(siteParam, currentUser);
            boolean hasPermission = filterRule.roles.stream().anyMatch( role -> userRoles.contains(role));
            if (!hasPermission) {
                logger.info("Restricted url '{}', path '{}' for user '{}'", requestUri, requestPath, currentUser);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parse restriction rules from yaml
     * @param studioConfiguration studio configuration instance
     * @return list of {@link StudioRestrictionRule}
     */
    protected List<StudioRestrictionRule> getRestrictionRule(StudioConfiguration studioConfiguration) {
        List<StudioRestrictionRule> rules = new ArrayList<>();
        List<HierarchicalConfiguration<ImmutableNode>> restrictionsConfig =
                studioConfiguration.getSubConfigs(STUDIO_SECURITY_RESTRICTION_URLS);
        if (CollectionUtils.isNotEmpty(restrictionsConfig)) {
            restrictionsConfig.forEach(restrictionConfig -> {
                StudioRestrictionRule rule = new StudioRestrictionRule();
                rule.setUrl(restrictionConfig.getString(STUDIO_SECURITY_RESTRICTION_CONFIG_KEY_URL, null));
                rule.setPath(restrictionConfig.getString(STUDIO_SECURITY_RESTRICTION_CONFIG_KEY_PATH, null));
                rule.setRoles(Arrays.asList(restrictionConfig.getString(STUDIO_SECURITY_RESTRICTION_CONFIG_KEY_ROLES, "")
                        .trim().split("\\s*,\\s*")));
                rules.add(rule);
            });
        }

        return rules;
    }

    /**
     * Get request URI
     * @param request a request to Studio
     * @return request URI
     */
    protected String getRequestUri(HttpServletRequest request) {
        return request.getRequestURI().replace(request.getContextPath(), "");
    }

    /**
     * Get request `path` parameter
     * @param request a request to Studio
     * @return `path` parameter if existed, null otherwise
     */
    protected String getPathParam(HttpServletRequest request) {
        String requestPath = request.getParameter("path");
        if (requestPath != null) {
            // replace multiple slash to one slash
            requestPath = requestPath.replaceAll("/+", "/");
            if (!requestPath.startsWith("/")) {
                // adding a prefix slash to match with yaml configuration
                requestPath = "/" + requestPath;
            }
        }

        return requestPath;
    }

    /**
     * Get request `site` parameter
     * Get from `site_id` or `site` parameter. If not, try getting from cookie
     * @param request a request to Studio
     * @return site parameter if existed, null otherwise
     */
    protected String getSiteParam(HttpServletRequest request) {
        String siteParam = request.getParameter("site_id");
        if (StringUtils.isEmpty(siteParam)) {
            siteParam = request.getParameter("site");
        }
        if (StringUtils.isEmpty(siteParam)) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("crafterSite")) {
                        siteParam = cookie.getValue();
                        break;
                    }
                }
            }
        }

        return siteParam;
    }

    private class StudioRestrictionRule {
        protected String url;
        protected String path;
        protected List<String> roles;

        public void setUrl(String url) {
            this.url = url;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
