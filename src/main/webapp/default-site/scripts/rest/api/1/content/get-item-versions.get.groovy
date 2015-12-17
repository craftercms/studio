import scripts.api.ContentServices;

def result = [:]
def site = params.site
def path = params.path

def context = ContentServices.createContext(applicationContext, request)

result.item = ContentServices.getContentItem(site, path, context)
result.versions = ContentServices.getContentItemVersionHistory(site, path, context) 

return result 