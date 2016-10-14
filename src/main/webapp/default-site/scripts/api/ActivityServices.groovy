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
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created by dejanbrkic on 11/27/14.
 */
package scripts.api

import scripts.api.ServiceFactory
import groovy.util.logging.Log

@Log
class ActivityServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    static getActivities(context, site, user, num, sort, ascending, excludeLive, filterType) {
        def activitiesServicesImpl = ServiceFactory.getActivityServices(context)
        return activitiesServicesImpl.getActivities(site, user, num, sort, ascending, excludeLive, filterType);
    }

    static postActivity(context, site, user, path, activityType, extraInfo) {
        def activitiesServicesImpl = ServiceFactory.getActivityServices(context)
        return activitiesServicesImpl.postActivity(site, user, path, activityType, extraInfo)
    }

    static getAuditLog(context, site, startPos, num) {
        def activitiesServicesImpl = ServiceFactory.getActivityServices(context);
        return activitiesServicesImpl.getAuditLog(site, startPos, num);
    }
}
