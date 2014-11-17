package scripts.api

import scripts.libs.Cookies
import scripts.api.impl.alfresco.AlfContentServices;
/** 
 * workflow services
 */
class ServiceFactory {
	
	static createContext(applicationContext, request) {
		def context = [:]

		if(request != null) {
			context.token = Cookies.getCookieValue("ccticket", request) 
		
			if(context.token == null) {
				context.token = request.getParameter("ticket");	
			}
		}

		context.applicationContext = applicationContext

		return context
	}

	/**
     * return the implementation for content services
	 */
	static getContentServices(context) {
		return new AlfContentServices(context)
	}
}