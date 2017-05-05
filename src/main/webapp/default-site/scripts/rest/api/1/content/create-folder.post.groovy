import scripts.api.ContentServices;

def site = request.getParameter("site")
def path = request.getParameter("path")
def name = request.getParameter("name")

def context = ContentServices.createContext(applicationContext, request)
result = ContentServices.createFolder(site, path, name, context);
return result
