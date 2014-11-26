import scripts.libs.Cookies
import scripts.api.ClipboardServices;

def result = [:]
def site = params.site
def destination = params.parentPath
def session = request.session
def requestBody = request.reader.text

def context = ClipboardServices.createContext(applicationContext, request)
result.status = ClipboardServices.paste(site, session, destination, context)
result.site = site

return result