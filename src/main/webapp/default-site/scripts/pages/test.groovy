
import groovyx.net.http.HTTPBuilder

import scripts.libs.EnvironmentOverrides

def result = [:]
model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)  
model.cookieDomain = request.getServerName()     

model.tests = doTests()

def doTests() {
	def tests = []

	tests.add(runTest("Create Site", this.&testCreateSite))

	return tests	
}

def testCreateSite() {
	def result = [:]
	result.testName = "Create Site"
	
	uri = "/api/1/services/api/1/site/create-site.json"
	def params = [:]
	params.siteId = "123abc"
	params.siteName="123abc"
	params.blueprintName = "empty"

	post(uri, params) 
	return true

	}

def runTest(testName, test) {
	def result = [:]
	result.name = testName
	result.status = false

	def startTime = System.currentTimeMillis()

	try {
		result.status = test()
	}
	catch(err) {
		result.error = err
	}

	def stopTime = System.currentTimeMillis()

	double duration = ((stopTime - startTime)/1000)
	result.duration = duration

	return result
}

def post(uri, params) {
	def url = "http://localhost:8080/studio"+uri  +"?siteId=123C&siteName=123Ce&blueprintName=empty"
	def content = "{}" 
	HTTPBuilder http = new HTTPBuilder(url)

	http.request( groovyx.net.http.Method.POST, groovyx.net.http.ContentType.TEXT) {
        
content = "{}"
    	def body = "{ "
		for ( e in params ) {
    		print "key = ${e.key}, value = ${e.value}"
		}		

        body+="}"
	}
}