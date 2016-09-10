import scripts.crafter.ext.activiti.Activiti

import groovy.json.JsonSlurper

def postBody = ""
def formData = []
def reader = request.getReader()
def content = reader.readLine()
def activitiUserName = null
def activitiPassword = null

if(profile) {
	activitiUserName = profile.activitiUserName
	activitiPassword = profile.activitiPassword
}

while (content != null) {
    postBody += content
    content = reader.readLine()
}

formData = new JsonSlurper().parseText(postBody)

def activitiAPI = new Activiti(activitiUserName, activitiPassword, logger, siteConfig)

return activitiAPI.submitForm(params.taskId, formData)