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

package org.craftercms.studio.api.v2.service.repository;

import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteNotRemovableException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v2.dal.DiffConflictedFile;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.dal.RepositoryStatus;

import java.util.List;

public interface RepositoryManagementService {

    boolean addRemote(String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException, RemoteRepositoryNotFoundException;

    List<RemoteRepositoryInfo> listRemotes(String siteId) throws ServiceLayerException;

    MergeResult pullFromRemote(String siteId, String remoteName, String remoteBranch, String mergeStrategy)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException;

    boolean pushToRemote(String siteId, String remoteName, String remoteBranch, boolean force)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException;

    void rebuildDatabase(String siteId);

    boolean removeRemote(String siteId, String remoteName)
            throws SiteNotFoundException, RemoteNotRemovableException;

    RepositoryStatus getRepositoryStatus(String siteId) throws ServiceLayerException;

    RepositoryStatus resolveConflict(String siteId, String path, String resolution)
            throws ServiceLayerException;

    DiffConflictedFile getDiffForConflictedFile(String siteId, String path)
            throws ServiceLayerException;

    RepositoryStatus commitResolution(String siteId, String commitMessage)
            throws ServiceLayerException;

    RepositoryStatus cancelFailedPull(String siteId) throws ServiceLayerException;

    /**
     * Unlock local git repository
     *
     * @param siteId site identifier, if null or empty it is global repository
     * @param repositoryType repository type (GLOBAL, SANDBOX, PUBLISHED)
     * @return true if successful
     */
    boolean unlockRepository(String siteId, GitRepositories repositoryType);

    /**
     * Checks if a given Git repository is corrupted
     * @param siteId the id of the site
     * @param repositoryType the type of the repository
     * @return true if the repo is corrupted
     * @throws ServiceLayerException if there is any error checking the repository
     */
    boolean isCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException;

    /**
     * Repairs a corrupted Git repository
     * @param siteId the id of the site
     * @param repositoryType the type of the repository
     * @throws ServiceLayerException if there is any error repairing the repository
     */
    void repairCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException;

}
