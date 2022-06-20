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

package scripts.libs

import org.craftercms.studio.api.v1.log.LoggerFactory
import scripts.api.SecurityServices
import scripts.api.SiteServices

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_PASSWORD_REQUIREMENTS_VALIDATION_REGEX
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.STUDIO_COOKIE_USE_BASE_DOMAIN

class EnvironmentOverrides {
  static USER_SERVICES_BEAN = "userService"
  static logger = LoggerFactory.getLogger(EnvironmentOverrides.class)

  static String getBaseDomain(String domainName) {
    def segments = domainName.split("\\.");

    if (segments == null || segments.length <= 2)
      return domainName;

    return segments[segments.length - 2] + "." + segments[segments.length - 1];
  }

  static getValuesForSite(appContext, request, response) {

    def result = [:]
    def serverProperties = appContext.get("studio.crafter.properties")

    def context = SiteServices.createContext(appContext, request)
    result.environment = serverProperties["environment"]

    def contextPath = request.getContextPath()
    if (contextPath.startsWith("/")) {
      contextPath = contextPath.substring(1)
    }
    result.studioContext = contextPath

    try {
      def userServiceSB = context.applicationContext.get(USER_SERVICES_BEAN)
      result.site = Cookies.getCookieValue("crafterSite", request)
      result.user = SecurityServices.getCurrentUser(context)

      def studioConfigurationSB = context.applicationContext.get("studioConfiguration")
      try {
        def authenticatedUser = userServiceSB.getCurrentUser()
        result.authenticationType = authenticatedUser.getAuthenticationType()
      } catch (error) {
        result.authenticationType = ""
      }

      result.passwordRequirementsRegex = studioConfigurationSB.getProperty(SECURITY_PASSWORD_REQUIREMENTS_VALIDATION_REGEX)
      result.useBaseDomain = studioConfigurationSB.getProperty(STUDIO_COOKIE_USE_BASE_DOMAIN)

      def language = Cookies.getCookieValue("crafterStudioLanguage", request)
      if (language == null || language == "" || language == "UNSET") {
        language = "en"
      }

      result.language = language

      if (result.user == null) {
        response.sendRedirect("/studio/login")
      } else {
        def sites = SiteServices.getSitesPerUser(context, 0, 25)
        if (sites.isEmpty()) {
          if (request.getRequestURI() != '/studio' && request.getRequestURI() != '/studio/') {
            response.sendRedirect("/studio/?noSites")
          }
        } else {

          if (result.site == "UNSET") {
            result.site = sites[0].siteId
            def useBaseDomain = result.useBaseDomain
            def hostname = request.getServerName()
            def domain = hostname

            if (useBaseDomain == "true") {
              def isHostnameIp = hostname ==~ /\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b/
              if (hostname.contains(".") && !isHostnameIp) {
                domain = getBaseDomain(hostname);
              }
            }

            Cookies.createCookie("crafterSite", sites[0].siteId, domain, "/", response)
          }

          try {
            def roles = SecurityServices.getUserRoles(context, result.site)
            if (roles != null && roles.size() > 0) {
              if (roles.contains("admin")) {
                result.role = "admin"
              } else {
                result.role = roles[0]
              }
            } else {
              logger.info("[EnvironmentOverrides] Attempt to visit '${result.site}' site without the necessary roles")
              response.sendRedirect("/studio/?roles")
            }
          } catch (error) {
            logger.info("[EnvironmentOverrides] Error retrieving roles for site '${result.site}'")
            response.sendRedirect("/studio/?error")
          }

          result.siteTitle = result.site + sites.size;

          for (int j = 0; j < sites.size; j++) {
            def site = sites[j]
            if (site.siteId == result.site) {
              result.siteTitle = site.name
              result.activeSite = site
              break;
            }
          }

        }
      }
    } catch (err) {
      result.err = err
      throw new Exception(err)
    }

    return result;
  }

  static getMinimalValuesForSite(appContext, request) {
    def result = [:]
    def context = SiteServices.createContext(appContext, request)
    def studioConfigurationSB = context.applicationContext.get("studioConfiguration")
    result.useBaseDomain = studioConfigurationSB.getProperty(STUDIO_COOKIE_USE_BASE_DOMAIN)
    return result
  }
}
