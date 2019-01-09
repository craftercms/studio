/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
        def springBackedService = this.context.applicationContext.get("studioCmisService")
        return springBackedService.listTotal(site, cmisRepo, path)
    }

    def list(site, cmisRepo, path, start, number) {
        def springBackedService = this.context.applicationContext.get("studioCmisService")
        return springBackedService.list(site, cmisRepo, path, start, number)
    }

    def searchTotal(site, cmisRepo, searchTerm, path) {
        def springBackedService = this.context.applicationContext.get("studioCmisService")
        return springBackedService.searchTotal(site, cmisRepo, searchTerm, path)
    }

    def search(site, cmisRepo, searchTerm, path, start, number) {
        def springBackedService = this.context.applicationContext.get("studioCmisService")
        return springBackedService.search(site, cmisRepo, searchTerm, path, start, number)
    }

    def cloneContent(site, cmisRepoId, cmisPath, studioPath) {
        def springBackedService = this.context.applicationContext.get("studioCmisService")
        return springBackedService.cloneContent(site, cmisRepoId, cmisPath, studioPath)
    }
}