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

package org.craftercms.studio.api.v2.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

public class GitRepositoryHelper {

    private static final Logger logger = LoggerFactory.getLogger(GitRepositoryHelper.class);

    private static GitRepositoryHelper instance;

    private StudioConfiguration studioConfiguration;
    private TextEncryptor encryptor;

    private Map<String, Repository> sandboxes = new HashMap<>();
    private Map<String, Repository> published = new HashMap<>();
    private Repository globalRepo = null;

    private GitRepositoryHelper() { }

    public static GitRepositoryHelper getHelper(StudioConfiguration studioConfiguration) throws CryptoException {
        if (instance == null) {
            instance = new GitRepositoryHelper();
            instance.studioConfiguration = studioConfiguration;
            instance.encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                    studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        }
        return instance;
    }

    public Repository getRepository(String siteId, GitRepositories gitRepository) {
        Repository repo;

        logger.debug("getRepository invoked with site" + siteId + "Repository Type: " + gitRepository.toString());

        switch (gitRepository) {
            case SANDBOX:
                repo = sandboxes.get(siteId);
                if (repo == null) {
                    if (buildSiteRepo(siteId)) {
                        repo = sandboxes.get(siteId);
                    } else {
                        logger.warn("Couldn't get the sandbox repository for site: " + siteId);
                    }
                }
                break;
            case PUBLISHED:
                repo = published.get(siteId);
                if (repo == null) {
                    if (buildSiteRepo(siteId)) {
                        repo = published.get(siteId);
                    } else {
                        logger.warn("Couldn't get the published repository for site: " + siteId);
                    }
                }
                break;
            case GLOBAL:
                if (globalRepo == null) {
                    Path globalConfigRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);
                    try {
                        globalRepo = openRepository(globalConfigRepoPath);
                    } catch (IOException e) {
                        logger.error("Error getting the global repository.", e);
                    }
                }
                repo = globalRepo;
                break;
            default:
                repo = null;
                break;
        }

        if (repo != null) {
            logger.debug("success in getting the repository for site: " + siteId);
        } else {
            logger.debug("failure in getting the repository for site: " + siteId);
        }

        return repo;
    }

    public boolean buildSiteRepo(String siteId) {
        boolean toReturn = false;
        Repository sandboxRepo;
        Repository publishedRepo;

        Path siteSandboxRepoPath = buildRepoPath(GitRepositories.SANDBOX, siteId).resolve(GIT_ROOT);
        Path sitePublishedRepoPath = buildRepoPath(GitRepositories.PUBLISHED, siteId).resolve(GIT_ROOT);

        try {
            if (Files.exists(siteSandboxRepoPath)) {
                // Build and put in cache
                sandboxRepo = openRepository(siteSandboxRepoPath);
                sandboxes.put(siteId, sandboxRepo);
                toReturn = true;
            }
        } catch (IOException e) {
            logger.error("Failed to create sandbox repo for site: " + siteId + " using path " + siteSandboxRepoPath
                    .toString(), e);
        }

        try {
            if (toReturn && Files.exists(sitePublishedRepoPath)) {
                // Build and put in cache
                publishedRepo = openRepository(sitePublishedRepoPath);
                published.put(siteId, publishedRepo);

                toReturn = true;
            }
        } catch (IOException e) {
            logger.error("Failed to create published repo for site: " + siteId + " using path " +
                    sitePublishedRepoPath.toString(), e);
        }

        return toReturn;
    }

    /**
     * Builds repository path
     *
     * @param repoType repository type
     * @return repository path
     */
    public Path buildRepoPath(GitRepositories repoType) {
        return buildRepoPath(repoType, StringUtils.EMPTY);
    }

    /**
     * Builds repository path
     *
     * @param repoType repository type
     * @param siteId site Id (if empty it is global repository)
     * @return repository path
     */
    public Path buildRepoPath(GitRepositories repoType, String siteId) {
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
            case GLOBAL:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH));
                break;
            default:
                path = null;
                break;
        }

        return path;
    }

    /**
     * Opens a git repository
     *
     * @param repositoryPath path to repository to open (including .git)
     * @return repository object if successful
     * @throws IOException
     */
    public Repository openRepository(Path repositoryPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
                .setGitDir(repositoryPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();
        return repository;
    }

    public boolean isRemoteValid(Git git, String remote, String authenticationType,
                                  String remoteUsername, String remotePassword, String remoteToken,
                                  String remotePrivateKey)
            throws CryptoException, IOException, ServiceLayerException, GitAPIException {
        LsRemoteCommand lsRemoteCommand = git.lsRemote();
        lsRemoteCommand.setRemote(remote);
        final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        lsRemoteCommand = setAuthenticationForCommand(lsRemoteCommand, authenticationType, remoteUsername,
                remotePassword, remoteToken, remotePrivateKey, tempKey, false);
        lsRemoteCommand.call();
        Files.deleteIfExists(tempKey);
        return true;
    }

    public SshSessionFactory getSshSessionFactory(String privateKey, final Path tempKey)  {
        try {
            Files.write(tempKey, privateKey.getBytes());
            SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host hc, Session session) {
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = new JSch();
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

    public <T extends TransportCommand> T setAuthenticationForCommand(T gitCommand, String authenticationType,
                                                                      String username, String password, String token,
                                                                      String privateKey, Path tempKey, boolean decrypt)
            throws CryptoException, ServiceLayerException {
        String passwordValue = password;
        String tokenValue = token;
        String privateKeyValue = privateKey;
        if (decrypt) {
            if (!StringUtils.isEmpty(password)) {
                passwordValue = encryptor.decrypt(password);
            }
            if (!StringUtils.isEmpty(token)) {
                tokenValue = encryptor.decrypt(token);
            }
            if (!StringUtils.isEmpty(privateKey)) {
                privateKeyValue = encryptor.decrypt(privateKey);
            }
        }
        final String pk = privateKeyValue;
        switch (authenticationType) {
            case RemoteRepository.AuthenticationType.NONE:
                logger.debug("No authentication");
                break;
            case RemoteRepository.AuthenticationType.BASIC:
                logger.debug("Basic authentication");
                UsernamePasswordCredentialsProvider credentialsProviderUP =
                        new UsernamePasswordCredentialsProvider(username, passwordValue);
                gitCommand.setCredentialsProvider(credentialsProviderUP);
                break;
            case RemoteRepository.AuthenticationType.TOKEN:
                logger.debug("Token based authentication");
                UsernamePasswordCredentialsProvider credentialsProvider =
                        new UsernamePasswordCredentialsProvider(tokenValue, StringUtils.EMPTY);
                gitCommand.setCredentialsProvider(credentialsProvider);
                break;
            case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                logger.debug("Private key authentication");
                tempKey.toFile().deleteOnExit();
                gitCommand.setTransportConfigCallback(new TransportConfigCallback() {
                    @Override
                    public void configure(Transport transport) {
                        SshTransport sshTransport = (SshTransport)transport;
                        sshTransport.setSshSessionFactory(getSshSessionFactory(pk, tempKey));
                    }
                });

                break;
            default:
                throw new ServiceLayerException("Unsupported authentication type " + authenticationType);
        }

        return gitCommand;
    }
}
