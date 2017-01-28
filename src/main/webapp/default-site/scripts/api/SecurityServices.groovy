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
	 * @param token - token to be validated
	 */
	static validateTicket(context, token) {
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.validateTicket(token)
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

	static getCurrentUser(context) {  
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.getCurrentUser()
	}

	static getUserRoles(context, site, user) {  
		def securityServicesImpl = ServiceFactory.getSecurityServices(context)
		return securityServicesImpl.getUserRoles(site, user)
	}

    static logout(context) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.logout();
    }

    static createUser(context, username, password, firstname, lastname, email) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.createUser(username, password, firstname, lastname, email)
    }

    static deleteUser(context, username) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.deleteUser(username)
    }

    static updateUser(context, username, firstname, lastname, email) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.updateUser(username, firstname, lastname, email)
    }

    static getUserDetails(context, username) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.getUserDetails(username)
    }

    static enableUser(context, username, enabled) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.enableUser(username, enabled)
    }

    static getUserStatus(context, username) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.getUserStatus(username)
    }



    static getUsersPerSite(context, site) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.getUsersPerSite(site)
    }

    static createGroup(context, groupName, description, siteId) {
        def securityServicesImpl = ServiceFactory.getSecurityServices(context)
        return securityServicesImpl.createGroup(groupName, description, siteId)
    }
}
