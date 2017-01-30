/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY without even the implied warranty of
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

    def getUserPermissions(site, path, user, groups) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserPermissions(site, path, user, groups)
    }

    def getCurrentUser(user) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getCurrentUser()
    }

    def getUserProfile(user) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserProfile(user)
    }

    def getUserRoles(site, user) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUserRoles(site, user)
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

    def getAllUsers() {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getAllUsers()
    }

    def getUsersPerSite(site) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUsersPerSite(site)
    }

    def createGroup(groupName, description, siteId) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.createGroup(groupName, description, siteId)
    }

    def getGroup(siteId, groupName) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getGroup(siteId, groupName)
    }

    def getAllGroups(start, end) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getAllGroups(start, end)
    }

    def getGroupsPerSite(siteId) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getGroupsPerSite(siteId)
    }

    def getUsersPerGroup(siteId, groupName, start, end) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.getUsersPerGroup(siteId, groupName, start, end)
    }

    def updateGroup(siteId, groupName, description) {
        def springBackedService = this.context.applicationContext.get("cstudioSecurityService")
        return springBackedService.updateGroup(siteId, groupName, description)
    }
}
