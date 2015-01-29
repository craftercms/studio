package scripts.api.impl.clipboard

//import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
/*
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
*/
//@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )
/**
 * content services
 */
class SpringClipboardServices {

    static CONTENT_SERVICES_BEAN = "cstudioClipboardService"

    def context = null

    /**
     * constructor
     *
     * @param context
     *          service context
     */
    def SpringClipboardServices(context) {
        this.context = context
    }

    /*
	def getAlfrescoUrl() {
		def propertiesMap = this.context.applicationContext.get(SERVER_PROPERTIES_BEAN_NAME)
		def alfrescoUrl = propertiesMap[ALFRESCO_URL_PROPERTY]
		return alfrescoUrl
	}

	def getAlfrescoTicket() {
		return this.context.token
	}*/

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
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.paste(site, clipboardItem.item, destination, clipboardItem.cut);

        /*
        def alfrescoUrl =  getAlfrescoUrl()
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
        }*/

    }

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    def createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    /**
     * paste a set of items from the clipboard
     *
     * @param site - the project ID
     * @param session - current request session
     * @param destination - the target location to paste
     * @param context - service context
     * @return response status
     */
    def paste(site, session, destination, context) {
        def clipboardItem = getItems(site, session)
        return paste(site, destination, clipboardItem);
    }

    /**
     * cut a set of items and store into session
     *
     * @param site - the project ID
     * @param session - current request session
     * @param requestJson - items in json format
     */
    def cut(site, session, requestJson) {
        clip(site, session, requestJson, true)
    }

    /**
     * copy a set of items and store into session
     *
     * @param site - the project ID
     * @param session - current request session
     * @param requestJson - items in json format
     */
    def copy(site, session, requestJson) {
        clip(site, session, requestJson, false)
    }

    /**
     * store the item given into session
     *
     * @param site - the project ID
     * @param session - request session
     * @param requestJson - items in JSON
     * @param cut - cut?
     */
    def clip(site, session, requestJson, cut) {
        def slurper = new JsonSlurper()
        def parsedReq = slurper.parseText(requestJson)

        def clipboardItem = [:]
        clipboardItem.cut = cut
        clipboardItem.item = parsedReq.item
        session.setAttribute(getKey(site), clipboardItem);

    }

    /**
     * get the items in session
     *
     * @param site - the project ID
     * @param session - request session
     * @return items cliped
     */
    def getItems(site, session) {
        def clipboardItem = session.getAttribute(getKey(site));
        return clipboardItem;
    }

    /**
     * create a key
     *
     * @param site - the project ID
     * @return a unique for the site
     */
    def static getKey(site) {
        return "clipboard_collection:" + site;
    }

}
