import scripts.libs.Clipboard

def result = [:]
def site = params.site
def cut = false
def deep = true
def requestBody = request.reader.text

Clipboard.copy(site, requestBody, cut, deep)

result.site = site

return result