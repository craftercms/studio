package scripts.api

import groovy.json.JsonSlurper
import scripts.api.ServiceFactory

import groovy.util.logging.Log

/**
 * Clipboard sevices are scoped to the specific user / session
 * (The only stateful services)
 */
@Log
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
     * @param context - service context
     * @return response status
     */
    public static paste(site, session, destination, context) {
        def clipboardItem = ClipboardServices.getItem(site, session)
        def clipboardServicesImpl = ServiceFactory.getClipboardServices(context)
        return clipboardServicesImpl.paste(site, destination, clipboardItem);
	}

    /**
     * cut a set of items and store into session
     *
     * @param site - the project ID
     * @param session - current request session
     * @param requestJson - items in json format
     * @param deep - cut recursively?
     */
    static cut(site, session, requestJson, deep) {
        ClipboardServices.clip(site, session, requestJson, true, deep)
    }

    /**
     * copy a set of items and store into session
     *
     * @param site - the project ID
     * @param session - current request session
     * @param requestJson - items in json format
     * @param deep - copy recursively?
     */
    static copy(site, session, requestJson, deep) {
        ClipboardServices.clip(site, session, requestJson, false, deep)
    }

    /**
     * store the item given into session
     *
     * @param site - the project ID
     * @param session - request session
     * @param requestJson - items in JSON
     * @param cut - cut?
     * @param deep - cut or copy recursively?
     */
    private static clip(site, session, requestJson, cut, deep) {
        log.info "[Clipboard][Copy] started clipping for " + site + " deep: " + deep + " cut: " + cut
        def slurper = new JsonSlurper()
        def parsedReq = slurper.parseText(requestJson)

        def clipboardItem = [:]
        clipboardItem.cut = cut
        clipboardItem.deep = deep
        clipboardItem.item = parsedReq.item
        session.setAttribute(ClipboardServices.getKey(site), clipboardItem);

        log.info "[Clipboard] copied -------------------- "
        log.info "" + parsedReq.item;

        log.info "[Clipboard] done started clipping for " + site + " deep: " + deep + " cut: " + cut
    }

    /**
     * get the items in session
     *
     * @param site - the project ID
     * @param session - request session
     * @return items cliped
     */
    static getItem(site, session) {
        def clipboardItem = session.getAttribute(getKey(site));
        return clipboardItem;
    }

    /**
     * create a key
     *
     * @param site - the project ID
     * @return a unique for the site
     */
    private static getKey(site) {
        return "clipboard_collection:" + site;
    }
}