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

import scripts.api.ServiceFactory

import groovy.util.logging.Log

@Log
class DeploymentServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    static getScheduledItems(context, site, sort, ascending, subSort, subAscending, filterType) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        deploymentServicesImpl.getScheduledItems(site, sort, ascending, subSort, subAscending, filterType)
    }

    static bulkGoLive(context, site, environment, path, comment) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.bulkGoLive(site, environment, path, comment)
    }

    static enablePublishing(context, siteId, enabled) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.enablePublishing(siteId, enabled)
    }

    static publishCommits(context, siteId, environment, commitIds, comment) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.publishCommits(siteId, environment, commitIds, comment)
    }

    static publishItems(context, site, environment, schedule, paths, submissionComment) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.publishItems(site, environment, schedule, paths, submissionComment)
    }

    static resetStagingEnvironment(context, siteId) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.resetStagingEnvironment(siteId)
    }
}
