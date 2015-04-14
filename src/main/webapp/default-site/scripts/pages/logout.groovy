import scripts.libs.Cookies
model.cookieDomain = request.getServerName();
def serverProperties = applicationContext.get("studio.crafter.properties")
def alfrescoUrl = serverProperties["alfrescoUrl"] // http://127.0.0.1:8080/alfresco
def cookieDomain = serverProperties["cookieDomain"] // 127.0.0.1

Cookies.removeCookie('ccticket', cookieDomain, "/", response)
Cookies.removeCookie('crafterSite', cookieDomain, "/", response)
Cookies.removeCookie('crafterSite', cookieDomain, "/", response)
Cookies.removeCookie('ccu', cookieDomain, "/", response)
Cookies.removeCookie('alf_ticket', cookieDomain, "/", response)
Cookies.removeCookie('username', cookieDomain, "/", response)
Cookies.removeCookie('alfUsername3', cookieDomain, "/", response)
Cookies.removeCookie('alfLogin', cookieDomain, "/", response)
Cookies.removeCookie('alfUser0', cookieDomain, "/", response)
Cookies.removeCookie('alfUsername3', cookieDomain, "/share", response)
Cookies.removeCookie('alfLogin', cookieDomain, "/share", response)
Cookies.removeCookie('alfUser0', cookieDomain, "/alfresco", response)

