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
}
