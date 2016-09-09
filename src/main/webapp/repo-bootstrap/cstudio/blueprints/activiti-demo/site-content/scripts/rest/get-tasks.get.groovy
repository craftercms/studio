import scripts.crafter.ext.activiti.Activiti

def activitiAPI = new Activiti(logger, siteConfig)

return activitiAPI.getTasksForUser("russ.danner@craftersoftware.comd")