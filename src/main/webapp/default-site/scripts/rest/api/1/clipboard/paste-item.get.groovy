import scripts.libs.Cookies
import scripts.api.ClipboardServices

import javax.servlet.http.HttpSession;

def result = [:]
def site = params.site
def destination = params.parentPath
def session = (HttpSession)request.session
def requestBody = request.reader.text

def context = ClipboardServices.createContext(applicationContext, request)
result.status = ClipboardServices.paste(context, site, session, destination)
result.site = site

return result