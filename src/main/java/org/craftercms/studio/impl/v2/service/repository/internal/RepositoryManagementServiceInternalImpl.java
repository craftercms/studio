/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.impl.v2.service.repository.internal;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteAlreadyExistsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryDAO;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;
import org.craftercms.studio.api.v2.util.GitRepositoryHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;

public class RepositoryManagementServiceInternalImpl implements RepositoryManagementServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceInternalImpl.class);

    private RemoteRepositoryDAO remoteRepositoryDao;
    private StudioConfiguration studioConfiguration;

    @Override
    public boolean addRemote(String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException {
        boolean isValid = false;
        try {
            logger.debug("Add remote " + remoteRepository.getRemoteName() + " to the sandbox repo for the site " + siteId);
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
            Repository repo = helper.getRepository(siteId, SANDBOX);
            try (Git git = new Git(repo)) {

                Config storedConfig = repo.getConfig();
                Set<String> remotes = storedConfig.getSubsections("remote");

                if (remotes.contains(remoteRepository.getRemoteName())) {
                    throw new RemoteAlreadyExistsException(remoteRepository.getRemoteName());
                }

                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName(remoteRepository.getRemoteName());
                remoteAddCommand.setUri(new URIish(remoteRepository.getRemoteUrl()));
                remoteAddCommand.call();

                try {
                    isValid = helper.isRemoteValid(git, remoteRepository.getRemoteName(),
                            remoteRepository.getAuthenticationType(), remoteRepository.getRemoteUsername(),
                            remoteRepository.getRemotePassword(), remoteRepository.getRemoteToken(),
                            remoteRepository.getRemotePrivateKey());
                } finally {
                    if (!isValid) {
                        RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
                        remoteRemoveCommand.setName(remoteRepository.getRemoteName());
                        remoteRemoveCommand.call();
                    }
                }

            } catch (URISyntaxException e) {
                logger.error("Remote URL is invalid " + remoteRepository.getRemoteUrl(), e);
                throw new InvalidRemoteUrlException();
            } catch (GitAPIException | IOException e) {
                logger.error("Error while adding remote " + remoteRepository.getRemoteName() + " (url: " +
                        remoteRepository.getRemoteUrl() + ") " + "for site " + siteId, e);
                throw new ServiceLayerException("Error while adding remote " + remoteRepository.getRemoteName() +
                        " (url: " + remoteRepository.getRemoteUrl() + ") for site " + siteId, e);
            }

            if (isValid) {
                insertRemoteToDb(siteId, remoteRepository);
            }
        } catch (CryptoException e) {
            throw new ServiceLayerException(e);
        }
        return isValid;
    }

    private void insertRemoteToDb(String siteId, RemoteRepository remoteRepository) throws CryptoException {
        logger.debug("Inserting remote " + remoteRepository.getRemoteName() + " for site " + siteId +
                " into database.");
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteRepository.getRemoteName());
        params.put("remoteUrl", remoteRepository.getRemoteUrl());
        params.put("authenticationType", remoteRepository.getAuthenticationType());
        params.put("remoteUsername", remoteRepository.getRemoteUsername());

        if (StringUtils.isNotEmpty(remoteRepository.getRemotePassword())) {
            logger.debug("Encrypt password before inserting to database");
            String hashedPassword = encryptor.encrypt(remoteRepository.getRemotePassword());
            params.put("remotePassword", hashedPassword);
        } else {
            params.put("remotePassword", remoteRepository.getRemotePassword());
        }
        if (StringUtils.isNotEmpty(remoteRepository.getRemoteToken())) {
            logger.debug("Encrypt token before inserting to database");
            String hashedToken = encryptor.encrypt(remoteRepository.getRemoteToken());
            params.put("remoteToken", hashedToken);
        } else {
            params.put("remoteToken", remoteRepository.getRemoteToken());
        }
        if (StringUtils.isNotEmpty(remoteRepository.getRemotePrivateKey())) {
            logger.debug("Encrypt private key before inserting to database");
            String hashedPrivateKey = encryptor.encrypt(remoteRepository.getRemotePrivateKey());
            params.put("remotePrivateKey", hashedPrivateKey);
        } else {
            params.put("remotePrivateKey", remoteRepository.getRemotePrivateKey());
        }

        logger.debug("Insert site remote record into database");
        remoteRepositoryDao.insertRemoteRepository(params);
    }

    public RemoteRepositoryDAO getRemoteRepositoryDao() {
        return remoteRepositoryDao;
    }

    public void setRemoteRepositoryDao(RemoteRepositoryDAO remoteRepositoryDao) {
        this.remoteRepositoryDao = remoteRepositoryDao;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
