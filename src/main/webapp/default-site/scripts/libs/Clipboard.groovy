package scripts.libs

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )

import groovy.json.JsonBuilder
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method

import groovy.json.JsonSlurper
import groovy.util.logging.Log

@Log
class Clipboard {

    public static paste(site, session, destination, serverUrl, ticket) {

        def clipboardItem = Clipboard.getItem(site, session)
        def serviceUrl = serverUrl
        serviceUrl += "?site=" + site
        serviceUrl += "&destination=" + destination
        serviceUrl += "&cut=" + clipboardItem.cut
        serviceUrl += "&alf_ticket=" + ticket
        log.info "[Clipboard] requesting paste to " + serviceUrl

        def requestItem = [:]
        requestItem.item = clipboardItem.item

        def http = new HTTPBuilder(serviceUrl);
        http.request( Method.POST, ContentType.JSON ) { req ->
            body = new JsonBuilder(requestItem).toPrettyString()
            response.success = { resp, json ->
                return resp.status
            }
            response.failure = { resp ->
                return resp.status
            }
        }
    }

    public static cut(site, session, requestJson, deep) {
        Clipboard.clip(site, session, requestJson, true, deep)
    }

    public static copy(site, session, requestJson, deep) {
        Clipboard.clip(site, session, requestJson, false, deep)
    }

    public static clip(site, session, requestJson, cut, deep) {
        log.info "[Clipboard][Copy] started clipping for " + site + " deep: " + deep + " cut: " + cut
        def slurper = new JsonSlurper()
        def parsedReq = slurper.parseText(requestJson)

        def clipboardItem = [:]
        clipboardItem.cut = cut
        clipboardItem.deep = deep
        clipboardItem.item = parsedReq.item
        session.setAttribute(Clipboard.getKey(site), clipboardItem);

        log.info "[Clipboard] copied -------------------- "
        log.info "" + parsedReq.item;

        log.info "[Clipboard] done started clipping for " + site + " deep: " + deep + " cut: " + cut
    }

    public static getItem(site, session) {
        def clipboardItem = session.getAttribute(getKey(site));
        return clipboardItem;
    }

    public static getKey(site) {
        return "clipboard_collection:" + site;
    }

}
