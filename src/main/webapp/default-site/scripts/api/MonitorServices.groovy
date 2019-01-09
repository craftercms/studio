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

package scripts.api

import scripts.api.ServiceFactory

import groovy.util.logging.Log

@Log
class MonitorServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    static getVersion(context) {
        def monitorServiceImpl = ServiceFactory.getMonitorServices(context);
        def manifestToRead=new java.util.jar.Manifest(
                context.applicationContext.servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"))
        return monitorServiceImpl.getVersion(manifestToRead)
    }

    static getStatus(context) {
        def monitorServiceImpl = ServiceFactory.getMonitorServices(context);
        return monitorServiceImpl.getStatus();
    }

    static getMemory(context) {
        def monitorServiceImpl = ServiceFactory.getMonitorServices(context);
        return monitorServiceImpl.getMemory();
    }
}
