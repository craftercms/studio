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
 */


import org.craftercms.studio.api.v1.service.activity.ActivityService
import scripts.api.ActivityServices;

def result = [:]
def site = params.site;
def user = params.user;
def path = params.path;
def activity = params.activity;
def contentTypeClass = params.contentTypeClass
def activityType = ActivityService.ActivityType.valueOf(activity.toUpperCase());
def extraInfo = [:]
extraInfo.contentType = contentTypeClass;

def context = ActivityServices.createContext(applicationContext, request);
result.result = ActivityServices.postActivity(context, site, user, path, activityType, extraInfo);

return result;
