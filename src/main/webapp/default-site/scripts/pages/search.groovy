import scripts.libs.EnvironmentOverrides

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = request.getServerName()
model.keywordTerm = (params.s) ? params.s : ""
model.mode = (params.mode) ? params.mode : "select"
model.context = (params.context) ? params.context : "default"
