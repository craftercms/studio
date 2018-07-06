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

    /** 
     * Return deployment history for a give nsite
     * @param site - the project ID
     * @param daysFromToday - number of days back to get
     * @param numberOfItems - number of items to get 
     * @param sort - field to sort on
     * @param ascending - true or false
     * @param filterType - pages/components/all 
     * @paran context - container for passing request, token and other values that may be needed by the implementation
     */
    static getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType, context) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending,
                filterType)
    }


    static getScheduledItems(context, site, sort, ascending, subSort, subAscending, filterType) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        deploymentServicesImpl.getScheduledItems(site, sort, ascending, subSort, subAscending, filterType)
    }

    static getAvailablePublishingChannelGroups(context, site, path) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.getAvailablePublishingChannelGroups(site, path)
    }

    static bulkGoLive(context, site, environment, path) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.bulkGoLive(site, environment, path)
    }

    static getStatus(context, siteId) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        return deploymentServicesImpl.getPublishStatus(siteId)
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
