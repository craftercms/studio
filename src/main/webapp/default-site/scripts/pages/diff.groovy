import scripts.libs.EnvironmentOverrides
import scripts.libs.HTMLCompareTools
import scripts.api.ContentServices
import org.apache.commons.lang.StringEscapeUtils


def result = [:]
def site = request.getParameter("site")
def path = request.getParameter("path")
def version = request.getParameter("version")
def versionTO = request.getParameter("versionTO")
def escaped = request.getParameter("escaped")

def context = ContentServices.createContext(applicationContext, request)
String original = "UNSET"
String revised = "UNSET"

model.version = version
model.versionTO = versionTO

model.xsl = HTMLCompareTools.CONTENT_XML_TO_HTML_XSL

if([Collection, Object[]].any { it.isAssignableFrom(version.getClass()) } == false && !versionTO) {
	original = ContentServices.getContent(site, path, false, context)
	revised = ContentServices.getContentVersionAtPath(site, path, version, context)
}
else {
	original = ContentServices.getContentVersionAtPath(site, path, version, context)
	revised = ContentServices.getContentVersionAtPath(site, path, versionTO, context)
}

if(!escaped){
	model.variantA = HTMLCompareTools.xmlAsStringToHtml(revised)
	model.variantB = HTMLCompareTools.xmlAsStringToHtml(original)
}else{
	model.revisedEscaped = HTMLCompareTools.xmlEscapedFormatted(revised)
	model.variantA = '<?xml version="1.0" encoding="UTF-8"?><html><body>' + model.revisedEscaped + '</body></html>'
	model.originalEscaped = HTMLCompareTools.xmlEscapedFormatted(original)
	model.variantB = '<?xml version="1.0" encoding="UTF-8"?><html><body>' + model.originalEscaped + '</body></html>'
}

model.diff = HTMLCompareTools.diff(model.variantA, model.variantB)

model.dir = path

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)  
model.cookieDomain = request.getServerName()     



