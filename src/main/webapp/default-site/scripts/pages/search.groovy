import scripts.libs.EnvironmentOverrides

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = request.getServerName()
model.keywordTerm = (request.getParameter("s")) ? request.getParameter("s") : ""
model.mode = (request.getParameter("mode")) ? request.getParameter("mode") : "select"
model.context = (request.getParameter("context")) ? request.getParameter("context") : "default"
