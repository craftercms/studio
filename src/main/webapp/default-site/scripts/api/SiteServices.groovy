/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

    static getAllAvailableSites(context) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getAllAvailableSites()
    }

    static createSiteFromBlueprint(context, blueprintName, siteName, siteId, desc) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.createSiteFromBlueprint(blueprintName, siteName, siteId, desc)
    }

    static createSiteWithRemoteOption(context, siteId, description, blueprint, remoteName, remoteUrl,
                                      authenticationType, remoteUsername, remotePassword, remoteToken,
                                      remotePrivateKey, createOption) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.createSiteWithRemoteOption(siteId, description, blueprint, remoteName, remoteUrl,
                authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey, createOption)
    }

    static deleteSite(context, siteId) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.deleteSite(siteId)
    }

    static getAvailableBlueprints(context) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getAvailableBlueprints()
    }

    static reloadSiteConfiguration(context, site) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.reloadSiteConfiguration(site)
    }

    static syncRepository(context, site) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.syncRepository(site)
    }

    static rebuildDatabase(context, site) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.rebuildDatabase(site)
    }

    static exists(context, site) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.exists(site)
    }

    static getSitesPerUserTotal(context, username) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getSitesPerUserTotal(username)
    }

    static getSitesPerUser(context, username, start, number) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getSitesPerUser(username, start, number)
    }

    static getSite(context, siteId) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getSite(siteId)
    }

    /**
     * Add remote
     * @param context container for passing request, token and other values that may be needed by the implementation
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteUrl remote url
     * @param authenticationType authentication type
     * @param remoteUsername remote username
     * @param remotePassword remote password
     * @param remoteToken remote token
     * @param remotePrivateKey remote private key
     * @return
     */
    static addRemote(context, siteId, remoteName, remoteUrl, authenticationType, remoteUsername, remotePassword,
                     remoteToken, remotePrivateKey) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.addRemote(siteId, remoteName, remoteUrl, authenticationType, remoteUsername,
                remotePassword, remoteToken, remotePrivateKey)
    }

    /**
     * Remove remote
     * @param context container for passing request, token and other values that may be needed by the implementation
     * @param siteId site identifier
     * @param remoteName remote name
     * @return
     */
    static removeRemote(context, siteId, remoteName) {
        def siteServiceImpl = ServiceFactory.getSiteServices(context)
        return siteServiceImpl.removeRemote(siteId, remoteName)
    }

    /**
     * List remote repositories
     *
     * @param context container for passing request, token and other values that may be needed by the implementation
     * @param siteId site identifier
     * @return
     */
    static listRemote(context, siteId) {
        def siteServiceImpl = ServiceFactory.getSiteServices(context)
        return siteServiceImpl.listRemote(siteId)
    }
}
