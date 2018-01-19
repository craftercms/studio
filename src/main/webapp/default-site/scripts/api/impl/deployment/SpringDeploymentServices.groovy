package scripts.api.impl.deployment

class SpringDeploymentServices {

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
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType)
    }

    def getScheduledItems(site, sort, ascending, subSort, subAscending, filterType) {

        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.getScheduledItems(site, sort, ascending, subSort, subAscending, filterType)
    }

    def getAvailablePublishingChannelGroups(site, path) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.getAvailablePublishingChannelGroups(site, path)
    }

    def syncAllContentToPreview(site) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.syncAllContentToPreview(site, false)
    }

    def getDeploymentEndpoints(site) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.getDeploymentEndpoints(site)
    }

    def cancelDeployment(site, path, deploymentId) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.cancelDeployment(site, path, deploymentId)
    }

    def bulkGoLive(site, environment, path) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.bulkGoLive(site, environment, path)
    }

    def getPublishStatus(siteId) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.getPublishStatus(siteId)
    }

    def enablePublishing(siteId, enabled) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.enablePublishing(siteId, enabled)
    }

    def publishCommits(siteId, environment, commitIds) {
        def springBackedService = this.context.applicationContext.get("cstudioDeploymentService")
        return springBackedService.publishCommits(siteId, environment, commitIds)
    }
}
