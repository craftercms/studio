import scripts.api.ContentServices;

def site = params.site
def path = params.path
def name = params.name

def context = ContentServices.createContext(applicationContext, request)
result = ContentServices.createFolder(site, path, name, context);
return result
