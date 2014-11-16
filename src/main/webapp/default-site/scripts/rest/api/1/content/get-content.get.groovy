import scripts.api.ContentServices;

def result = [:]
def site = params.site
def path = params.path

result.content = ContentServices.getContent(site, path)

return result