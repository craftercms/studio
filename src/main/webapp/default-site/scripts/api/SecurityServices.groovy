package scripts.api

import scripts.api.ServiceFactory

import groovy.util.logging.Log

/**
 * security services
 */
@Log
class SecurityServices {

	/**
	 * create the context object
	 * @param applicationContext - studio application's contect (spring container etc)
	 * @param request - web request if in web request context
	 */
	static createContext(applicationContext, request) {
		return ServiceFactory.createContext(applicationContext, request)
	}

	/**
	 * get a user's basic information
	 * @param site - the project ID
	 * @param userId - ID of the user to get
	 */
	def getUser(site, userId) {

	}

	/**
	 * get permissions for a user at a given path
	 * @param site - the project ID
	 * @param userId - id of user
	 * @param path - the path to the config
	 */
	def getPermissions(site, userId, path) {

	}

	/**
	 * get roles for a user within a site
	 * @param site - the project ID
	 * @param userId - id of user
	 */
	def getRoles(site, userId) {

	}

	/**
	 * validate a security token
	 * @param site - the project ID
	 * @param token - token to be validated
	 */
	def validateToken(site, token) {

	}

	def getUserPermissions(context, site, path, user, groups) {
		def securityServicesImpl = ServiceFactory.getSecurityServices(context);
		return securityServicesImpl.getUserPermissions(site, path, user, groups);
	}
}