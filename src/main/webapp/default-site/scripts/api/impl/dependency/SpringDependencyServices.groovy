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
package scripts.api.impl.dependency

import net.sf.json.JSONArray
import net.sf.json.JSONSerializer
import org.apache.commons.lang.StringUtils;

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

    def getDependencies(site, requestBody, deleteDependencies) {
        def paths = []
        //def dependencies = []
        def jsonArray = (JSONArray) JSONSerializer.toJSON(requestBody)
        if (jsonArray != null && jsonArray.size() > 0) {
            def iterator = jsonArray.listIterator()
            while (iterator.hasNext()) {
                def jsonObject = iterator.next()
                def uri = jsonObject.getString("uri")
                paths.add(uri)
            }
        }
        def springBackendService = this.context.applicationContext.get(DEPENDENCY_SERVICES_BEAN);
        def springBackendContentService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
        def dependencies = []
        def sb = new StringBuilder()
        def submissionComments = new HashSet<String>()
        def toProcess = []
        toProcess.addAll(paths)
        if (deleteDependencies) {
            def dependencyPaths = springBackendService.getDeleteDependencies(site, paths)
            toProcess.addAll(dependencyPaths)
        } else {
            toProcess = springBackendService.getPublishingDependencies(site, paths)
        }

        toProcess.each {
            def item = springBackendContentService.getContentItem(site, it, 0)
            dependencies.add(item)
            def comment = item.getSubmissionComment();
            if (StringUtils.isNotEmpty(comment)) {
                if (!submissionComments.contains(comment)) {
                    sb.append(comment).append("\n");
                    submissionComments.add(comment);
                }
            }
        }

        def result = [:]
        result.items = dependencies
        result.submissionComment = sb.toString()
        result.dependencies = dependencies
        return result
        return springBackendService.getDependencies(site, requestBody, deleteDependencies);
    }

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

    def calculateDependencies(site, paths) {
        def springBackendService = this.context.applicationContext.get(DEPENDENCY_SERVICES_BEAN)
        return springBackendService.calculateDependencies(site, paths)
    }
}
