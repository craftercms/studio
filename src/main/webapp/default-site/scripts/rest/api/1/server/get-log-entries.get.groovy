import groovy.json.JsonSlurper
import org.craftercms.engine.service.context.SiteContext
import scripts.libs.Cookies

def result = []

def server = request.getLocalName()
def port = request.getLocalPort()
def millis = System.currentTimeMillis()
def site = Cookies.getCookieValue("crafterSite", request)

def sitesurl = "http://"+server+":"+port+"/api/1/monitoring/log?siteId="+site+"&since="+millis;
  
def response = (sitesurl).toURL().getText()
entries = new JsonSlurper().parseText( response )
result = entries;

//def entry = [:]
//entry.timestamp = new Date()
//entry.level = "ERROR"
//entry.message = "The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. The quick brown fox jumped the fence. "
//entry.exception = "" + new Exception("TEST")
//entry.stacktrace = "Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. Carlos was here. "
//result[0] = entry

return result 