
import scripts.crafter.ext.activiti.Activiti

import groovy.json.JsonSlurper

def postBody = ""
def formData = []
def activitiUserName = null
def activitiPassword = null
def taskId = null
def result = null

if(profile) {
	activitiUserName = profile.activitiUserName
	activitiPassword = profile.activitiPassword
}

try {
	def reader = request.getReader()
	def content = reader.readLine()

	while(content != null) {
	    postBody += content
	    content = reader.readLine()
	}

	def postData = new JsonSlurper().parseText(postBody)
	taskId = postData.taskId
	formData = postData.data
}
catch(err) {
	logger.error("error parsing form body [${postBody}] due to error [${err}]")
}

try {
	def activitiAPI = new Activiti(activitiUserName, activitiPassword, logger, siteConfig)
	result = activitiAPI.submitForm(taskId, formData)
}
catch(err) {
	logger.error("error submitting task [${taskId}] with data [${formData}], ${err}")
}
return result