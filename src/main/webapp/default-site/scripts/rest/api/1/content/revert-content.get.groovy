import scripts.api.ContentServices;

def result = [:]
def site = params.site
def path = params.path
def version = params.version

def context = ContentServices.createContext(applicationContext, request)

result = ContentServices.revertContentItem(site, path, version, context) 

return result 