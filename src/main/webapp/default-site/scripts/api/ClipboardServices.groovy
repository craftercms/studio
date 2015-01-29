package scripts.api

import scripts.api.ServiceFactory
import groovy.util.logging.Log

/**
 * Clipboard sevices are scoped to the specific user / session
 * (The only stateful services)
 */
class ClipboardServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    /**
     * paste a set of items from the clipboard
     *
     * @param site - the project ID
     * @param session - current request session
     * @param destination - the target location to paste
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     * @return response status
     */
    static paste(context, site, session, destination) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        return clipboardServicesImpl.paste(site, session, destination, context);
	}

    /**
     * cut a set of items and store into session
     *
     * @param site - the project ID
     * @param session - current request session
     * @param requestJson - items in json format
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static cut(site, session, requestJson, context) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        clipboardServicesImpl.cut(site, session, requestJson)
    }

    /**
     * copy a set of items and store into session
     *
     * @param site - the project ID
     * @param session - current request session
     * @param requestJson - items in json format
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static copy(context, site, session, requestJson) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        clipboardServicesImpl.copy(site, session, requestJson)
    }

    /**
     * get the items in session
     *
     * @param site - the project ID
     * @param session - request session
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     * @return items clipped
     */
    static getItems(site, session, context) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        return clipboardServicesImpl.getItems(site, session);
    }

}