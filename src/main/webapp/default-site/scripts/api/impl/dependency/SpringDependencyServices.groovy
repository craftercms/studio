/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
package scripts.api.impl.dependency

/**
 * content type services
 */
class SpringDependencyServices {

    static DEPENDENCY_SERVICES_BEAN = "studioDependencyService"
    static CONTENT_SERVICES_BEAN = "cstudioContentService"

    def context = null

    def SpringDependencyServices(context) {
        this.context = context
    }

    @Deprecated
    def getDependantItems(site, path) {
        def springBackendService = this.context.applicationContext.get(DEPENDENCY_SERVICES_BEAN);
        def springBackendContentService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
        def dependants = []
        def dependantPaths = springBackendService.getItemsDependingOn(site, path, 1)
        dependantPaths.each {
            dependants.add(springBackendContentService.getContentItem(site, it, 0))
        }
        return dependants
    }

    def getDependenciesItems(site, path) {
        def springBackendService = this.context.applicationContext.get(DEPENDENCY_SERVICES_BEAN);
        def springBackendContentService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
        def dependencies = []
        def dependencyPaths = springBackendService.getItemDependencies(site, path, 1)
        dependencyPaths.each {
            dependencies.add(springBackendContentService.getContentItem(site, it, 0))
        }
        return dependencies
    }
}
