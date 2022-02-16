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

package scripts.api.impl.deployment

class SpringDeploymentServices {

    static DEPLOYMENT_SERVICES_BEAN = "cstudioDeploymentService"

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringDeploymentServices(context) {
        this.context = context
    }

    def getScheduledItems(site, sort, ascending, subSort, subAscending, filterType) {

        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.getScheduledItems(site, sort, ascending, subSort, subAscending, filterType)
    }

    def syncAllContentToPreview(site) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.syncAllContentToPreview(site, false)
    }

    def bulkGoLive(site, environment, path, comment) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.bulkGoLive(site, environment, path, comment)
    }

    def enablePublishing(siteId, enabled) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.enablePublishing(siteId, enabled)
    }

    def publishCommits(siteId, environment, commitIds, comment) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.publishCommits(siteId, environment, commitIds, comment)
    }

    def publishItems(site, environment, schedule, paths, submissionComment) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.publishItems(site, environment, schedule, paths, submissionComment)
    }

    def resetStagingEnvironment(siteId) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.resetStagingEnvironment(siteId)
    }
}
