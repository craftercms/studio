package org.craftercms.studio.impl.v1.web.filter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.CStudioConstants.STUDIO_SESSION_TOKEN_ATRIBUTE;

public class StudioAuthenticationFilter extends GenericFilterBean {

    protected int sessionTimeout;
    protected String[] publicUrls;
    protected SecurityService securityService;
    protected boolean ssoEnabled = false;
    protected String ssoHeaderName;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpReponse = (HttpServletResponse) servletResponse;

        String requestUrl = HttpUtils.getRequestUriWithoutContextPath(httpRequest);

        if (ArrayUtils.contains(publicUrls, requestUrl) || !StringUtils.startsWith(requestUrl, "/api/")) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            if (isAuthentcated()) {
                if (isSessionExpired(httpRequest)) {
                    securityService.logout();
                    httpReponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            } else {
                securityService.logout();
                httpReponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }

    private boolean isSessionExpired(HttpServletRequest request) {
        if (ssoEnabled) {
            String ssoUserName = request.getHeader(ssoHeaderName);
            return StringUtils.isEmpty(ssoUserName);
        } else {
            HttpSession httpSession = request.getSession();
            String sessionToken = (String) httpSession.getAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE);
            String user = securityService.getCurrentUser();
            if (StringUtils.isNotEmpty(sessionToken) && StringUtils.isNotEmpty(user)) {
                if (SessionTokenUtils.validateToken(sessionToken, user)) {
                    sessionToken = SessionTokenUtils.createToken(user, sessionTimeout);
                    httpSession.setAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE, sessionToken);
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isAuthentcated() {
        String token = securityService.getCurrentToken();
        String user = securityService.getCurrentUser();
        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(token)) {
            return false;
        }
        Map<String, String> userProfile = securityService.getUserProfile(user);
        if (userProfile != null && !userProfile.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public String[] getPublicUrls() { return publicUrls; }
    public void setPublicUrls(String[] publicUrls) { this.publicUrls = publicUrls; }

    public int getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public boolean isSsoEnabled() { return ssoEnabled; }
    public void setSsoEnabled(boolean ssoEnabled) { this.ssoEnabled = ssoEnabled; }

    public String getSsoHeaderName() { return ssoHeaderName; }
    public void setSsoHeaderName(String ssoHeaderName) { this.ssoHeaderName = ssoHeaderName; }
}
