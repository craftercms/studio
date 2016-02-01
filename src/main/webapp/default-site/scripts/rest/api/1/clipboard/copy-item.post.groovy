import scripts.api.ClipboardServices;

def result = [:]
def site = params.site
def session = request.session
def requestBody = request.reader.text

def context = ClipboardServices.createContext(applicationContext, request)
ClipboardServices.copy(context, site, session, requestBody)

result.success = true

return result
