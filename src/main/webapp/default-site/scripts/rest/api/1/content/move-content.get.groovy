import scripts.api.ContentServices;

def site = params.site
def fromPath = params.fromPath
def toPath = params.toPath

def context = ContentServices.createContext(applicationContext, request)
result = ContentServices.moveContent(site, fromPath, toPath, context);
return result
