import scripts.libs.EnvironmentOverrides
import scripts.libs.HTMLCompareTools
import scripts.api.ContentServices

def result = [:]
def site = params.site
def path = params.path
def version = params.version

def context = ContentServices.createContext(applicationContext, request)
String original = "UNSET"
String revised = "UNSET"

def compareType = ""
def originalVersion = ""
def revisedVersion = ""

if([Collection, Object[]].any { it.isAssignableFrom(version.getClass()) } == false) {
	// COMPARE VERSION to HEAD
	compareType = "COMPARE_TO_HEAD"
	originalVersion = "HEAD"
	revisedVersion = version

	original = ContentServices.getContent(site, path, false, context)
	revised = ContentServices.getContentVersionAtPath(site, path, version, context)
}
else {
	if(version[0] != version[1]) {

		// COMPARE VERSION TO VERSION
		compareType = "VERSION_VS_VERSION"
		originalVersion = version[0]
		revisedVersion = version[1]

		original = ContentServices.getContentVersionAtPath(site, path, version[0], context)
		revised = ContentServices.getContentVersionAtPath(site, path, version[1], context)
	}
	else{

		// LOOK AT A VERSION
		version = version[0]

		compareType = "VERSION_CONTENT"
		originalVersion = version
		revisedVersion = version

		original = ContentServices.getContentVersionAtPath(site, path, version[0], context)
		revised = original
	}
}

if(revised == null || original == null) {
	logger.error("${site} : ${path} : ${compareType}" )
	logger.error("${originalVersion} =================================")
	logger.error(""+original)
	logger.error("${revisedVersion} =================================")
	logger.error(""+revised)
	throw new Exception("Cannot diff because one of the expected content versions is null")
}

model.version = version

model.xsl = HTMLCompareTools.CONTENT_XML_TO_HTML_XSL

model.variantA = HTMLCompareTools.xmlAsStringToHtml(revised)
model.variantB = HTMLCompareTools.xmlAsStringToHtml(original)

model.diff = HTMLCompareTools.diff(model.variantA, model.variantB)
model.dir = path

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = request.getServerName()
