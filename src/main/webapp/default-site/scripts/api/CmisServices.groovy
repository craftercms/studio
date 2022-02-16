/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

/**
 * cmis services
 */
@Log
class CmisServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    static listTotal(context, site, cmisRepo, path) {
        def cmisServiceImpl = ServiceFactory.getCmisServices(context)
        return cmisServiceImpl.listTotal(site, cmisRepo, path)
    }

    static list(context, site, cmisRepo, path, start, number) {
        def cmisServiceImpl = ServiceFactory.getCmisServices(context)
        return cmisServiceImpl.list(site, cmisRepo, path, start, number)
    }

    static searchTotal(context, site, cmisRepo, searchTerm, path) {
        def cmisServiceImpl = ServiceFactory.getCmisServices(context)
        return cmisServiceImpl.searchTotal(site, cmisRepo, searchTerm, path)
    }

    static search(context, site, cmisRepo, searchTerm, path, start, number) {
        def cmisServiceImpl = ServiceFactory.getCmisServices(context)
        return cmisServiceImpl.search(site, cmisRepo, searchTerm, path, start, number)
    }

    static cloneContent(context, siteId, cmisRepoId, cmisPath, studioPath) {
        def cmisServiceImpl = ServiceFactory.getCmisServices(context)
        return cmisServiceImpl.cloneContent(siteId, cmisRepoId, cmisPath, studioPath)
    }

    static uploadContent(context, siteId, cmisRepoId, cmisPath, filename, content) {
        def cmisServiceImpl = ServiceFactory.getCmisServices(context)
        return cmisServiceImpl.uploadContent(siteId, cmisRepoId, cmisPath, filename, content)
    }
}
