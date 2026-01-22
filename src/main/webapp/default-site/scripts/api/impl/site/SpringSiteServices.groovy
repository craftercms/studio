/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
package scripts.api.impl.site

class SpringSiteServices {

	static SITE_SERVICES_BEAN = "cstudioSiteServiceSimple"

	def context = null

	/**
	 * constructor
	 *
	 * @param context - service context
	 */
	def SpringSiteServices(context) {
		this.context = context
	}

	def createSiteFromBlueprint(blueprintName, siteId, siteName, sandboxBranch, desc, params, createAsOrphan) {
		def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
		return springBackedService.createSiteFromBlueprint(blueprintName, siteId, siteName, sandboxBranch, desc,
			params, createAsOrphan)
	}

	def createSiteWithRemoteOption(siteId, siteName, sandboxBranch, description, blueprint, remoteName, remoteUrl,
				       remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword,
				       remoteToken, remotePrivateKey, createOption, params, createAsOrphan) {
		def springBackedService = this.context.applicationContext.get(SITE_SERVICES_BEAN)
		return springBackedService.createSiteWithRemoteOption(siteId, siteName, sandboxBranch, description, blueprint,
			remoteName, remoteUrl, remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword,
			remoteToken, remotePrivateKey, createOption, params, createAsOrphan)
	}

}
