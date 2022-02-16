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
import scripts.libs.EnvironmentOverrides
import scripts.api.SecurityServices

def result = [:]
def ticket = request.getSession().getValue("alf_ticket");
def username = request.getSession().getValue("username");

def context = SecurityServices.createContext(applicationContext, request, response);
def profile = SecurityServices.getUserProfile(context, username);

//model.envConfig = EnvironmentOverrides.getValuesForSite(applicationContext, request)
model.userEmail = profile.email
model.userFirstName = profile.firstName
model.userLastName =  profile.lastName
model.authenticationType =  profile.authentication_type
model.cookieDomain = StringEscapeUtils.escapeXml10(request.getServerName())

model.username = username
model.ticket = ticket
