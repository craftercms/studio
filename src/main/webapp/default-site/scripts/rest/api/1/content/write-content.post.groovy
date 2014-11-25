import scripts.api.ContentServices;

// get post body as input stream
def content = request.getInputStream()
def site = params.site
def path = params.path

def context = ContentServices.createContext(applicationContext, request)
result = ContentServices.writeContent(site, path, content, context);
return result
