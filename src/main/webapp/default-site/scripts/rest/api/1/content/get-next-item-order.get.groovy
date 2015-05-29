import scripts.api.PageNavigationOrderServices;

def result = [:]
def site = params.site
def path = params.path

def context = PageNavigationOrderServices.createContext(applicationContext, request)
result.nextValue = PageNavigationOrderServices.getNextItemOrder(context, site, path);

return result 