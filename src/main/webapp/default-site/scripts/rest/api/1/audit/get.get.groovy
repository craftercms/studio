
/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/**
 * @auhor Dejan Brkic
 */

import scripts.api.ActivityServices;

def result = [:]
def site = params.site;
def num = params.num.toInteger();
def valid = true;
def startPos = params.startpos.toInteger();


def context = ActivityServices.createContext(applicationContext, request)
def activities = ActivityServices.getAuditLog(context, site, startPos, num);

result.total = activities.size();
result.items = activities;
return result
