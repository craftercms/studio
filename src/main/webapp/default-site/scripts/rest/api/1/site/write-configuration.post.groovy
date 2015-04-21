import scripts.api.SiteServices;
 
def result = [:];
def path = params.path;
def content = request.getInputStream()

def context = SiteServices.createContext(applicationContext, request);
result.result = SiteServices.writeConfiguration(context, path, content);
  
return result