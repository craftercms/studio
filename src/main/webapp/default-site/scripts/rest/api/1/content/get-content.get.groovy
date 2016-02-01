import scripts.api.ContentServices;

def result = [:];
def site = params.site;
def path = params.path
def edit = (params.edit == "true")?true:false;

def context = ContentServices.createContext(applicationContext, request);

result.content = ContentServices.getContent(site, path, edit, context);

return result 
