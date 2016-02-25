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
		def deploymentServicesImpl = ServiceFactory.getDeploymentServices(context);
		return deploymentServicesImpl.syncAllContentToPreview(site);
	}
}
