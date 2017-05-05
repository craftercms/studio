import scripts.api.SiteServices;
 
def result = [:];
def path = request.getParameter("path")
def site = request.getParameter("site")
def content = request.getInputStream()

def context = SiteServices.createContext(applicationContext, request);
if (site != null && site != "") {
    result.result = SiteServices.writeConfiguration(context, site, path, content);
} else {
    result.result = SiteServices.writeConfiguration(context, path, content);
}
return result
