import scripts.api.ContentServices;

def result = [:]
def site = params.site
def path = params.path

def context = ContentServices.createContext(applicationContext, request)

def item = ContentServices.getContentItem(site, path, context);
result.item = item;

return result 
