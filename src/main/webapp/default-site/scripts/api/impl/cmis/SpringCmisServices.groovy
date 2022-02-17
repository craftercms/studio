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

package scripts.api.impl.cmis

class SpringCmisServices {

    static CMIS_SERVICES_BEAN = "studioCmisService"

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringCmisServices(context) {
        this.context = context
    }

    def listTotal(site, cmisRepo, path) {
        def springBackedService = this.context.applicationContext.get(CMIS_SERVICES_BEAN)
        return springBackedService.listTotal(site, cmisRepo, path)
    }

    def list(site, cmisRepo, path, start, number) {
        def springBackedService = this.context.applicationContext.get(CMIS_SERVICES_BEAN)
        return springBackedService.list(site, cmisRepo, path, start, number)
    }

    def searchTotal(site, cmisRepo, searchTerm, path) {
        def springBackedService = this.context.applicationContext.get(CMIS_SERVICES_BEAN)
        return springBackedService.searchTotal(site, cmisRepo, searchTerm, path)
    }

    def search(site, cmisRepo, searchTerm, path, start, number) {
        def springBackedService = this.context.applicationContext.get(CMIS_SERVICES_BEAN)
        return springBackedService.search(site, cmisRepo, searchTerm, path, start, number)
    }

    def cloneContent(site, cmisRepoId, cmisPath, studioPath) {
        def springBackedService = this.context.applicationContext.get(CMIS_SERVICES_BEAN)
        return springBackedService.cloneContent(site, cmisRepoId, cmisPath, studioPath)
    }

    def uploadContent(site, cmisRepoId, cmisPath, filename, content) {
        def springBackedService = this.context.applicationContext.get(CMIS_SERVICES_BEAN)
        return springBackedService.uploadContent(site, cmisRepoId, cmisPath, filename, content)
    }
}