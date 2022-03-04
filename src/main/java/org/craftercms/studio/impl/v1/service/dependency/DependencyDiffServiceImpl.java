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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.dependency.DependencyDiffService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DependencyDiffServiceImpl implements DependencyDiffService {

    protected DependencyService dependencyService;

    public DependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }

    /**
     * Computes addedDependenices and removedDependenices based on the DiffRequest information provided
     * @param diffRequest
     * @return diff response object
     * @throws ServiceLayerException
     */
    public DiffResponse diff(DiffRequest diffRequest) throws ServiceLayerException {

        if(diffRequest == null)
            throw new ServiceLayerException("diffcontext cannot be null");

        DiffResponse response = new DiffResponse();
        boolean recursive = diffRequest.isRecursive();
        String site = diffRequest.getSite();

        String sourcePath = diffRequest.getSourcePath();
        String destPath = diffRequest.getDestPath();
        if(StringUtils.isEmpty(destPath)){
            destPath = sourcePath;
        }

        List<String> sourceDependencies = new ArrayList<String>();
        sourceDependencies = findDependencies(site,diffRequest.getSourceSandbox(),sourcePath, recursive, sourceDependencies);
        List<String> destDependencies =  new ArrayList<String>();
        destDependencies = findDependencies(site,diffRequest.getDestSandbox(),destPath, recursive, destDependencies);

        //Removed dependenices
        for(String destDependency:destDependencies){
            if(!sourceDependencies.contains(destDependency)){
                response.getRemovedDependencies().add(destDependency);
            }
        }
        //Added dependenices
        for(String sourceDependency:sourceDependencies){
            if(!destDependencies.contains(sourceDependency)){
                response.getAddedDependencies().add(sourceDependency);
            }
        }
        return response;
    }

    protected List<String> findDependencies(String site, String sandbox, String relativePath, boolean isRecursive,
                                            List<String> dependencies) throws ServiceLayerException {
        Set<String> dependenciesFromDoc = dependencyService.getItemDependencies(site, relativePath, 1);
        dependencies.addAll(dependenciesFromDoc);
        if(isRecursive){
            for(String dependency:dependenciesFromDoc){
                if (!dependencies.contains(dependency)) {
                    dependencies.addAll(findDependencies(site, sandbox, dependency, isRecursive, dependencies));
                }
            }
        }
        return dependencies;
    }
}
