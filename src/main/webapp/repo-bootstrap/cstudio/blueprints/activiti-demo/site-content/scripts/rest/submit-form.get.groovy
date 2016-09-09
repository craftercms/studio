import scripts.crafter.ext.activiti.Activiti

def activitiAPI = new Activiti(logger)

return activitiAPI.submitForm(params.taskId, [firstname: "Russ", lastname: "Danner", request: "A request"])