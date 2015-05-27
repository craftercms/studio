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
	 * get permissions for a user at a given path
	 * @param site - the project ID
	 * @param userId - id of user
	 * @param path - the path to the config
	 */
	def getPermissions(site, userId, path) {

	}

	/**
	 * validate a security token
	 * @param site - the project ID
	 * @param token - token to be validated
	 */
	static validateToken(context, site, token) {
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.validateToken(site, token)
	}

	/** 
	 * authenticate a user. returns ticket
	 * @param username
	 * @param password
	 */
	static authenticate(context, username, password) {
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.authenticate(username, password)
	}

	/** 
	 * get user profile
	 * @param username
	 */
	static getUserProfile(context, username) {
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.getUserProfile(username)
	}

	static getUserPermissions(context, site, path, user, groups) {
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.getUserPermissions(site, path, user, groups)
	}

	static getUserRoles(context, site, user) {  
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.getUserRoles(site, user)
	}
}