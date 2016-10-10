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
	public siteConfig = null
	public username = null
	public password = null

	// https://docs.alfresco.com/activiti/docs/dev-guide/1.4.0/#_rest_api
	public String REST_UNKNOWN = "UNKOWN"
	public String REST_ENT_GET_PROCESSES = getActivitiAppName() + "UNKOWN"
	public String REST_COM_GET_PROCESSES = REST_UNKNOWN
	
	public String REST_ENT_GET_TASKS = getActivitiAppName() + "/api/enterprise/tasks/query"
	public String REST_COM_GET_TASKS = REST_UNKNOWN
	
	public String REST_ENT_GET_TASK_FORM = getActivitiAppName() + "/api/enterprise/task-forms/"
	public String REST_COM_GET_TASK_FORM = REST_UNKNOWN                           
	
	public String REST_ENT_SUBMIT_TASK = REST_ENT_GET_TASK_FORM 
	public String REST_COM_SUBMIT_TASK = REST_UNKNOWN
	
	public String REST_ENT_GET_PROC_DEFS = getActivitiAppName() + "/api/enterprise/process-definitions"
	public String REST_COM_GET_PROC_DEFS = REST_UNKNOWN
	
	public String REST_ENT_START_PROC = getActivitiAppName() + "/api/enterprise/process-instances"
	public String REST_COM_START_PROC = REST_UNKNOWN

	/**
	 * create a activiti connection
	 */
	public Activiti(username, password, logger, siteConfig) {
		this.logger = logger
		this.siteConfig = siteConfig
		this.username = username
		this.password = password
	}

	public getProcessDefs() {
		def url = (isEnterprise()) ? REST_ENT_GET_PROC_DEFS  : REST_COM_GET_PROC_DEFS 

		return doRequest(url, Method.GET, null)
	}

	public startProcess(processId, name) {
        def url = (isEnterprise()) ? REST_ENT_START_PROC : REST_ENT_START_PROC
        def body =  [ processDefinitionId:processId, name:name]

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
		logDebug("calling [${methodType}] for url [${serviceUrl}] with body [$reqBody]")

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
		return (getConfigValue("activiti.hostBaseUrl", "true") == "true")
	}

	public getHostBaseUrl() {
		return getConfigValue("activiti.hostBaseUrl", "http://localhost:8080")
	}

	public getUserName() {
		return (this.username!=null) ? this.username : "admin@app.activiti.com"
	}

	public getPassword() {
		return (this.password!=null) ? this.password : "admin"
	}

	public getActivitiAppName() {
		return getConfigValue("activiti.appName", "/activiti-app")
	}

	public getConfigValue(key, defaultValue) {
		return (siteConfig) ? siteConfig.getString(key, defaultValue) : defaultValue
	}

	public logDebug(message) {
		if(logger) logger.debug(""+message)
	}

	public logInfo(message) {
		if(logger) logger.info(""+message)
	}
}