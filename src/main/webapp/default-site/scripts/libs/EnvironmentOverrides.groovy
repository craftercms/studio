/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package scripts.libs

import scripts.api.SiteServices;
import scripts.api.SecurityServices;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_TYPE;

class EnvironmentOverrides {
    static SITE_SERVICES_BEAN = "cstudioSiteServiceSimple"

    static getValuesForSite(appContext, request, response) {

        def result = [:]
        def serverProperties = appContext.get("studio.crafter.properties")
        def cookies = request.getCookies();

        def context = SiteServices.createContext(appContext, request)
        result.environment = serverProperties["environment"]

        def contextPath = request.getContextPath()
        if (contextPath.startsWith("/")) {
            contextPath = contextPath.substring(1)
        }
        result.studioContext = contextPath

        try {
            def siteServiceSB = context.applicationContext.get(SITE_SERVICES_BEAN)
            result.site = Cookies.getCookieValue("crafterSite", request)
            result.authoringServer =  siteServiceSB.getAuthoringServerUrl(result.site)
            result.previewServerUrl = siteServiceSB.getPreviewServerUrl(result.site)
            result.previewEngineServerUrl = siteServiceSB.getPreviewEngineServerUrl(result.site)
            result.graphqlServerUrl = siteServiceSB.getGraphqlServerUrl(result.site)

            result.user = SecurityServices.getCurrentUser(context)

            def studioConfigurationSB = context.applicationContext.get("studioConfiguration")
            def authenticationType = studioConfigurationSB.getProperty(SECURITY_TYPE)
            result.authenticationType = authenticationType

            def language = Cookies.getCookieValue("crafterStudioLanguage", request)
            if(language == null || language == "" || language == "UNSET") {
                language = "en"
            }
            result.language = language

            if(result.user == null){
                response.sendRedirect("/studio/#/login")
            }else{
                try{
                    def roles = SecurityServices.getUserRoles(context, result.site, result.user)

                    if(roles!=null && roles.size() > 0) {
                        if (roles.contains("admin")) {
                            result.role = "admin"
                        } else {
                            result.role = roles[0]
                        }
                    }
                    else {
                        response.sendRedirect("/studio/#/sites?siteValidation="+result.site)
                    }
                }catch(error){
                    response.sendRedirect("/studio/#/sites?siteValidation="+result.site)
                }
                def sites = SiteServices.getSitesPerUser(context, result.user, 0, 25)

                result.siteTitle = result.site +sites.size;

                for(int j = 0; j < sites.size; j++) {
                    def site = sites[j];
                    if(site.siteId == result.site) {
                        result.siteTitle = site.name;
                        break;
                    }
                }
            }
        }
        catch(err) {
            result.err = err
            throw new Exception(err)
        }

        return result;
    }
}
