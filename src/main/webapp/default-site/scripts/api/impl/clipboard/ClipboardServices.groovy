package scripts.api.impl.clipboard

import groovy.json.JsonBuilder
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )

/**
 * content services
 */
class ClipboardServices {


	def context = null		

    /**
     * constructor
     *
     * @param context
     *          service context
     */
	def AlfClipboardServices(context) {
		this.context = context
	}

	def getAlfrescoUrl() {
		def propertiesMap = this.context.applicationContext.get(SERVER_PROPERTIES_BEAN_NAME)
		def alfrescoUrl = propertiesMap[ALFRESCO_URL_PROPERTY]
		return alfrescoUrl
	}

	def getAlfrescoTicket() {
		return this.context.token
	}

    /**
     * paste the clipboard item back to the server
     *
     * @param site
     *          site to paste the item in
     * @param destination
     *          the target location
     * @param clipboardItem
     *          contains items cut or copied
     * @return response status
     */
    def paste(site, destination, clipboardItem) {
/*        def alfrescoUrl =  getAlfrescoUrl()
        def ticket = getAlfrescoTicket()
        def alfServiceApiUrl = alfrescoUrl + "/service/cstudio/wcm/clipboard/paste?site=" + site +
                "&destination=" + destination + "&cut=" + clipboardItem.cut + "&alf_ticket=" + ticket
        def requestItem = [:]
        requestItem.item = clipboardItem.item

        def http = new HTTPBuilder(alfServiceApiUrl);
        http.request( Method.POST, ContentType.JSON ) { req ->
            body = new JsonBuilder(requestItem).toPrettyString()
            response.success = { resp, json ->
                session.removeAttribute(ClipboardServices.getKey(site));
                return resp.status
            }
            response.failure = { resp ->
                return resp.status
            }
        }
*/
    }

// move the logic from the rest calls to here for cut, copy

}	
