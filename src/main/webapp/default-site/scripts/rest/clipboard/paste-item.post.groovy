import scripts.libs.Cookies
import scripts.libs.Clipboard

def result = [:]
def site = params.site
def destination = params.destination
def session = request.session
def requestBody = request.reader.text

def serverProperties = applicationContext.get("studio.crafter.properties")
def alfrescoUrl = serverProperties["alfrescoUrl"] // http://127.0.0.1:8080/alfresco
def ticket = Cookies.getCookieValue("ccticket", request)
def pasteServiceUrl = "/service/cstudio/wcm/clipboard/paste"
def serviceUrl = alfrescoUrl + pasteServiceUrl;

result.status = Clipboard.paste(site, session, destination, serviceUrl, ticket)
result.site = site

return result