/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.dal;

import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.dal.ItemStateMapper;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.GitLogDAO;
import org.craftercms.studio.api.v2.dal.RetryingOperationFacade;

import java.util.Map;

@RetryingOperation
public class RetryingOperationFacadeImpl implements RetryingOperationFacade {

    private GitLogDAO gitLogDao;
    private ClusterDAO clusterDao;
    private DependencyMapper dependencyMapper;
    private PublishRequestMapper publishRequestMapper;
    private ItemStateMapper itemStateMapper;
    private SiteFeedMapper siteFeedMapper;

    @Override
    public void markGitLogProcessedBeforeMarker(String siteId, long marker, int processed, int unprocessed) {
        gitLogDao.markGitLogProcessedBeforeMarker(siteId, marker, processed, unprocessed);
    }

    @Override
    public void markGitLogAudited(String siteId, String commitId, int audited) {
        gitLogDao.markGitLogAudited(siteId, commitId, audited);
    }

    @Override
    public void addClusterRemoteRepository(long clusterId, long remoteRepositoryId) {
        clusterDao.addClusterRemoteRepository(clusterId, remoteRepositoryId);
    }

    @Override
    public void deleteDependenciesForSite(Map params) {
        dependencyMapper.deleteDependenciesForSite(params);
    }

    @Override
    public void deleteDeploymentDataForSite(Map params) {
        publishRequestMapper.deleteDeploymentDataForSite(params);
    }

    @Override
    public void deleteAllSourceDependencies(Map params) {
        dependencyMapper.deleteAllSourceDependencies(params);
    }

    @Override
    public void insertDependenciesList(Map params) {
        dependencyMapper.insertList(params);
    }

    @Override
    public void cancelWorkflowBulk(Map params) {
        publishRequestMapper.cancelWorkflowBulk(params);
    }

    @Override
    public void markPublishRequestCompleted(PublishRequest publishRequest) {
        publishRequestMapper.markItemCompleted(publishRequest);
    }

    @Override
    public void setSystemProcessingBySiteAndPathBulk(Map params) {
        itemStateMapper.setSystemProcessingBySiteAndPathBulk(params);
    }

    @Override
    public void updateSiteLastCommitId(Map params) {
        siteFeedMapper.updateLastCommitId(params);
    }

    @Override
    public void updateClusterNodeLastCommitId(long clusterNodeId, long siteId, String commitId) {
        clusterDao.updateNodeLastCommitId(clusterNodeId, siteId, commitId);
    }

    @Override
    public void updateSiteLastVerifiedGitlogCommitId(Map params) {
        siteFeedMapper.updateLastVerifiedGitlogCommitId(params);
    }

    @Override
    public void updateClusterNodeLastVerifiedGitlogCommitId(long clusterNodeId, long siteId, String commitId) {
        clusterDao.updateNodeLastVerifiedGitlogCommitId(clusterNodeId, siteId, commitId);
    }

    @Override
    public void updateSiteLastSyncedGitlogCommitId(Map params) {
        siteFeedMapper.updateLastSyncedGitlogCommitId(params);
    }

    @Override
    public void updateClusterNodeLastSyncedGitlogCommitId(long clusterNodeId, long siteId, String commitId) {
        clusterDao.updateNodeLastSyncedGitlogCommitId(clusterNodeId, siteId, commitId);
    }

    @Override
    public void markGitLogProcessed(Map params) {
        gitLogDao.markGitLogProcessed(params);
    }

    public GitLogDAO getGitLogDao() {
        return gitLogDao;
    }

    public void setGitLogDao(GitLogDAO gitLogDao) {
        this.gitLogDao = gitLogDao;
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public DependencyMapper getDependencyMapper() {
        return dependencyMapper;
    }

    public void setDependencyMapper(DependencyMapper dependencyMapper) {
        this.dependencyMapper = dependencyMapper;
    }

    public PublishRequestMapper getPublishRequestMapper() {
        return publishRequestMapper;
    }

    public void setPublishRequestMapper(PublishRequestMapper publishRequestMapper) {
        this.publishRequestMapper = publishRequestMapper;
    }

    public ItemStateMapper getItemStateMapper() {
        return itemStateMapper;
    }

    public void setItemStateMapper(ItemStateMapper itemStateMapper) {
        this.itemStateMapper = itemStateMapper;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }
}
