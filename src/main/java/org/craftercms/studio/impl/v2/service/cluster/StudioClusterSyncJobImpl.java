/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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

package org.craftercms.studio.impl.v2.service.cluster;

import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.search.SearchService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.service.cluster.StudioClusterSyncJob;
import org.springframework.core.task.TaskExecutor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class StudioClusterSyncJobImpl implements StudioClusterSyncJob {

    private final static Logger logger = LoggerFactory.getLogger(StudioClusterSyncJobImpl.class);

    private SiteService siteService;
    private TaskExecutor taskExecutor;
    private PreviewDeployer previewDeployer;
    private SearchService searchService;
    private StudioConfiguration studioConfiguration;
    private ContentRepository contentRepository;
    private ClusterDAO clusterDAO;

    protected static final ReentrantLock singleWorkerLock = new ReentrantLock();

    @Override
    public void run() {
        logger.error(singleWorkerLock.toString() + " " + singleWorkerLock.getHoldCount());
        if (singleWorkerLock.tryLock()) {
            logger.error("Do the work - single thread : count " + singleWorkerLock.getHoldCount());
            try {
                Set<String> siteNames = siteService.getAllAvailableSites();
                List<ClusterMember> clusterMembers = clusterDAO.getAllMembers();
                if ((clusterMembers != null && clusterMembers.size() > 0) && (siteNames != null && siteNames.size() > 0)) {
                    for (String site : siteNames) {
                        StudioNodeSyncTaskImpl nodeSyncTask = new StudioNodeSyncTaskImpl();
                        nodeSyncTask.setSiteId(site);
                        nodeSyncTask.setPreviewDeployer(previewDeployer);
                        nodeSyncTask.setSearchService(searchService);
                        nodeSyncTask.setStudioConfiguration(studioConfiguration);
                        nodeSyncTask.setContentRepository(contentRepository);
                        nodeSyncTask.setClusterNodes(clusterMembers);

                        taskExecutor.execute(nodeSyncTask);
                    }
                }
            } catch (Exception err) {
                logger.error("Error while executing cluster sync job", err);
            } finally {
                singleWorkerLock.unlock();
            }

        } else {
            logger.error("Another thread working ... leave");
        }
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public PreviewDeployer getPreviewDeployer() {
        return previewDeployer;
    }

    public void setPreviewDeployer(PreviewDeployer previewDeployer) {
        this.previewDeployer = previewDeployer;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public ClusterDAO getClusterDAO() {
        return clusterDAO;
    }

    public void setClusterDAO(ClusterDAO clusterDAO) {
        this.clusterDAO = clusterDAO;
    }
}
