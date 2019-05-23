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

import org.apache.commons.collections4.CollectionUtils;
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
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;
import org.craftercms.studio.api.v2.util.GitRepositoryHelper;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_SANDBOX_BRANCH;
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

    @Override
    public List<RemoteRepositoryInfo> listRemotes(String siteId, String sandboxBranch) throws ServiceLayerException {
        List<RemoteRepositoryInfo> res = new ArrayList<RemoteRepositoryInfo>();
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
        try (Repository repo = helper.getRepository(siteId, SANDBOX)) {
            try (Git git = new Git(repo)) {
                TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                        studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
                List<RemoteConfig> resultRemotes = git.remoteList().call();
                if (CollectionUtils.isNotEmpty(resultRemotes)) {
                    for (RemoteConfig conf : resultRemotes) {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("siteId", siteId);
                        params.put("remoteName", conf.getName());
                        RemoteRepository remoteRepository = remoteRepositoryDao.getRemoteRepository(params);
                        if (remoteRepository != null) {
                            String password = StringUtils.EMPTY;
                            FetchCommand fetchCommand = git.fetch().setRemote(conf.getName());
                            fetchCommand = helper.setAuthenticationForCommand(fetchCommand,
                                    remoteRepository.getAuthenticationType(), remoteRepository.getRemoteUsername(),
                                    password,)
                            switch (remoteRepository.getAuthenticationType()) {
                                case RemoteRepository.AuthenticationType.NONE:
                                    logger.debug("No authentication");
                                    git.fetch().setRemote(conf.getName()).call();
                                    break;
                                case RemoteRepository.AuthenticationType.BASIC:
                                    logger.debug("Basic authentication");
                                    String hashedPassword = remoteRepository.getRemotePassword();
                                    String password = encryptor.decrypt(hashedPassword);
                                    git.fetch().setRemote(conf.getName()).setCredentialsProvider(
                                            new UsernamePasswordCredentialsProvider(
                                                    remoteRepository.getRemoteUsername(), password)).call();
                                    break;
                                case RemoteRepository.AuthenticationType.TOKEN:
                                    logger.debug("Token based authentication");
                                    String hashedToken = remoteRepository.getRemoteToken();
                                    String remoteToken = encryptor.decrypt(hashedToken);
                                    git.fetch().setRemote(conf.getName()).setCredentialsProvider(
                                            new UsernamePasswordCredentialsProvider(remoteToken, StringUtils.EMPTY)).call();
                                    break;
                                case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                                    logger.debug("Private key authentication");
                                    final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                                    String hashedPrivateKey = remoteRepository.getRemotePrivateKey();
                                    String privateKey = encryptor.decrypt(hashedPrivateKey);
                                    tempKey.toFile().deleteOnExit();
                                    git.fetch().setRemote(conf.getName()).setTransportConfigCallback(
                                            new TransportConfigCallback() {
                                                @Override
                                                public void configure(Transport transport) {
                                                    SshTransport sshTransport = (SshTransport) transport;
                                                    sshTransport.setSshSessionFactory(helper.getSshSessionFactory(privateKey, tempKey));
                                                }
                                            }).call();
                                    Files.delete(tempKey);
                                    break;
                                default:
                                    throw new ServiceLayerException("Unsupported authentication type " +
                                            remoteRepository.getAuthenticationType());
                            }
                        }
                    }
                    List<Ref> resultRemoteBranches = git.branchList()
                            .setListMode(ListBranchCommand.ListMode.REMOTE)
                            .call();
                    Map<String, List<String>> remoteBranches = new HashMap<String, List<String>>();
                    for (Ref remoteBranchRef : resultRemoteBranches) {
                        String branchFullName = remoteBranchRef.getName().replace(Constants.R_REMOTES, "");
                        String remotePart = StringUtils.EMPTY;
                        String branchNamePart = StringUtils.EMPTY;
                        int slashIndex = branchFullName.indexOf("/");
                        if (slashIndex > 0) {
                            remotePart = branchFullName.substring(0, slashIndex);
                            branchNamePart = branchFullName.substring(slashIndex + 1);
                        }

                        if (!remoteBranches.containsKey(remotePart)) {
                            remoteBranches.put(remotePart, new ArrayList<String>());
                        }
                        remoteBranches.get(remotePart).add(branchNamePart);
                    }
                    String sandboxBranchName = sandboxBranch;
                    if (StringUtils.isEmpty(sandboxBranchName)) {
                        sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
                    }
                    for (RemoteConfig conf : resultRemotes) {
                        RemoteRepositoryInfo rri = new RemoteRepositoryInfo();
                        rri.setName(conf.getName());
                        List<String> branches = remoteBranches.get(rri.getName());
                        if (CollectionUtils.isEmpty(branches)) {
                            branches = new ArrayList<String>();
                            branches.add(sandboxBranchName);
                        }
                        rri.setBranches(branches);

                        StringBuilder sbUrl = new StringBuilder();
                        if (CollectionUtils.isNotEmpty(conf.getURIs())) {
                            for (int i = 0; i < conf.getURIs().size(); i++) {
                                sbUrl.append(conf.getURIs().get(i).toString());
                                if (i < conf.getURIs().size() - 1) {
                                    sbUrl.append(":");
                                }
                            }
                        }
                        rri.setUrl(sbUrl.toString());

                        StringBuilder sbFetch = new StringBuilder();
                        if (CollectionUtils.isNotEmpty(conf.getFetchRefSpecs())) {
                            for (int i = 0; i < conf.getFetchRefSpecs().size(); i++) {
                                sbFetch.append(conf.getFetchRefSpecs().get(i).toString());
                                if (i < conf.getFetchRefSpecs().size() - 1) {
                                    sbFetch.append(":");
                                }
                            }
                        }
                        rri.setFetch(sbFetch.toString());

                        StringBuilder sbPushUrl = new StringBuilder();
                        if (CollectionUtils.isNotEmpty(conf.getPushURIs())) {
                            for (int i = 0; i < conf.getPushURIs().size(); i++) {
                                sbPushUrl.append(conf.getPushURIs().get(i).toString());
                                if (i < conf.getPushURIs().size() - 1) {
                                    sbPushUrl.append(":");
                                }
                            }
                        } else {
                            sbPushUrl.append(rri.getUrl());
                        }
                        rri.setPushUrl(sbPushUrl.toString());
                        res.add(rri);
                    }
                }
            } catch (GitAPIException | CryptoException | IOException e) {
                logger.error("Error getting remote repositories for site " + siteId, e);
            }
        }
        return res;
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
