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
 *
 */

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyRule;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class DeploymentDependencyRule implements DependencyRule {

    private final static Logger logger = LoggerFactory.getLogger(DeploymentDependencyRule.class);

    @Override
    public Set<String> applyRule(String site, String path) {
        Set<String> dependencies = new HashSet<String>();
        List<String> allDependencies = new ArrayList<String>();
        getMandatoryParent(site, path, allDependencies);
        getAllDependenciesRecursive(site, path, allDependencies);
        dependencies.addAll(allDependencies);
        return dependencies;
    }

    protected void getMandatoryParent(String site, String path, List<String> dependecyPaths) {
        int idx = path.lastIndexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE);
        if (idx > 0) {
            path = path.substring(0, idx);
        }
        String parentPath = ContentUtils.getParentUrl(path);
        if (StringUtils.isNotEmpty(parentPath)) {
            if (contentService.contentExists(site, parentPath)) {
                ContentItemTO item = contentService.getContentItem(site, parentPath);

                if (item.isNew() || objectMetadataManager.isRenamed(site, item.getUri())) {
                    dependecyPaths.add(item.getUri());
                    getMandatoryParent(site, item.getUri(), dependecyPaths);
                }
            }
        }
    }

    protected void getAllDependenciesRecursive(String site, String path, List<String> dependecyPaths) {
        List<String> depPaths = dmDependencyService.getDependencyPaths(site, path);
        for (String depPath : depPaths) {
            if (!dependecyPaths.contains(depPath)) {
                if (objectStateService.isNew(site, depPath)) {
                    dependecyPaths.add(depPath);
                    getAllDependenciesRecursive(site, depPath, dependecyPaths);
                } else {
                    if (objectStateService.isUpdated(site, depPath)) {
                        for (String contentSpecificDependency : contentSpecificDependencies) {
                            Pattern p = Pattern.compile(contentSpecificDependency);
                            Matcher m = p.matcher(depPath);
                            if (m.matches()) {
                                dependecyPaths.add(depPath);
                                getAllDependenciesRecursive(site, depPath, dependecyPaths);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public List<String> getContentSpecificDependencies() { return contentSpecificDependencies; }
    public void setContentSpecificDependencies(List<String> contentSpecificDependencies) { this.contentSpecificDependencies = contentSpecificDependencies; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    protected DmDependencyService dmDependencyService;
    protected ObjectStateService objectStateService;
    protected ContentService contentService;
    protected List<String> contentSpecificDependencies;
    protected ObjectMetadataManager objectMetadataManager;
}
