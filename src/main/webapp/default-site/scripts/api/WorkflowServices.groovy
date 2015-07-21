
package scripts.api

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )
@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.1' )

import scripts.api.ServiceFactory
import groovy.util.logging.Log

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
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
	 * get workflow jobs
	 * @param site - the project ID
	 */
	static getWorkflowJobs(context, site) {
		def workflowServicesImpl = ServiceFactory.getWorkflowServices(context)
		return workflowServicesImpl.getWorkflowJobs(site)
	}

	/**
	 * create a workflow job
	 * @param site - the project ID
	 * @param items - collection of items
	 * @param workflowID - id of workflow
	 */
	static createWorkflowJob(context, site, items, workflowId, properties) {
		def workflowServicesImpl = ServiceFactory.getWorkflowServices(context)
		def ret = ""
		def getNodeRefResponse = ""
		def furl = ""

		if(workflowId.indexOf("activiti") != -1) {
			// lookup item node refs
			def alfrescoConnector = context.applicationContext.get("cstudioAlfrescoContentRepository")
			def alfItems = []
			def assignee = "admin"
			def processDefinitionId = workflowId //"activitiAdhoc:1:4"
			def processDefinitionKey = processDefinitionId.substring(processDefinitionId.indexOf(":"))

			def host = alfrescoConnector.getAlfrescoUrl() //"http://127.0.0.1:7080"
		    def alfTicket = alfrescoConnector.getAlfTicket()
 			def http = new HTTPBuilder(host)


		    def serviceGetNodeID = "/alfresco/service/api/nodelocator/xpath"
		    for(int i=0; i<items.size; i++) {
		    	def cmsPath = items[i] 
		    	def contentPath = "/wem-projects/"+site+"/"+site+"/work-area"+cmsPath
		    	def alfPath = "/app:company_home" + contentPath.replaceAll("/", "/cm:")

				http.get(
 					path: serviceGetNodeID,
 					query: ["alf_ticket": alfTicket, "query": alfPath],
 				) { resp, reader -> [reader,resp.status]
 					getNodeRefResponse = reader
 					getNodeRefResponse = ""+getNodeRefResponse.data.nodeRef
					getNodeRefResponse = getNodeRefResponse.replace("workspace://SpacesStore/","")
					alfItems.add(getNodeRefResponse)
 				}
 
		    }

		    def servicePath = "/alfresco/api/-default-/public/workflow/versions/1/processes"

		    def serviceBody = "{" + 
	   				"\"processDefinitionId\":\""+processDefinitionId+"\"," +
					"\"processDefinitionKey\": \""+processDefinitionKey+"\"," +
	   				"\"variables\":{" +
		      			"\"bpm_assignee\":\""+ assignee +"\"" +
	   				"}," +
	   				"\"items\":["

					    for(int j=0; j<alfItems.size; j++) {
					    	serviceBody += "\""+alfItems[j]+"\""

					    	if(j<(alfItems.size-1)) {
					    		serviceBody += ","
					    	} 
					    }	   				
	   			serviceBody += "]}"
		    
			try{
 				http.post(
 					path: servicePath,
 					query: ["alf_ticket": alfTicket],
 					body: serviceBody
 				) { resp, reader -> [reader,resp.status]
 					ret = reader
 				}

	            return ret;    
			}
			catch(err) {
				throw new Exception ("error starting Alfresco workflow: "+err+":"+ret)
			}
		}
		else {
			return workflowServicesImpl.createWorkflowJob(context, site, items, workflowId, properties)
		}
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

	static getWorkflowAffectedPaths(context, site, path) {
		def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
		workflowServicesImpl.getWorkflowAffectedPaths(site, path);
	}

    static goDelete(context, site, requestBody, user) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        return workflowServicesImpl.goDelete(site, requestBody, user);
    }

    static goLive(context, site, requestBody) {
        def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
        return workflowServicesImpl.goLive(site, requestBody);
    }
	static submitToGoLive(context, site, user, requestBody) {
		def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
		return workflowServicesImpl.submitToGoLive(site, user, requestBody);
	}

	static submitToDelete(context, user, site, requestBody) {
		def workflowServicesImpl = ServiceFactory.getWorkflowServices(context);
		return workflowServicesImpl.submitToDelete(site, user, requestBody);
	}
}