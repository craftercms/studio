/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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

package scripts.api

import groovy.util.logging.Log
import scripts.api.ServiceFactory

/**
 * Site Services
 */
@Log
class SiteServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    static writeConfiguration(context, path, content){
        
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.writeConfiguration(path, content)        
    }

    static writeConfiguration(context, site, path, content){
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.writeConfiguration(site, path, content)  
    }

    static getConfiguraiton(context, path, content) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.writeConfiguration(path, content)  
    }

    static getConfiguration(context, site, path, applyEnvironment) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getConfiguration(site, path, applyEnvironment)
    }

    static getUserSites(context, user) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getUserSites(user)
    }

    static createSiteFromBlueprint(context, blueprintName, siteName, siteId, desc) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.createSiteFromBlueprint(blueprintName, siteName, siteId, desc)
    }    

    static deleteSite(context, siteId) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.deleteSite(siteId)
    }

    static getAvailableBlueprints(context) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getAvailableBlueprints()
    }

}