import scripts.crafter.ext.activiti.Activiti

def activitiAPI = new Activiti(logger)

return activitiAPI.getTasksForUser("russ.danner@craftersoftware.comd")