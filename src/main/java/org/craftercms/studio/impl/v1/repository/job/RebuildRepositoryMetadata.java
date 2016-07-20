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
import org.craftercms.studio.api.v1.dal.RebuildRepositoryMetadataMapper;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class RebuildRepositoryMetadata {

    private final static Logger logger = LoggerFactory.getLogger(RebuildRepositoryMetadata.class);

    private static ReentrantLock taskLock = new ReentrantLock();

    public void execute(String site) {
        if (taskLock.tryLock()) {
            try {
                logger.debug("Starting Rebuild Repository Metadata Task.");
                String ticket = securityService.getCurrentToken();
                CronJobContext securityContext = new CronJobContext(ticket);
                RebuildRepositoryMetadataTask task = new RebuildRepositoryMetadataTask(securityContext, site);
                taskExecutor.execute(task);
            } finally {
                taskLock.unlock();
            }
        }
    }

    class RebuildRepositoryMetadataTask implements Runnable {

        private String site;
        private CronJobContext securityContext;

        public RebuildRepositoryMetadataTask(CronJobContext securityContext, String site) {
            this.securityContext = securityContext;
            this.site = site;
        }

        @Override
        public void run() {
            CronJobContext.setCurrent(securityContext);
            List<Map<String, Object>> existingQueue = getExistingQueue(site);
            if (!(existingQueue != null && existingQueue.size() > 0)) {
                logger.debug("Previous task execution queue does not exist.");
                try {
                    logger.debug("Cleaning previous task queue.");
                    rebuildRepositoryMetadataMapper.deleteRebuildRepoMetadataQueue();
                } catch (Exception err) {
                    logger.info("Error while deleting rebuild repository metadata queue: " + err.getMessage());
                }
                logger.debug("Cleaning existing repository metadata for site " + site);
                cleanOldMetadata(site);
                logger.debug("Create and populate Rebuild Repository Metadata Task Queue.");
                rebuildRepositoryMetadataMapper.createRebuildRepoMetadataQueue();
                populateRebuildRepositoryMetadataQueue(site);
            }
            logger.debug("Initiate rebuild metadata process for site " + site);
            rebuildMetadata(site, existingQueue);
            logger.debug("Cleanup rebuild repository metadata queue after task was completed.");
            rebuildRepositoryMetadataMapper.deleteRebuildRepoMetadataQueue();
            CronJobContext.clear();
        }
    }

    protected List<Map<String, Object>> getExistingQueue(String site) {
        logger.debug("Get rebuild metadata queue for site " + site + " (batch size: " + batchSize + ").");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("batchSize", batchSize);
        List<Map<String, Object>> existingQueue = null;
        try {
            existingQueue = rebuildRepositoryMetadataMapper.getNextBatchFromQueue(params);
        } catch (Exception err) {
            logger.error("Error while getting rebuild repository metadata: " + err.getMessage());
        }
        return existingQueue;
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

    protected boolean populateRebuildRepositoryMetadataQueue(String site) {
        logger.debug("Populating Rebuild Repository Metadata queue for site " + site);
        Path siteContentRootPath = Paths.get(previewRepoRootPath, contentService.expandRelativeSitePath(site, ""));
        logger.debug("Retrieving files list for content repository");
        Iterator<File> fileIterator = FileUtils.iterateFiles(Paths.get(previewRepoRootPath, contentService.expandRelativeSitePath(site, "")).toFile(), null, true);
        List<String> paths = new ArrayList<String>();
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            Path filePath = Paths.get(file.toURI());
            String relativePath = "/" + filePath.subpath(siteContentRootPath.getNameCount(), filePath.getNameCount());
            logger.debug("Processing " + relativePath);
            paths.add(relativePath);
            if (paths.size() == batchSize) {
                logger.debug("Insert batch of file paths into queue.");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("pathList", paths);
                rebuildRepositoryMetadataMapper.insertRebuildRepoMetadataQueue(params);
                paths = new ArrayList<String>();
            }
        }
        if (paths != null && paths.size() > 0) {
            logger.debug("Insert batch of file paths into queue.");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("pathList", paths);
            rebuildRepositoryMetadataMapper.insertRebuildRepoMetadataQueue(params);
            paths = new ArrayList<String>();
        }
        return true;
    }

    protected boolean rebuildMetadata(String site, List<Map<String, Object>> existingQueue) {
        if (existingQueue == null || existingQueue.size() < 1) {
            existingQueue = getExistingQueue(site);
        }
        while (existingQueue != null && existingQueue.size() > 0) {
            for (Map<String, Object> queueItem : existingQueue) {
                String relativePath = queueItem.get("path").toString();
                logger.debug("Processing " + relativePath);
                logger.debug("Insert content metadata.");
                objectMetadataManager.insertNewObjectMetadata(site, relativePath);
                logger.debug("Insert workflow state");
                objectStateService.insertNewEntry(site, relativePath);
                if (relativePath.endsWith(DmConstants.XML_PATTERN)) {
                    logger.debug("Calculate dependencies");
                    try {
                        Document document = contentService.getContentAsDocument(contentService.expandRelativeSitePath(site, relativePath));
                        dmDependencyService.extractDependencies(site, relativePath, document, null);
                    } catch (DocumentException | ServiceException err) {
                        logger.debug("Error while calculating dependencies for " + relativePath, err);
                    }

                }
                logger.debug("Mark file as processed.");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("path", relativePath);
                rebuildRepositoryMetadataMapper.markProcessed(params);
            }
            existingQueue = getExistingQueue(site);
        }
        return false;
    }

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;

    @Autowired
    protected PublishToTargetMapper publishToTargetMapper;

    @Autowired
    protected RebuildRepositoryMetadataMapper rebuildRepositoryMetadataMapper;

    protected ObjectMetadataManager objectMetadataManager;
    protected ObjectStateService objectStateService;
    protected DmDependencyService dmDependencyService;
    protected ContentService contentService;
    protected SecurityService securityService;
    protected String previewRepoRootPath;
    protected TaskExecutor taskExecutor;
    protected int batchSize;

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public String getPreviewRepoRootPath() { return previewRepoRootPath; }
    public void setPreviewRepoRootPath(String previewRepoRootPath) { this.previewRepoRootPath = previewRepoRootPath; }

    public TaskExecutor getTaskExecutor() { return taskExecutor; }
    public void setTaskExecutor(TaskExecutor taskExecutor) { this.taskExecutor = taskExecutor; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
}
