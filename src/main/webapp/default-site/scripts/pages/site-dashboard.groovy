import scripts.libs.EnvironmentOverrides

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request)  
model.cookieDomain = request.getServerName()    