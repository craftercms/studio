import scripts.api.ClipboardServices;

def result = [:]
def site = params.site
def deep = true
def session = request.session
def requestBody = request.reader.text

def context = ClipboardServices.createContext(applicationContext, request)
ClipboardServices.cut(site, session, requestBody, context)

result.success = true

return result