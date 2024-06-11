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
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.RequireSiteReady;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.exception.PullFromRemoteConflictException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.repository.MergeResult;
import org.craftercms.studio.api.v2.service.repository.RepositoryManagementService;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;

import java.beans.ConstructorProperties;
import java.util.List;

import static java.lang.String.format;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

@RequireSiteReady
public class RepositoryManagementServiceImpl implements RepositoryManagementService {

    private final RepositoryManagementServiceInternal repositoryManagementServiceInternal;
    private final SiteService siteService;
    private final AuditServiceInternal auditServiceInternal;
    private final SecurityService securityService;

    @ConstructorProperties({"repositoryManagementServiceInternal", "siteService", "auditServiceInternal", "securityService"})
    public RepositoryManagementServiceImpl(final RepositoryManagementServiceInternal repositoryManagementServiceInternal,
                                           final SiteService siteService, final AuditServiceInternal auditServiceInternal,
                                           final SecurityService securityService) {
        this.repositoryManagementServiceInternal = repositoryManagementServiceInternal;
        this.siteService = siteService;
        this.auditServiceInternal = auditServiceInternal;
        this.securityService = securityService;
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_ADD_REMOTE)
    public boolean addRemote(@SiteId String siteId, RemoteRepository remoteRepository)
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
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_LIST_REMOTES)
    public List<RemoteRepositoryInfo> listRemotes(@SiteId String siteId)
            throws ServiceLayerException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        return repositoryManagementServiceInternal.listRemotes(siteId, siteFeed.getSandboxBranch());
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_PULL_FROM_REMOTE)
    public MergeResult pullFromRemote(@SiteId String siteId, String remoteName,
                                      String remoteBranch, String mergeStrategy)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        MergeResult mergeResult = repositoryManagementServiceInternal.pullFromRemote(siteId, remoteName, remoteBranch,
                mergeStrategy);
        insertAddRemoteAuditLog(siteId, OPERATION_PULL_FROM_REMOTE, remoteName + "/" + remoteBranch,
                remoteName + "/" + remoteBranch);

        if (!mergeResult.isSuccessful()) {
            throw new PullFromRemoteConflictException("Pull from remote result is merge conflict.");
        }
        return mergeResult;
    }


    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_PUSH_TO_REMOTE)
    public boolean pushToRemote(@SiteId String siteId, String remoteName,
                                String remoteBranch, boolean force)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        boolean toRet = repositoryManagementServiceInternal.pushToRemote(siteId, remoteName, remoteBranch, force);
        insertAddRemoteAuditLog(siteId, OPERATION_PUSH_TO_REMOTE, remoteName + "/" + remoteBranch,
                remoteName + "/" + remoteBranch);
        return toRet;
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_REBUILD_DATABASE)
    public void rebuildDatabase(@SiteId String siteId) throws SiteNotFoundException {
        siteService.rebuildDatabase(siteId);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_REMOVE_REMOTE)
    public boolean removeRemote(@SiteId String siteId, String remoteName)
            throws SiteNotFoundException, RemoteNotRemovableException {
        boolean toRet = repositoryManagementServiceInternal.removeRemote(siteId, remoteName);
        insertAddRemoteAuditLog(siteId, OPERATION_REMOVE_REMOTE, remoteName, remoteName);
        return toRet;
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_SITE_STATUS)
    public RepositoryStatus getRepositoryStatus(@SiteId String siteId)
            throws ServiceLayerException {
        return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_RESOLVE_CONFLICT)
    public RepositoryStatus resolveConflict(@SiteId String siteId,
                                            @ProtectedResourceId(PATH_RESOURCE_ID) String path, String resolution)
            throws ServiceLayerException {
        boolean success = repositoryManagementServiceInternal.resolveConflict(siteId, path, resolution);
        if (success) {
            return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
        }
        throw new ServiceLayerException("Failed to resolve conflict for site " + siteId + " path " + path);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_SITE_DIFF_CONFLICTED_FILE)
    public DiffConflictedFile getDiffForConflictedFile(@SiteId String siteId,
                                                       @ProtectedResourceId(PATH_RESOURCE_ID) String path)
            throws ServiceLayerException {
        return repositoryManagementServiceInternal.getDiffForConflictedFile(siteId, path);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_COMMIT_RESOLUTION)
    public RepositoryStatus commitResolution(@SiteId String siteId,
                                             String commitMessage) throws ServiceLayerException {
        boolean success = repositoryManagementServiceInternal.commitResolution(siteId, commitMessage);
        if (success) {
            return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
        }
        throw new ServiceLayerException(format("Failed to commit conflict resolution for site '%s'", siteId));
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CANCEL_FAILED_PULL)
    public RepositoryStatus cancelFailedPull(@SiteId String siteId)
            throws ServiceLayerException {
        boolean success = repositoryManagementServiceInternal.cancelFailedPull(siteId);
        if (success) {
            return repositoryManagementServiceInternal.getRepositoryStatus(siteId);
        }
        throw new ServiceLayerException("Failed to cancel failed pull from remote for site " + siteId);
    }

    @Override
    @RequireSiteExists
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UNLOCK_REPO)
    public boolean unlockRepository(@SiteId String siteId,
                                    GitRepositories repositoryType) throws SiteNotFoundException {
        return repositoryManagementServiceInternal.unlockRepository(siteId, repositoryType);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_REPAIR_REPOSITORY)
    public boolean isCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException {
        return repositoryManagementServiceInternal.isCorrupted(siteId, repositoryType);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_REPAIR_REPOSITORY)
    public void repairCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException {
        repositoryManagementServiceInternal.repairCorrupted(siteId, repositoryType);
    }
}
