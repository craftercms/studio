package scripts.api

import scripts.api.alfresco.AlfContentServices;
/** 
 * workflow services
 */
class ServiceFactory {

	/**
     * return the implementation for content services
	 */
	static getContentServices() {
		return new AlfContentServices();
	}
}