import scripts.api.DeploymentServices;

def result = [:]
def site = params.site
def days = params.days.toInteger()
def num = params.num.toInteger()
def filterType = params.filterType

def context = DeploymentServices.createContext(applicationContext, request)

result.content = DeploymentServices.getDeploymentHistory(site, days, num, "eventDate", false, filterType, context)

return result