import scripts.api.DeploymentServices;

def result = [:]
def site = params.site
def days = params.days
def num = params.num
def filterType = prams.filterType

result.content = DeploymentServices.getDeploymentHistory(site, days, num, "eventDate", false,filterType)
return result