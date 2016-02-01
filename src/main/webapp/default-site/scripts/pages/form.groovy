import scripts.libs.EnvironmentOverrides

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = request.getServerName()
