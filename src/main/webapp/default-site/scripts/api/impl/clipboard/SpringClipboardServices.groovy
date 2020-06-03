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

package scripts.api.impl.clipboard

/**
 * clipboard services
 * This is a session based service that tracks items clipped (COPY/CUT) by the caller and executes the 
 * proper content services (COPY TO DEST, MOVE TO DEST) on paste operation. 
 */
class SpringClipboardServices {

    static CLIPBOARD_SERVICES_BEAN = "cstudioClipboardService"

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
     * @param destination - the target location to paste
     * @param context - service context
     * @return response status
     */
    def paste(site, destination) {
        def springBackedService = this.context.applicationContext.get(CLIPBOARD_SERVICES_BEAN)
        return springBackedService.paste(site, destination, context.request.session)
    }

    /**
     * cut a set of items and store on clipboard
     *
     * @param site - the project ID
     * @param path to cut
     */
    def cut(site, path) {
        def springBackedService = this.context.applicationContext.get(CLIPBOARD_SERVICES_BEAN)
        return springBackedService.cut(site, path, context.request.session)
    }

    /**
     * copy a set of items and store on clipboard
     * @param site - the project ID
     * @param path to copy
     */
    def copy(site, paths) {
        def springBackedService = this.context.applicationContext.get(CLIPBOARD_SERVICES_BEAN)
        return springBackedService.copy(site, paths, context.request.session)
    }

    /**
     * get the items in on the clipboard
     * @param site - the project ID
     * @return items cliped
     */
    def getItems(site) {
        def springBackedService = this.context.applicationContext.get(CLIPBOARD_SERVICES_BEAN)
        return springBackedService.getItems(site, context.request.session)
    }
}