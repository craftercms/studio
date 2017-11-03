import org.craftercms.commons.validation.validators.impl.NoTagsValidator
import org.craftercms.engine.exception.HttpStatusCodeException
import org.springframework.http.HttpStatus

import scripts.libs.EnvironmentOverrides
import scripts.libs.utils.ValidationUtils

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = request.getServerName()
model.keywordTerm = (request.getParameter("s")) ? request.getParameter("s") : ""
model.mode = (request.getParameter("mode")) ? request.getParameter("mode") : "select"
model.context = (request.getParameter("context")) ? request.getParameter("context") : "default"

try {
    ValidationUtils.validateInput(model.keywordTerm, new NoTagsValidator("keywordTerm"))
} catch (e) {
    throw new HttpStatusCodeException(HttpStatus.BAD_REQUEST, "Invalid keywordTerm input", e)
}
