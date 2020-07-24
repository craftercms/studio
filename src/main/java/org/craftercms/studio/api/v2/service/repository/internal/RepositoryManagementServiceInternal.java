/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.repository.internal;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteNotRemovableException;
import org.craftercms.studio.api.v2.dal.DiffConflictedFile;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.dal.RepositoryStatus;

import java.util.List;

public interface RepositoryManagementServiceInternal {

    boolean addRemote(String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException;

    List<RemoteRepositoryInfo> listRemotes(String siteId, String sandboxBranch)
            throws ServiceLayerException, CryptoException;

    boolean pullFromRemote(String siteId, String remoteName, String remoteBranch, String mergeStrategy)
            throws InvalidRemoteUrlException, ServiceLayerException, CryptoException;

    boolean pushToRemote(String siteId, String remoteName, String remoteBranch, boolean force)
            throws CryptoException, ServiceLayerException, InvalidRemoteUrlException;

    boolean removeRemote(String siteId, String remoteName) throws CryptoException, RemoteNotRemovableException;

    RepositoryStatus getRepositoryStatus(String siteId) throws CryptoException, ServiceLayerException;

    boolean resolveConflict(String siteId, String path, String resolution)
            throws CryptoException, ServiceLayerException;

    DiffConflictedFile getDiffForConflictedFile(String siteId, String path)
            throws ServiceLayerException, CryptoException;

    boolean commitResolution(String siteId, String commitMessage) throws CryptoException, ServiceLayerException;

    boolean cancelFailedPull(String siteId) throws ServiceLayerException, CryptoException;
}
