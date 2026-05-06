/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils
import scripts.libs.EnvironmentOverrides

def ticket = request.getSession().getValue("alf_ticket")
def username = request.getSession().getValue("username")

def currentUser = SecurityUtils.getCurrentUser()

model.envConfig = EnvironmentOverrides.getMinimalValuesForSite(applicationContext, request)
model.userEmail = currentUser?.email
model.userFirstName = currentUser?.firstName
model.userLastName = currentUser?.lastName
model.authenticationType = ''
model.cookieDomain = StringEscapeUtils.escapeXml10(request.getServerName())

model.username = username
model.ticket = ticket
