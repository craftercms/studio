import org.craftercms.studio.api.v1.exception.ServiceException
import scripts.libs.Cookies
import scripts.api.ClipboardServices

import javax.servlet.http.HttpSession;

def result = [:]
def site = params.site
def destination = params.parentPath
def session = (HttpSession)request.session
def requestBody = request.reader.text

def context = ClipboardServices.createContext(applicationContext, request)
try {
    result.status = ClipboardServices.paste(context, site, session, destination)
    result.site = site
} catch (ServiceException error) {
    result.site = site
    result.error = error.message
    response.status = 500;
}

return result