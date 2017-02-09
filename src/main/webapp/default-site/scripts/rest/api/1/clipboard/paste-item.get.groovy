import org.craftercms.studio.api.v1.exception.ServiceException
import scripts.api.ClipboardServices

def result = [:]
def site = params.site
def destination = params.parentPath

def context = ClipboardServices.createContext(applicationContext, request)

try {
    result.status = ClipboardServices.paste(site, destination, context)
    result.site = site
}  
catch (ServiceException error) {
    result.site = site
    result.error = error.message
    response.status = 500
}  

return result