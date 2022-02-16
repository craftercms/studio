
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
package scripts.api.impl.site

import org.craftercms.studio.api.v2.service.notification.NotificationMessageType;

class SpringSiteServices {

    static SITE_SERVICES_BEAN = "cstudioSiteServiceSimple"
    static NOTIFICATION_SERVICES_BEAN = "cstudioNotificationService"
    static NEW_NOTIFICATION_SERVICES_BEAN = "cstudioNotificationService"

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringSiteServices(context) {
        this.context = context
    }

    def getAllAvailableSites() {
         def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getAllAvailableSites()
    }

    def createSiteFromBlueprint(blueprintName, siteId, siteName, sandboxBranch, desc, params, createAsOrphan) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.createSiteFromBlueprint(blueprintName, siteId, siteName, sandboxBranch, desc,
                params, createAsOrphan)
    }

    def createSiteWithRemoteOption(siteId, siteName, sandboxBranch, description, blueprint, remoteName, remoteUrl,
                                   remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword,
                                   remoteToken, remotePrivateKey, createOption, params, createAsOrphan) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.createSiteWithRemoteOption(siteId, siteName, sandboxBranch, description, blueprint,
                remoteName, remoteUrl, remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword,
                remoteToken, remotePrivateKey, createOption, params, createAsOrphan)
    }

    def deleteSite(siteId) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.deleteSite(siteId)
    }

    def getAvailableBlueprints() {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getAvailableBlueprints()
    }

    def reloadSiteConfiguration(site) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.reloadSiteConfiguration(site)
    }

    def getCannedMessage(site,messageKey,locale="us"){
        def notificationSystem=this.context.applicationContext.get(NEW_NOTIFICATION_SERVICES_BEAN)
        return  notificationSystem.getNotificationMessage(site,NotificationMessageType.CannedMessages,messageKey,Locale
                    .forLanguageTag(locale))
    }

    def syncRepository(site) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.syncRepository(site)
    }

    def rebuildDatabase(site) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.rebuildDatabase(site)
    }

    def exists(site) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.exists(site)
    }

    def getSitesPerUserTotal() {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getSitesPerUserTotal()
    }

    def getSitesPerUser(start, number) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getSitesPerUser(start, number)
    }

    def getSite(siteId) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getSite(siteId)
    }

    def addRemote(siteId, remoteName, remoteUrl, authenticationType, remoteUsername, remotePassword,
                  remoteToken, remotePrivateKey) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.addRemote(siteId, remoteName, remoteUrl, authenticationType,
                remoteUsername, remotePassword, remoteToken, remotePrivateKey)
    }

    def removeRemote(siteId, remoteName) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.removeRemote(siteId, remoteName)
    }

    def listRemote(siteId) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.listRemote(siteId)
    }
}
