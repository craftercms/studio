import org.craftercms.commons.validation.validators.impl.NoTagsValidator
import org.craftercms.engine.exception.HttpStatusCodeException
import org.springframework.http.HttpStatus

import scripts.libs.EnvironmentOverrides
import scripts.libs.utils.ValidationUtils

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = request.getServerName()
model.keywordTerm = (params.s) ? params.s : ""
model.mode = (params.mode) ? params.mode : "select"
model.context = (params.context) ? params.context : "default"

try {
    ValidationUtils.validateInput(model.keywordTerm, new NoTagsValidator("keywordTerm"))
} catch (e) {
    throw new HttpStatusCodeException(HttpStatus.BAD_REQUEST, "Invalid keywordTerm input", e)
}
