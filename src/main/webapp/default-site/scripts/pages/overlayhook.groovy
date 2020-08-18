/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import scripts.libs.EnvironmentOverrides

import static java.nio.charset.StandardCharsets.UTF_8

import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request, response)
model.cookieDomain = StringEscapeUtils.escapeXml10(request.getServerName())
response.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
response.setCharacterEncoding(UTF_8.name())