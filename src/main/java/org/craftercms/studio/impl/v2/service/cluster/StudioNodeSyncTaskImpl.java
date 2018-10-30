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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.RemoteRepository;
import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.search.SearchService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.Cluster;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;

public class StudioNodeSyncTaskImpl implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncTaskImpl.class);

    protected String siteId;
    protected List<Cluster> clusterNodes;
    protected SearchService searchService;
    protected PreviewDeployer previewDeployer;
    protected StudioConfiguration studioConfiguration;

    @Override
    public void run() {
        boolean success = false;
        boolean siteCheck = checkIfSiteRepoExists();
        if (!siteCheck) {
            try {
                searchService.createIndex(siteId);
            } catch (ServiceLayerException e) {
                logger.error("Error creating search index on cluster node for site " + siteId + "." +
                                " Is the Search running and configured correctly in Studio?", e);
            }

            try {
                success = previewDeployer.createTarget(siteId);
            } catch (Exception e) {
                success = false;
                logger.error("Error while creating site: " + siteId +
                        ". Is the Preview Deployer running and configured correctly in Studio?", e);
            }
            if (!success) {
                // Rollback search index creation
                try {
                    searchService.deleteIndex(siteId);
                } catch (ServiceLayerException e) {
                    logger.error("Error while rolling back/deleting site: " + siteId + ". This means the site search " +
                            "index (core) is still present, but the site is not successfully created.", e);
                }
            }
            try {
                createSiteFromRemote();
            } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException | RemoteRepositoryNotFoundException | ServiceLayerException e) {
                e.printStackTrace();
            }

            addRemotes();
        }

        try {
            updateContent();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CryptoException e) {
            e.printStackTrace();
        } catch (ServiceLayerException e) {
            e.printStackTrace();
        }
    }

    private boolean createSiteFromRemote()
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException {

        Cluster remoteNode = clusterNodes.get(0);
        boolean toRet = true;
        // prepare a new folder for the cloned repository
        Path siteSandboxPath = buildRepoPath(SANDBOX);
        File localPath = siteSandboxPath.toFile();
        localPath.delete();
        logger.debug("Add user credentials if provided");
        // then clone
        logger.debug("Cloning from " + remoteNode.getRemoteUrl() + " to " + localPath);
        CloneCommand cloneCommand = Git.cloneRepository();
        Git cloneResult = null;

        try {
            final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(),".tmp");
            switch (remoteNode.getAuthenticationType()) {
                case RemoteRepository.AuthenticationType.NONE:
                    logger.debug("No authentication");
                    break;
                case RemoteRepository.AuthenticationType.BASIC:
                    logger.debug("Basic authentication");
                    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                            remoteNode.getRemoteUsername(),
                            remoteNode.getRemotePassword()));
                    break;
                case RemoteRepository.AuthenticationType.TOKEN:
                    logger.debug("Token based authentication");
                    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                            remoteNode.getRemoteToken(),
                            StringUtils.EMPTY));
                    break;
                case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                    logger.debug("Private key authentication");
                    tempKey.toFile().deleteOnExit();
                    cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
                        @Override
                        public void configure(Transport transport) {
                            SshTransport sshTransport = (SshTransport)transport;
                            sshTransport.setSshSessionFactory(
                                    getSshSessionFactory(remoteNode.getRemotePrivateKey(), tempKey));
                        }
                    });

                    break;
                default:
                    throw new ServiceLayerException("Unsupported authentication type " + remoteNode.getAuthenticationType());
            }
            cloneResult = cloneCommand
                    .setURI(remoteNode.getRemoteUrl())
                    .setDirectory(localPath)
                    .setRemote(remoteNode.getRemoteName())
                    .setCloneAllBranches(true)
                    .call();
            Files.deleteIfExists(tempKey);

        } catch (InvalidRemoteException e) {
            logger.error("Invalid remote repository: " + remoteNode.getRemoteName() + " (" + remoteNode.getRemoteUrl() + ")", e);
            throw new InvalidRemoteRepositoryException("Invalid remote repository: " + remoteNode.getRemoteName() + " (" +
                    remoteNode.getRemoteUrl() + ")");
        } catch (TransportException e) {
            if (StringUtils.endsWithIgnoreCase(e.getMessage(), "not authorized")) {
                logger.error("Bad credentials or read only repository: " + remoteNode.getRemoteName() + " (" + remoteNode.getRemoteUrl() + ")",
                        e);
                throw new InvalidRemoteRepositoryCredentialsException("Bad credentials or read only repository: " +
                        remoteNode.getRemoteName() + " (" + remoteNode.getRemoteUrl() + ") for username " + remoteNode.getRemoteUsername(), e);
            } else {
                logger.error("Remote repository not found: " + remoteNode.getRemoteName() + " (" + remoteNode.getRemoteUrl() + ")",
                        e);
                throw new RemoteRepositoryNotFoundException("Remote repository not found: " + remoteNode.getRemoteName() + " (" +
                        remoteNode.getRemoteUrl() + ")");
            }
        } catch (GitAPIException | IOException e) {
            logger.error("Error while creating repository for site with path" + siteSandboxPath.toString(), e);
            toRet = false;
        } finally {
            if (cloneResult != null) {
                cloneResult.close();
            }
        }
        return toRet;
    }

    private Path buildRepoPath(GitRepositories repoType) {
        Path path;
        switch (repoType) {
            case SANDBOX:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), siteId,
                        studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH));
                break;
            case PUBLISHED:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), siteId,
                        studioConfiguration.getProperty(StudioConfiguration.PUBLISHED_PATH));
                break;
            default:
                path = null;
        }

        return path;
    }

    private void addRemotes() {

    }

    private void updateContent() throws IOException, CryptoException, ServiceLayerException {
        //Cluster remoteNode = clusterNodes.get(0);
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        boolean toRet = true;
        // prepare a new folder for the cloned repository
        Path siteSandboxPath = buildRepoPath(SANDBOX);
        logger.debug("Add user credentials if provided");
        // then clone
        //logger.debug("Cloning from " + remoteNode.getRemoteUrl() + " to " + siteSandboxPath.toString());
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .setGitDir(siteSandboxPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();
        try (Git git = new Git(repo)) {
            List<RemoteConfig> remotes = git.remoteList().call();
            for (RemoteConfig remote : remotes) {
                FetchResult fetchResult = git.fetch()
                        .setRemote(remote.getName())
                        .setRefSpecs(remote.getFetchRefSpecs())
                        .setRemoveDeletedRefs(true)
                        .call();
            }

            for (Cluster remoteNode : clusterNodes) {
                PullCommand pullCommand = git.pull();
                logger.debug("Set remote " + remoteNode.getRemoteName());
                pullCommand.setRemote(remoteNode.getRemoteName());
                pullCommand.setStrategy(MergeStrategy.THEIRS);
                switch (remoteNode.getAuthenticationType()) {
                    case RemoteRepository.AuthenticationType.NONE:
                        logger.debug("No authentication");
                        pullCommand.call();
                        break;
                    case RemoteRepository.AuthenticationType.BASIC:
                        logger.debug("Basic authentication");
                        String hashedPassword = remoteNode.getRemotePassword();
                        String password = encryptor.decrypt(hashedPassword);
                        pullCommand.setCredentialsProvider(
                                new UsernamePasswordCredentialsProvider(remoteNode.getRemoteUsername(), password));

                        pullCommand.call();
                        break;
                    case RemoteRepository.AuthenticationType.TOKEN:
                        logger.debug("Token based authentication");
                        String hashedToken = remoteNode.getRemoteToken();
                        String token = encryptor.decrypt(hashedToken);
                        pullCommand.setCredentialsProvider(
                                new UsernamePasswordCredentialsProvider(token, StringUtils.EMPTY));
                        pullCommand.call();
                        break;
                    case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                        logger.debug("Private key authentication");
                        final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                        String hashedPrivateKey = remoteNode.getRemotePrivateKey();
                        String privateKey = encryptor.decrypt(hashedPrivateKey);
                        tempKey.toFile().deleteOnExit();
                        pullCommand.setTransportConfigCallback(new TransportConfigCallback() {
                            @Override
                            public void configure(Transport transport) {
                                SshTransport sshTransport = (SshTransport)transport;
                                sshTransport.setSshSessionFactory(getSshSessionFactory(privateKey, tempKey));
                            }
                        });
                        pullCommand.call();
                        Files.delete(tempKey);
                        break;
                    default:
                        throw new ServiceLayerException("Unsupported authentication type " +
                                remoteNode.getAuthenticationType());
                }
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

    }

    private SshSessionFactory getSshSessionFactory(String remotePrivateKey, final Path tempKey) {
        try {

            Files.write(tempKey, remotePrivateKey.getBytes());
            SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host hc, Session session) {
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(tempKey.toAbsolutePath().toString());
                    return defaultJSch;
                }
            };
            return sshSessionFactory;
        } catch (IOException e) {
            logger.error("Failed to create private key for SSH connection.", e);
        }
        return null;
    }

    private boolean checkIfSiteRepoExists() {
        return false;
    }


    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<Cluster> getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(List<Cluster> clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public PreviewDeployer getPreviewDeployer() {
        return previewDeployer;
    }

    public void setPreviewDeployer(PreviewDeployer previewDeployer) {
        this.previewDeployer = previewDeployer;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
