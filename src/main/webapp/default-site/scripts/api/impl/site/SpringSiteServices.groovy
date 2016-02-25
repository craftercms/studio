
/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
package scripts.api.impl.site;

class SpringSiteServices {

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringSiteServices(context) {
        this.context = context
    }

    def writeConfiguration(path, content){
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.writeConfiguration(path, content);       
    }

    def writeConfiguration(site, path, content){
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.writeConfiguration(site, path, content);
    }

    def getConfiguraiton(path) {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.getConfiguration(site, path);
    }

    def getConfiguration(site, path, applyEnvironment) {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.getConfiguration(site, path, applyEnvironment);
    }

    def getAllAvailableSites() {
         def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.getAllAvailableSites()
    }

    def getUserSites(user) {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.getUserSites(user);
    }

    def createSiteFromBlueprint(blueprintName, siteName, siteId, desc) {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.createSiteFromBlueprint(blueprintName, siteName, siteId, desc)
    }

    def deleteSite(siteId) {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.deleteSite(siteId)
    }

    def getAvailableBlueprints() {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.getAvailableBlueprints()
    }

    def reloadSiteConfiguration(site) {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.reloadSiteConfiguration(site)
    }

    def importSite(config) {
        def springBackedService = this.context.applicationContext.get("cstudioSiteServiceSimple")
        return springBackedService.importSite(config)
    }
}
