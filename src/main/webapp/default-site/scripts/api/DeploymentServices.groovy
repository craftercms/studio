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

    static getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType) {
        def deploymentServicesImpl = ServiceFactory.getContentServices(context)
        return deploymentServicesImpl.getDeploymentHistory(site, daysFromToday, numberOfItems, sort, ascending, filterType)
    }

    /** 
     * get the scheduled items for a site
     * @param site - the project ID
     * @param filter - filters to apply to listing
     */ 
    static getScheduledItems(site, filter) {

    }

}
