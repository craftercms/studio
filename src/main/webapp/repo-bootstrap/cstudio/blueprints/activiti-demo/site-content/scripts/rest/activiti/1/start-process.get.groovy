import scripts.crafter.ext.activiti.Activiti

def activitiUserName = null
def activitiPassword = null
def processKey = params.processDef

if(profile) {
    activitiUserName = profile.activitiUserName
    activitiPassword = profile.activitiPassword
}

def activitiAPI = new Activiti(activitiUserName, activitiPassword, logger, siteConfig)

return activitiAPI.startProcess(processKey, processKey + " : " + new Date())