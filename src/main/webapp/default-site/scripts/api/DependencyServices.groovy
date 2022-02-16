
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
/**
 * Dependency services
 */
class DependencyServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

    def static getDependencies(context, site, requestBody, deleteDependencies) {
        def dependencyServiceImpl = ServiceFactory.getDependencyServices(context);
        return dependencyServiceImpl.getDependencies(site, requestBody, deleteDependencies);
    }

    static getDependantItems(context,site,path){
        def dependencyServiceImpl = ServiceFactory.getDependencyServices(context);
        return dependencyServiceImpl.getDependantItems(site, path);
    }


    static getDependenciesItems(context,site,path){
        def dependencyServiceImpl = ServiceFactory.getDependencyServices(context);
        return dependencyServiceImpl.getDependenciesItems(site, path);
    }

    static calculateDependencies(context, site, paths) {
        def dependencyServiceImpl = ServiceFactory.getDependencyServices(context);
        return dependencyServiceImpl.calculateDependencies(site, paths);
    }
}
