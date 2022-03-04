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

import scripts.libs.Cookies
import scripts.api.impl.content.SpringContentServices
import scripts.api.impl.content.SpringContentTypeServices
import scripts.api.impl.content.SpringPageNavigationOrderServices
import scripts.api.impl.deployment.SpringDeploymentServices
import scripts.api.impl.activity.SpringActivityServices
import scripts.api.impl.workflow.SpringWorkflowServices
import scripts.api.impl.security.SpringSecurityServices
import scripts.api.impl.site.SpringSiteServices
import scripts.api.impl.dependency.SpringDependencyServices
import scripts.api.impl.cmis.SpringCmisServices
import scripts.api.impl.user.SpringUserServices

/**
 * Class is a factory used by the API wrappers to find their implementation
 */
class ServiceFactory {
	
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
	 * return the implementation for deps services
	 *
	 * @param context site context
	 * @return Deps Services
	 */
    static getDependencyServices(context) {
        return new SpringDependencyServices(context)
    }

	/**
	 * return the implementation for nav services
	 *
	 * @param context site context
	 * @return Nav Services
	 */
	static getPageNavigationOrderServices(context) {
		return new SpringPageNavigationOrderServices(context)
	}

    /**
     * return the implementation for cmis services
     *
     * @param context site context
     * @return Cmis Services
     */
    static getCmisServices(context) {
        return new SpringCmisServices(context)
    }
	/**
	 * return the implementation for user services
	 *
	 * @param context site context
	 * @return User Services
	 */
	static getUserServices(context) {
		return new SpringUserServices(context)
	}

}
