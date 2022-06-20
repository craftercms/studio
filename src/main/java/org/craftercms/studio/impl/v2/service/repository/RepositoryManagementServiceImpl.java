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

package org.craftercms.studio.impl.v2.service.repository;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteNotRemovableException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.DiffConflictedFile;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.dal.RepositoryStatus;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.repository.MergeResult;
import org.craftercms.studio.api.v2.service.repository.RepositoryManagementService;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;

import java.util.List;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_ADD_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PULL_FROM_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PUSH_TO_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_REMOVE_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_REMOTE_REPOSITORY;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;

public class RepositoryManagementServiceImpl implements RepositoryManagementService {

    private RepositoryManagementServiceInternal repositoryManagementServiceInternal;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private SecurityService securityService;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "add_remote")
    public boolean addRemote(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException, RemoteRepositoryNotFoundException {
        boolean toRet = repositoryManagementServiceInternal.addRemote(siteId, remoteRepository);
        insertAddRemoteAuditLog(siteId, OPERATION_ADD_REMOTE, remoteRepository.getRemoteName(),
                remoteRepository.getRemoteName());
        return toRet;
    }

    private void insertAddRemoteAuditLog(String siteId, String operation, String primaryTargetId,
                                         String primaryTargetValue) throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        String user = securityService.getCurrentUser();
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(operation);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(primaryTargetId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_REMOTE_REPOSITORY);
        auditLog.setPrimaryTargetValue(primaryTargetValue);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "list_remotes")
    public List<RemoteRepositoryInfo> listRemotes(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId)
            throws ServiceLayerException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        return repositoryManagementServiceInternal.listRemotes(siteId, siteFeed.getSandboxBranch());
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "pull_from_remote")
    public MergeResult pullFromRemote(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String remoteName,
                                      String remoteBranch, String mergeStrategy)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        MergeResult toRet = repositoryManagementServiceInternal.pullFromRemote(siteId, remoteName, remoteBranch,
                mergeStrategy);
        insertAddRemoteAuditLog(siteId, OPERATION_PULL_FROM_REMOTE, remoteName + "/" + remoteBranch,
                remoteName + "/" + remoteBranch);
        return toRet;
    }



    @Override
    @HasPermission(type = DefaultPermission.class, action = "push_to_remote")
    public boolean pushToRemote(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String remoteName,
                                String remoteBranch, boolean force)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        boolean toRet = repositoryManagementServiceInternal.pushToRemote(siteId, remoteName, remoteBranch, force);
        insertAddRemoteAuditLog(siteId, OPERATION_PUSH_TO_REMOTE,remoteName + "/" + remoteBranch,
                remoteName + "/" + remoteBranch);
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "rebuild_database")
    public void rebuildDatabase(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        siteService.rebuildDatabase(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "remove_remote")
    public boolean removeRemote(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId, String remoteName)
            throws SiteNotFoundException, RemoteNotRemovableException {
        boolean toRet = repositoryManagementServiceInternal.removeRemote(siteId, remoteName);
        insertAddRemoteAuditLog(siteId, OPERATION_REMOVE_REMOTE, remoteName, remoteName);
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "site_status")
    public RepositoryStatus getRepositoryStatus(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId)
            throws ServiceLayerException {
        return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "resolve_conflict")
    public RepositoryStatus resolveConflict(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                            @ProtectedResourceId(PATH_RESOURCE_ID) String path, String resolution)
            throws ServiceLayerException {
        boolean success = repositoryManagementServiceInternal.resolveConflict(siteId, path, resolution);
        if (success) {
            return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
        } else {
            throw new ServiceLayerException("Failed to resolve conflict for site " + siteId + " path " + path);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "site_diff_conflicted_file")
    public DiffConflictedFile getDiffForConflictedFile(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                       @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws ServiceLayerException {
        return repositoryManagementServiceInternal.getDiffForConflictedFile(siteId, path);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "commit_resolution")
    public RepositoryStatus commitResolution(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                             String commitMessage) throws ServiceLayerException {
        boolean success = repositoryManagementServiceInternal.commitResolution(siteId, commitMessage);
        if (success) {
            return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
        } else {
            throw new ServiceLayerException("Failed to commit conflict resolution for site " + siteId);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "cancel_failed_pull")
    public RepositoryStatus cancelFailedPull(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId)
            throws ServiceLayerException {
        boolean success = repositoryManagementServiceInternal.cancelFailedPull(siteId);
        if (success) {
            return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
        } else {
            throw new ServiceLayerException("Failed to cancel failed pull from remote for site " + siteId);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "unlock_repository")
    public boolean unlockRepository(@ProtectedResourceId(SITE_ID_RESOURCE_ID)String siteId,
                                    GitRepositories repositoryType) {
        return repositoryManagementServiceInternal.unlockRepository(siteId, repositoryType);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "repair_repository")
    public boolean isCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException {
        return repositoryManagementServiceInternal.isCorrupted(siteId, repositoryType);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "repair_repository")
    public void repairCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException {
        repositoryManagementServiceInternal.repairCorrupted(siteId, repositoryType);
    }

    public void setRepositoryManagementServiceInternal(RepositoryManagementServiceInternal repositoryManagementServiceInternal) {
        this.repositoryManagementServiceInternal = repositoryManagementServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
