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

    /** 
     * Return deployment history for a given site
     * @param site - the project ID
     * @param daysFromToday - number of days back to get
     * @param numberOfItems - number of items to get 
     * @param sort - field to sort on
     * @param ascending - true or false
     * @param filterType - pages/components/all 
     * @oaran context - container for passing request and other values that may be needed by the implementation
     */
    def getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType)
    }

    def getScheduledItems(site, sort, ascending, subSort, subAscending, filterType) {

        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.getScheduledItems(site, sort, ascending, subSort, subAscending, filterType)
    }

    def getAvailablePublishingChannelGroups(site, path) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.getAvailablePublishingChannelGroups(site, path)
    }

    def syncAllContentToPreview(site) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.syncAllContentToPreview(site, false)
    }

    def bulkGoLive(site, environment, path) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.bulkGoLive(site, environment, path)
    }

    def getPublishStatus(siteId) {
        def springBackedService = this.context.applicationContext.get(DEPLOYMENT_SERVICES_BEAN)
        return springBackedService.getPublishStatus(siteId)
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
