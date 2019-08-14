
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
package scripts.api.impl.site

import org.craftercms.studio.api.v2.service.notification.NotificationMessageType;
import java.util.Locale;

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

    def writeConfiguration(path, content){
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.writeConfiguration(path, content);
    }

    def writeConfiguration(site, path, content){
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.writeConfiguration(site, path, content);
    }

    def getConfiguraiton(path) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getConfiguration(site, path);
    }

    def getConfiguration(site, path, applyEnvironment) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getConfiguration(site, path, applyEnvironment);
    }

    def getAllAvailableSites() {
         def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getAllAvailableSites()
    }

    def createSiteFromBlueprint(blueprintName, siteName, siteId, sandboxBranch, desc) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.createSiteFromBlueprint(blueprintName, siteName, siteId, sandboxBranch, desc)
    }

    def createSiteWithRemoteOption(siteId, sandboxBranch, description, blueprint, remoteName, remoteUrl, remoteBranch,
                                   singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
                                   remotePrivateKey, createOption) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.createSiteWithRemoteOption(siteId, sandboxBranch, description, blueprint, remoteName,
                remoteUrl, remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
                remotePrivateKey, createOption)
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

    def getSitesPerUserTotal(username) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getSitesPerUserTotal(username)
    }

    def getSitesPerUser(username, start, number) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.getSitesPerUser(username, start, number)
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

    /**
     * Cancel failed pull from remote repository for given site
     *
     * @param siteId site identifier
     * @return
     */
    def cancelFailedPull(siteId) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.cancelFailedPull(siteId)
    }

    /**
     * Commit a resolved set of conflicts for a site
     *
     * @param context container for passing request, token and other values that may be needed by the implementation
     * @param siteId site identifier
     * @param commitMessage commit message
     * @return repository status
     */
    def commitResolution(siteId, commitMessage) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.commitResolution(siteId, commitMessage)
    }

    /**
     * Get the difference between ours and theirs for a conflicted file for a site
     *
     * @param siteId site identifier
     * @param path path of the conflicted file
     * @return
     */
    def diffConflictedFile(siteId, path) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.diffConflictedFile(siteId, path)
    }

    /**
     * Resolve a conflict for a file by accepting ours or theirs
     *
     * @param siteId site identifier
     * @param path path of the conflicted file
     * @param resolution resolution strategy
     * @return
     */
    def resolveConflict(siteId, path, resolution) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.resolveConflict(siteId, path, resolution)
    }

    /**
     * Get repository status for conflicts
     *
     * @param siteId site identifier
     * @return
     */
    def repositoryStatus(siteId) {
        def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
        return springBackedService.repositoryStatus(siteId)
    }
}
