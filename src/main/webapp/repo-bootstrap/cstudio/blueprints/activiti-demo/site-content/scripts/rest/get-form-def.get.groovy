import scripts.crafter.ext.activiti.Activiti

def activitiAPI = new Activiti(logger)

return activitiAPI.getFormDefForTask(params.taskId)