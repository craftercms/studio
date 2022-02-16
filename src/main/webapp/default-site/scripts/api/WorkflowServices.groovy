
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

/**
 * workflow services
 */
@Log
class WorkflowServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    /**
     * get the items that are in flight, waiting for approval for a site
     * @param site - the project ID
     * @param filter - filters to apply to listing
     */
    def getItemsWaitingReview(site, filter) {

    }

    /**
     * submit content in to workflow
     * @param site - the project ID
     * @param items - items to submit
     * @param workflowID - id of workflow
     * @param deploymentOptions - deployment options
     */
    def submitContent(site, contentItems, workflowId, deploymentOptions) {
    }

    /**
     * reject content in workflow (workflow controls step, this method simply sends signal)
     * @param site - the project ID
     * @param items - items to submit
     * @param workflowID - id of workflow
     */
    def rejectContent(site, contentItems, workflowID) {
    }

    /**
     * approve content in workflow (workflow controls step, this method simply sends signal)
     * @param site - the project ID
     * @param items - items to submit
     * @param workflowID - id of workflow
     */
    def approveContent(site, contentItems, workflowID) {
    }

    /**
     * set the state of an object
     * @param site - the project ID
     * @param path - path of item
     * @param state - state to set
     */
    def setObjectState(site, path, state) {

    }

    /**
     * get a user's activity history
     * @param site - the project ID
     * @param userId - id of the user
     */
    def getUserActivities(site, userId) {

    }

    static getInProgressItems(context, site, sort, ascending, inProgressOnly) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        workflowServicesImpl.getInProgressItems(site, sort, ascending, inProgressOnly);
    }

    static getGoLiveItems(context, site, sort, ascending) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        workflowServicesImpl.getGoLiveItems(site, sort, ascending);
    }

    static goDelete(context, site, requestBody) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        return workflowServicesImpl.goDelete(site, requestBody);
    }

    static goLive(context, site, requestBody) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        return workflowServicesImpl.goLive(site, requestBody);
    }
    static submitToGoLive(context, site, user, requestBody) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        return workflowServicesImpl.submitToGoLive(site, user, requestBody);
    }

    static reject(context, site, requestBody) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        return workflowServicesImpl.reject(site, requestBody);
    }
}