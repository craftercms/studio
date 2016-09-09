package scripts.crafter.ext.activiti

@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7')
@Grab('oauth.signpost:signpost-core:1.2.1.2')
@Grab('oauth.signpost:signpost-commonshttp4:1.2.1.2')

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient

public class Activiti {

	public logger = null

	public String REST_ENT_GET_PROCESSES = getActivitiAppName() + "UNKOWN"
	public String REST_COM_GET_PROCESSES = "UNKOWN"
	
	public String REST_ENT_GET_TASKS = getActivitiAppName() + "/api/enterprise/tasks/query"
	public String REST_COM_GET_TASKS = "/activiti-app/api/enterprise/tasks/query"
	
	public String REST_ENT_GET_TASK_FORM = getActivitiAppName() + "/api/enterprise/task-forms/"
	public String REST_COM_GET_TASK_FORM = "/activiti-app/api/enterprise/task-forms/"
	
	public String REST_ENT_SUBMIT_TASK = getActivitiAppName() + "/api/enterprise/task-forms/"
	public String REST_COM_SUBMIT_TASK = "/activiti-app/api/enterprise/task-forms/"
	
	public String REST_ENT_GET_PROC_DEFS = getActivitiAppName() + "/api/enterprise/process-definitions"
	public String REST_COM_GET_PROC_DEFS = "/activiti-app/api/enterprise/process-definitions"
	
	public String REST_ENT_START_PROC = getActivitiAppName() + "/api/enterprise/process-instances"
	public String REST_COM_START_PROC = "/activiti-app/api/enterprise/process-instances"

	/**
	 * create a activiti connection
	 */
	public Activiti(logger) {
		this.logger = logger
	}

	public getProcesses() {
		return []
	}

	public startProcess(processKey, name) {
		def url = (isEnterprise()) ? REST_ENT_GET_PROC_DEFS : REST_ENT_GET_PROC_DEFS 
		def procDefs = doRequest(url, Method.GET, null)
        def procDef = procDefs.data[0]
        def procId = procDef.id

        url = (isEnterprise()) ? REST_ENT_START_PROC : REST_ENT_START_PROC
        def body =  [ processDefinitionId:procId, name:name]

		return doRequest(url, Method.POST, body)
    }

	public getTasksForUser(user) {
		def url = (isEnterprise()) ? REST_ENT_GET_TASKS : REST_COM_GET_TASKS
		def body = [ assignee: user ]

		return doRequest(url, Method.POST, body)
	}

	public getFormDefForTask(taskId) {
		def url = ((isEnterprise()) ? REST_ENT_GET_TASK_FORM : REST_COM_GET_TASK_FORM) + taskId
		return doRequest(url, Method.GET, null)
	}

	public submitForm(taskId, values) {
		def url = ((isEnterprise()) ? REST_ENT_SUBMIT_TASK : REST_COM_SUBMIT_TASK) + taskId
		def body = [ values: values]

		return doRequest(url, Method.POST, body)
	}

	/**
	 * Make the actual REST call
	 */
	public doRequest(serviceUrl, methodType, reqBody) {
		logInfo("calling [${methodType}] for url [${serviceUrl}] with body [$reqBody]")

		def ret = null
		def http = new HTTPBuilder(getHostBaseUrl())
		http = authenticate(http)

		http.request(methodType) {
    		
    		uri.path = serviceUrl
    		requestContentType = ContentType.JSON
    		if(methodType==Method.POST) body = reqBody
    
    		response.success = { resp, reader -> ret = reader }
		}

		return ret
	}	

	/**
	 * authenticate the request object
	 */
	public authenticate(http) {
		def user = getUserName()
		def password = getPassword()
		def authPair = user + ":" + password
		def authEncoded = authPair.bytes.encodeBase64().toString()

		http.setHeaders([Authorization: "Basic "+authEncoded])	
		
		return http	
	}

	public isEnterprise() {
		return true
	}

	public getHostBaseUrl() {
		return "http://localhost:8080"
	}

	public getUserName() {
		return "russ.danner@craftersoftware.com"
	}

	public getPassword() {
		return "crafter"
	}

	public getActivitiAppName() {
		return "/activiti-app"
	}

	public logInfo(message) {
		if(logger) logger.info(""+message)
	}
}