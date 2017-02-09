import scripts.api.ClipboardServices
import groovy.json.JsonSlurper

def result = [:]
def site = params.site
def requestBody = request.reader.text

def slurper = new JsonSlurper()
def items = slurper.parseText(requestBody)

def path = items.item[0].uri

def context = ClipboardServices.createContext(applicationContext, request)

ClipboardServices.cut(site, path, context)

result.success = true

return result