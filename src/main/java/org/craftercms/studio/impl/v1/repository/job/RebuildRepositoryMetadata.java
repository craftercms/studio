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

package org.craftercms.studio.impl.v1.repository.job;

import org.apache.commons.io.FileUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.CopyToEnvironmentMapper;
import org.craftercms.studio.api.v1.dal.PublishToTargetMapper;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RebuildRepositoryMetadata {

    private final static Logger logger = LoggerFactory.getLogger(RebuildRepositoryMetadata.class);

    public void execute(String site) {
        cleanOldMetadata(site);
        rebuildMetadata(site);
    }

    protected boolean cleanOldMetadata(String site) {
        logger.debug("Clean repository metadata for site " + site);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);

        try {
            // Delete all dependencies
            logger.debug("Deleting dependencies for site " + site);
            dmDependencyService.deleteDependenciesForSite(site);
        } catch (Exception error) {
            logger.error("Failed to delete dependencies for site " + site);
        }

        try {
            // Delete deployment queue
            logger.debug("Deleting deployment queue for site " + site);
            copyToEnvironmentMapper.deleteDeploymentDataForSite(params);
        } catch (Exception error) {
            logger.error("Failed to delete deployment queue for site " + site);
        }
        try {
            // Delete target sync queue
            logger.debug("Deleting target sync queue for site " + site);
            publishToTargetMapper.deleteDeploymentDataForSite(params);
        } catch (Exception error) {
            logger.error("Failed to delete target sync queue for site " + site);
        }

        try {
            // Delete content metadata
            logger.debug("Deleting content metadata for site " + site);
            objectMetadataManager.deleteObjectMetadataForSite(site);
        } catch (Exception error) {
            logger.error("Failed to delete content metadata for site " + site);
        }

        try {
            // Delete content workflow states data
            logger.debug("Deleting workflow states data for site " + site);
            objectStateService.deleteObjectStatesForSite(site);
        } catch (Exception error) {
            logger.error("Failed to delete workflow states data for site " + site);
        }

        return true;
    }

    protected boolean rebuildMetadata(String site) {
        Path siteContentRootPath = Paths.get(previewRepoRootPath, contentService.expandRelativeSitePath(site, ""));
        Iterator<File> fileIterator = FileUtils.iterateFiles(Paths.get(previewRepoRootPath, contentService.expandRelativeSitePath(site, "")).toFile(), null, true);
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            Path filePath = Paths.get(file.toURI());
            String relativePath = "/" + filePath.subpath(siteContentRootPath.getNameCount(), filePath.getNameCount());
            logger.debug("Processing " + relativePath);
            logger.debug("Insert content metadata.");
            objectMetadataManager.insertNewObjectMetadata(site, relativePath);
            logger.debug("Insert workflow state");
            objectStateService.insertNewEntry(site, relativePath);
            if (relativePath.endsWith(DmConstants.XML_PATTERN)) {
                logger.debug("Calculate dependencies");
                SAXReader saxReader = new SAXReader();
                try {
                    Document document = saxReader.read(file);
                    dmDependencyService.extractDependencies(site, relativePath, document, null);
                } catch (DocumentException | ServiceException err) {
                    logger.debug("Error while calculating dependencies for " + relativePath, err);
                }

            }

        }
        return false;
    }

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;

    @Autowired
    protected PublishToTargetMapper publishToTargetMapper;

    protected ObjectMetadataManager objectMetadataManager;
    protected ObjectStateService objectStateService;
    protected DmDependencyService dmDependencyService;
    protected ContentService contentService;
    protected String previewRepoRootPath;

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public String getPreviewRepoRootPath() { return previewRepoRootPath; }
    public void setPreviewRepoRootPath(String previewRepoRootPath) { this.previewRepoRootPath = previewRepoRootPath; }


}
