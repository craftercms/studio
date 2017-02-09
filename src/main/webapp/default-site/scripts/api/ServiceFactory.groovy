package scripts.api

import scripts.libs.Cookies
import scripts.api.impl.content.SpringContentServices
import scripts.api.impl.content.SpringContentTypeServices
import scripts.api.impl.content.SpringPageNavigationOrderServices
import scripts.api.impl.clipboard.SpringClipboardServices
import scripts.api.impl.deployment.SpringDeploymentServices
import scripts.api.impl.activity.SpringActivityServices
import scripts.api.impl.workflow.SpringWorkflowServices
import scripts.api.impl.security.SpringSecurityServices
import scripts.api.impl.site.SpringSiteServices
import scripts.api.impl.dependency.SpringDependencyServices
import scripts.api.impl.objectstate.SpringObjectStateServices

class ServiceFactory {
	
	/**
	 * Create a service context
	 */
	static createContext(applicationContext, request) {
		def context = [:]
		context.token = ""
		context.applicationContext = applicationContext
		context.request = request

		if(request != null) {
			context.token = Cookies.getCookieValue("ccticket", request) 
		
			if(context.token == null) {
				context.token = request.getParameter("ticket")
			}
		}

		return context
	}

	/**
     * return the implementation for content services
	 */
	static getContentServices(context) {
		return new SpringContentServices(context)
	}

	/**
	 * return the implementation for content type services
	 */
	static getContentTypeServices(context) {
		return new SpringContentTypeServices(context)
	}

    /**
     * return the implementation for clipboard services
     *
     * @param context site context
     * @return ClipboardServices
     */
    static getClipboardServices(context) {
        return new SpringClipboardServices(context)
    }

	/**
	 * return the implementation for deployment services
	 *
	 * @param context site context
	 * @return DeploymentServices
	 */
	static getDeploymentServices(context) {
		return new SpringDeploymentServices(context)
	}

	/**
	 * return the implementation for activity services
	 *
	 * @param context site context
	 * @return ActivityServices
	 */
	static getActivityServices(context) {
		return new SpringActivityServices(context)
	}

	/**
	 * return the implementation for workflow services
	 *
	 * @param context site context
	 * @return WorkflowServices
	 */
	static getWorkflowServices(context) {
		return new SpringWorkflowServices(context)
	}

	/**
	 * return the implementation for security services
	 *
	 * @param context site context
	 * @return SecurityServices
	 */
	static getSecurityServices(context) {
		return new SpringSecurityServices(context)
	}

	/**
	 * return the implementation for site services
	 *
	 * @param context site context
	 * @return SiteServices
	 */
	static getSiteServices(context) {
		return new SpringSiteServices(context)
	}

	/**
	 * return the implementation for dependency services
	 */
    static getDependencyServices(context) {
        return new SpringDependencyServices(context)
    }

	/**
	 * return the implementation for object state services
	 */
    static getObjectStateServices(context) {
        return new SpringObjectStateServices(context)
    }

	/**
	 * return the implementation for page navigatiun services
	 */
	static getPageNavigationOrderServices(context) {
		return new SpringPageNavigationOrderServices(context)
	}
}