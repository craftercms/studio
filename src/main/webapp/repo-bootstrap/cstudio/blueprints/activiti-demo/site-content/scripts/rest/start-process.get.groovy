import scripts.crafter.ext.activiti.Activiti

def activitiAPI = new Activiti(logger, siteConfig)

return activitiAPI.startProcess("doesntmatteryet", "My Process")