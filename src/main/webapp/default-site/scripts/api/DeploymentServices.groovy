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
        return deploymentServicesImpl.getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType)
    }


    static getScheduledItems(context, site, sort, ascending, subSort, subAscending, filterType) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context)
        deploymentServicesImpl.getScheduledItems(site, sort, ascending, subSort, subAscending, filterType);
    }

    static getAvailablePublishingChannelGroups(context, site, path) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context);
        return deploymentServicesImpl.getAvailablePublishingChannelGroups(site, path);
    }

    static getDeploymentQueue(context, site) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context);
        return deploymentServicesImpl.getDeploymentQueue(site);
    }

    static cancelDeployment(context, site, path, deploymentId) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context);
        return deploymentServicesImpl.cancelDeployment(site, path, deploymentId);
    }

    static bulkGoLive(context, site, environment, path) {
        def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context);
        return deploymentServicesImpl.bulkGoLive(site, environment, path);
    }
}
