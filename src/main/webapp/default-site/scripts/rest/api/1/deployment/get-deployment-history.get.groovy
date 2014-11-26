import scripts.api.DeploymentServices;

def result = [:]
def site = params.site
def days = 1000//params.days.toInteger()
def num = 10 //params.num.toInteger()
def filterType = params.filterType

def context = DeploymentServices.createContext(applicationContext, request)

result.content = DeploymentServices.getDeploymentHistory(site, days, num, "eventDate", false, filterType, context)

return result