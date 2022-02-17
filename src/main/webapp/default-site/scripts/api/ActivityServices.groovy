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

    static getActivities(context, site, num, sort, ascending, excludeLive, filterType) {
        def activitiesServicesImpl = ServiceFactory.getActivityServices(context)
        return activitiesServicesImpl.getActivities(site, num, sort, ascending, excludeLive, filterType)
    }

}
