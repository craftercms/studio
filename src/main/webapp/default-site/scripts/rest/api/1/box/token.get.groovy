def boxService = applicationContext["studioBoxService"]

def site = params.site
def profileId = params.profileId

def accessToken = boxService.getAccessToken(site, profileId)

return [
    accessToken : accessToken
]