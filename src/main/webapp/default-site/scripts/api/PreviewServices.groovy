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

package scripts.api
/**
 * preview services
 */
import scripts.api.ServiceFactory

import groovy.util.logging.Log

@Log
class PreviewServices {

	static createContext(applicationContext, request) {
		return ServiceFactory.createContext(applicationContext, request)
	}
	/**
	 * render a component
	 * @param site - the project ID
	 * @param path - the path to the component
	 */
	def renderComponentPreview(site, path) {

	}

	/**
	 * initiate a sync of content to the preview environment
	 * @param site - the project ID
	 */
	static syncAllContentToPreview(context, site) {
		def deploymentServices = ServiceFactory.getDeploymentServices(context)
		return deploymentServices.syncAllContentToPreview(site)
	}
}
