
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
package scripts.api.impl.workflow

/**
 * @author Dejan Brkic
 */

class SpringWorkflowServices {

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringWorkflowServices(context) {
        this.context = context;
    }

    def getInProgressItems(site, sort, ascending, inProgressOnly) {
        def springBackedService = this.context.applicationContext.get("cstudioWorkflowService");
        springBackedService.getInProgressItems(site, sort, ascending, inProgressOnly);
    }

    def getGoLiveItems(site, sort, ascending) {
        def springBackedService = this.context.applicationContext.get("cstudioWorkflowService");
        springBackedService.getGoLiveItems(site, sort, ascending);
    }

    def goDelete(site, requestBody) {
        def springBackedService = this.context.applicationContext.get("cstudioWorkflowService");
        springBackedService.goDelete(site, requestBody);
    }

    def goLive(site, requestBody) {
        def springBackedService = this.context.applicationContext.get("cstudioWorkflowService");
        springBackedService.goLive (site, requestBody);
    }

    def submitToGoLive(site, user, requestBody) {
        def springBackedService = this.context.applicationContext.get("cstudioWorkflowService");
        springBackedService.submitToGoLive (site, user, requestBody);
    }

    def reject(site, requestBody) {
        def springBackedService = this.context.applicationContext.get("cstudioWorkflowService");
        return springBackedService.reject(site, requestBody);
    }

    /**
     * create a workflow job
     * @param site - the project ID
     * @param items - collection of items
     * @param workflowID - id of workflow
     */
    def createWorkflowJob(site, items, workflowId, properties) {
        def springBackedService = this.context.applicationContext.get("cstudioWorkflowService");
        return springBackedService.createJob(site, items, workflowId, properties);
    }
}

