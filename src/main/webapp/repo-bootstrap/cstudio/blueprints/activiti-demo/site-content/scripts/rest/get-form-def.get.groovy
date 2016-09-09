import scripts.crafter.ext.activiti.Activiti

def activitiAPI = new Activiti(logger, siteConfig)

return activitiAPI.getFormDefForTask(params.taskId)