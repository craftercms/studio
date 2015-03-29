import scripts.libs.Cookies
import scripts.api.SecurityServices

def result = [:]
def username = params.username;
def password = params.password;
def serverProperties = applicationContext.get("studio.crafter.properties")
def alfrescoUrl = serverProperties["alfrescoUrl"] // http://127.0.0.1:8080/alfresco
def cookieDomain = serverProperties["cookieDomain"] // 127.0.0.1

def url = alfrescoUrl + "/service/api/login?u=" + username + "&pw=" + password;
def srvresponse = "";
def invalidpw = false;

try {
    srvresponse = (url).toURL().getText();
    srvresponse = srvresponse.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
    srvresponse = srvresponse.replace("\n", "");
    srvresponse = srvresponse.replace("<ticket>", "");
    srvresponse = srvresponse.replace("</ticket>", "");

    Cookies.createCookie('ccticket', srvresponse, cookieDomain, "/", response)
    Cookies.createCookie('ccu', username, cookieDomain, "/", response)
    Cookies.createCookie('alf_ticket', srvresponse, cookieDomain, "/", response)
    Cookies.createCookie('username', username, cookieDomain, "/", response)
    Cookies.createCookie('alfUsername3', username, cookieDomain, "/", response)

    def context = SecurityServices.createContext(applicationContext, request);
    def profile = SecurityServices.getUserProfile(context, username);

 
    def user = ["name":profile.firstName,"surname":profile.lastName,"email":profile.email];

    result.type = "success";
    result.message = "Login successful";
    result.user = user;

} catch(err) {
    invalidpw = true;
    result.exception = err;
    result.response = srvresponse;
    result.type = "error";
    result.message = "Invalid user name or password";
}

return result;