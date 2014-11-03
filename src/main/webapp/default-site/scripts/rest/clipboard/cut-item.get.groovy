import scripts.libs.Clipboard

def result = [:]
def site = params.site
def deep = true
def session = request.session
def requestBody = request.reader.text

Clipboard.cut(site, session, requestBody, deep)

result.site = site

return result