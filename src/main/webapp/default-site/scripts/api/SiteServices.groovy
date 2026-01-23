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

package scripts.api

import groovy.util.logging.Log
import scripts.api.ServiceFactory

/**
 * Site Services
 */
@Log
class SiteServices {

	/**
	 * create the context object
	 * @param applicationContext - studio application's contect (spring container etc)
	 * @param request - web request if in web request context
	 */
	static createContext(applicationContext, request) {
		return ServiceFactory.createContext(applicationContext, request)
	}

	static createSiteFromBlueprint(context, blueprintName, siteId, siteName, sandboxBranch, desc, params,
				       createAsOrphan) {
		def siteServicesImpl = ServiceFactory.getSiteServices(context)
		return siteServicesImpl.createSiteFromBlueprint(blueprintName, siteId, siteName, sandboxBranch, desc, params,
			createAsOrphan)
	}

	static createSiteWithRemoteOption(context, siteId, siteName, sandboxBranch, description, blueprint, remoteName,
					  remoteUrl, remoteBranch, singleBranch, authenticationType, remoteUsername,
					  remotePassword, remoteToken, remotePrivateKey, createOption, params,
					  createAsOrphan) {
		def siteServicesImpl = ServiceFactory.getSiteServices(context)
		return siteServicesImpl.createSiteWithRemoteOption(siteId, siteName, sandboxBranch, description, blueprint,
			remoteName, remoteUrl, remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword,
			remoteToken, remotePrivateKey, createOption, params, createAsOrphan)
	}

	static getSitesPerUser(context, start, number) {
		def siteServicesImpl = ServiceFactory.getSiteServices(context)
		return siteServicesImpl.getSitesPerUser(start, number)
	}
}
