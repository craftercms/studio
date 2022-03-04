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

    static getAllAvailableSites(context) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getAllAvailableSites()
    }

    static createSiteFromBlueprint(context, blueprintName, siteId, siteName, sandboxBranch, desc, params,
                                   createAsOrphan) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.createSiteFromBlueprint(blueprintName, siteId, siteName, sandboxBranch, desc, params,
                createAsOrphan)
    }

    static createSiteWithRemoteOption(context, siteId, siteName, sandboxBranch, description, blueprint, remoteName,
                                      remoteUrl, remoteBranch, singleBranch, authenticationType, remoteUsername,
                                      remotePassword, remoteToken, remotePrivateKey, createOption, params,
                                      createAsOrphan) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.createSiteWithRemoteOption(siteId, siteName, sandboxBranch, description, blueprint,
                remoteName, remoteUrl, remoteBranch, singleBranch,authenticationType, remoteUsername, remotePassword,
                remoteToken, remotePrivateKey, createOption, params, createAsOrphan)
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

    static getCannedMessage(context, site,key,locale="en") {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getCannedMessage(site,key,locale)
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

    static getSitesPerUserTotal(context) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getSitesPerUserTotal()
    }

    static getSitesPerUser(context, start, number) {
        def siteServicesImpl = ServiceFactory.getSiteServices(context)
        return siteServicesImpl.getSitesPerUser(start, number)
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
    static addRemote(context, siteId, remoteName, remoteUrl, authenticationType, remoteUsername,
                     remotePassword, remoteToken, remotePrivateKey) {
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
