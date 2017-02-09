package scripts.api

import scripts.api.ServiceFactory
import groovy.util.logging.Log
import org.craftercms.studio.api.v1.service.clipboard.ClipboardService
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
     * @param destination - the target location to paste
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     * @return response status
     */
    static paste(site, destination, context) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        return clipboardServicesImpl.paste(site, destination);
    }

    /**
     * cut a set of items and store into clipboard
     *
     * @param site - the project ID
     * @param path - item to cut
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static cut(site, path, context) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        clipboardServicesImpl.cut(site, path)
    }

    /**
     * copy a set of items and store into clipboard
     *
     * @param site - the project ID
     * @param path - items in json format
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static copy(site, paths, context) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        clipboardServicesImpl.copy(site, paths)
    }

    /**
     * get the items in session
     *
     * @param site - the project ID
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     * @return items clipped
     */
    static getItems(site, context) {
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        return clipboardServicesImpl.getItems(site);
    }

    static newClipboardItem(path, cut) {
        return new ClipboardService.ClipboardItem(path, cut)
    }
}