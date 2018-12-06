def boxService = applicationContext["studioBoxService"]

def site = params.site
def profileId = params.profileId
def fileId = params.fileId
def filename = params.filename

def result = [:]
def invalidParams = false
def paramsList = []

if(!site) {
    invalidParams = true
    paramsList += "site"
}

if(!profileId) {
    invalidParams = true
    paramsList += "profileId"
}

if(!fileId) {
    invalidParams = true
    paramsList += "fileId"
}

if(!filename) {
    invalidParams = true
    paramsList += "filename"
}

if(invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    result.url = boxService.getUrl(site, profileId, fileId, filename)
}

return result