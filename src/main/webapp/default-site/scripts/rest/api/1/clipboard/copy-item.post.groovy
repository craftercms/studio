import scripts.api.ClipboardServices
import groovy.json.JsonSlurper



def result = [:]
def site = request.getParameter("site")

def requestBody = request.reader.text
def context = ClipboardServices.createContext(applicationContext, request)
def slurper = new JsonSlurper()
def tree = slurper.parseText(requestBody)
def paths = []

// parse the inbound request and compose an array of paths to put on the clipboard
def rootItem = ClipboardServices.newClipboardItem(tree.item[0].uri, false)

ClipboardServices.parseTree(rootItem, tree.item[0])

ClipboardServices.copy(site, rootItem, context)

result.success = true

return result



