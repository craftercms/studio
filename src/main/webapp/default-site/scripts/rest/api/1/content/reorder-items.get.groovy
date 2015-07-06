import scripts.api.ContentServices;

def result = [:]
def site = params.site
def path = params.path
def after = params.after
def before = params.before

def context = ContentServices.createContext(applicationContext, request)
result.orderValue = ContentServices.reorderItems(context, site, path, before, after)

return result 