import scripts.libs.EnvironmentOverrides
import scripts.libs.HTMLCompareTools
import scripts.api.ContentServices

def result = [:]
def site = params.site
def path = params.path
def version = params.version

def context = ContentServices.createContext(applicationContext, request)
def original = "UNSET"
def revised = "UNSET"

if([Collection, Object[]].any { it.isAssignableFrom(version.getClass()) } == false) {
	original = ContentServices.getContent(site, path, false, context)
	revised = ContentServices.getContentVersionAtPath(site, path, version, context)
}
else {
	original = ContentServices.getContentVersionAtPath(site, path, version[0], context)
	revised = ContentServices.getContentVersionAtPath(site, path, version[1], context)
}
model.xsl = HTMLCompareTools.CONTENT_XML_TO_HTML_XSL

model.variantA = HTMLCompareTools.xmlToHtml(revised)
model.variantB = HTMLCompareTools.xmlToHtml(original)

model.diff = HTMLCompareTools.diff(model.variantA, model.variantB)

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)  
model.cookieDomain = request.getServerName()     
