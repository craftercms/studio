import groovy.json.JsonException
import scripts.api.ClipboardServices
import groovy.json.JsonSlurper

def result = [:]
try {
    def site = request.getParameter("site")
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def items = slurper.parseText(requestBody)

    def path = items.item[0].uri

    def context = ClipboardServices.createContext(applicationContext, request)

    ClipboardServices.cut(site, path, context)

    result.success = true
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result
