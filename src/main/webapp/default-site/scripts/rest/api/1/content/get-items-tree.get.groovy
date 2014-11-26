import scripts.api.ContentServices;

def result = [:]
def site = params.site
def path = params.path
def depth = params.depth.toInteger();

def context = ContentServices.createContext(applicationContext, request)

result.item = ContentServices.getContentItemTree(site, path, depth, context) 

return result 