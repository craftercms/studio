/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import org.apache.commons.text.StringEscapeUtils
import org.craftercms.commons.validation.validators.impl.NoTagsValidator
import org.craftercms.engine.exception.HttpStatusCodeException
import org.springframework.http.HttpStatus

import scripts.libs.EnvironmentOverrides
import scripts.libs.utils.ValidationUtils

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = StringEscapeUtils.escapeXml10(request.getServerName())
model.keywordTerm = (request.getParameter("s")) ? request.getParameter("s") : ""
model.mode = (request.getParameter("mode")) ? request.getParameter("mode") : "select"
model.context = (request.getParameter("context")) ? request.getParameter("context") : "default"

try {
    ValidationUtils.validateInput(model.keywordTerm, new NoTagsValidator("keywordTerm"))
} catch (e) {
    throw new HttpStatusCodeException(HttpStatus.BAD_REQUEST, "Invalid keywordTerm input", e)
}
