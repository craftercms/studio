
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
package scripts.api.impl.activity


/**
 * @author Dejan Brkic
 */
class SpringActivityServices {

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringActivityServices(context) {
        this.context = context
    }

    def getActivities(site, user, num, sort, ascending, excludeLive, filterType) {

        def springBackedService = this.context.applicationContext.get("cstudioActivityService");
        return springBackedService.getActivities(site, user, num, sort, ascending, excludeLive, filterType);
    }

    def postActivity(site, user, path, activityType, activitySource, extraInfo) {
        def springBackedService = this.context.applicationContext.get("cstudioActivityService");
        return springBackedService.postActivity(site, user, path, activityType, activitySource, extraInfo);
    }

    def getAuditLog(site, start, number, user, actions) {
        def springBackedService = this.context.applicationContext.get("cstudioActivityService");
        return springBackedService.getAuditLogForSite(site, start, number, user, actions)
    }

    def getAuditLogTotal(site, user, actions) {
        def springBackedService = this.context.applicationContext.get("cstudioActivityService");
        return springBackedService.getAuditLogForSiteTotal(site, user, actions)
    }
}
