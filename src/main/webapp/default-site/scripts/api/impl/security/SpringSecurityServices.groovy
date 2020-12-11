/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package scripts.api.impl.security

class SpringSecurityServices {

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringSecurityServices(context) {
        this.context = context
    }

    def getUserPermissions(site, path, groups) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserPermissions(site, path, groups)
    }

    def getCurrentUser(user) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getCurrentUser()
    }

    def getUserProfile(user) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserProfile(user)
    }

    def getUserRoles(site) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserRoles(site)
    }

    def authenticate(username, password) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.authenticate(username, password)
    }

    def validateTicket(token) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.validateTicket(token)
    }

    def logout() {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.logout()
    }

    def createUser(username, password, firstName, lastName, email) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.createUser(username, password, firstName, lastName, email)
    }

    def deleteUser(username) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.deleteUser(username)
    }

    def updateUser(username,  firstName, lastName, email) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.updateUser(username, firstName, lastName, email)
    }

    def getUserDetails(username) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserProfile(username)
    }

    def enableUser(username, enabled) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.enableUser(username, enabled)
    }

    def getUserStatus(username) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserStatus(username)
    }

    def getAllUsers(start, number) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getAllUsers(start, number)
    }

    def getAllUsersTotal() {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getAllUsersTotal()
    }

    def getUsersPerSite(site, start, number) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUsersPerSite(site, start, number)
    }

    def getUsersPerSiteTotal(site) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUsersPerSiteTotal(site)
    }

    def createGroup(groupName, description, siteId) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.createGroup(groupName, description, siteId)
    }

    def getGroup(siteId, groupName) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getGroup(siteId, groupName)
    }

    def getAllGroups(start, number) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getAllGroups(start, number)
    }

    def getGroupsPerSite(siteId, start, number) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getGroupsPerSite(siteId, start, number)
    }

    def getGroupsPerSiteTotal(siteId) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getGroupsPerSiteTotal(siteId)
    }

    def getUsersPerGroup(siteId, groupName, start, number) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUsersPerGroup(siteId, groupName, start, number)
    }

    def getUsersPerGroupTotal(siteId, groupName) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUsersPerGroupTotal(siteId, groupName)
    }

    def updateGroup(siteId, groupName, description) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.updateGroup(siteId, groupName, description)
    }

    def deleteGroup(siteId, groupName) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.deleteGroup(siteId, groupName)
    }

    def addUserToGroup(siteId, groupName, username) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.addUserToGroup(siteId, groupName, username)
    }

    def removeUserFromGroup(siteId, groupName, username) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.removeUserFromGroup(siteId, groupName, username)
    }
    
    def validateToken(token) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.validateToken(token)
    }

    def validateSession(request) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.validateSession(request)
    }
}
