import scripts.api.DeploymentServices;

def result = [:]
def site = params.site
def days = params.days.toInteger()
def num = params.num.toInteger()
def filterType = params.filterType

def context = DeploymentServices.createContext(applicationContext, request)

def deploymentHistory = DeploymentServices.getDeploymentHistory(site, days, num, "eventDate", false, filterType, context)
def total = 0;
for (task in deploymentHistory) {
    total += task.numOfChildren;
}
result.total = total;
result.documents = deploymentHistory;

return result