/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

    static parseTree(parent, subtree) {
        subtree.children.each { childItem ->
            def clipboardItem = newClipboardItem(childItem.uri, false)
            parseTree(clipboardItem, childItem)
            parent.children.add(clipboardItem)
        }
    }
}
