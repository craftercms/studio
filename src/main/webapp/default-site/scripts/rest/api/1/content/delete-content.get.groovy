import scripts.api.ContentServices;

def site = params.site
def path = params.path

def context = ContentServices.createContext(applicationContext, request)
result = ContentServices.deleteContent(site, path, context);
return result
