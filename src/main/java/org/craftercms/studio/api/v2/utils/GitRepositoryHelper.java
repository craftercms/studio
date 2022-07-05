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

package org.craftercms.studio.api.v2.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.git.utils.AuthConfiguratorFactory;
import org.craftercms.commons.git.utils.AuthenticationType;
import org.craftercms.commons.git.utils.TypeBasedAuthConfiguratorBuilder;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.exception.git.cli.NoChangesToCommitException;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.repository.StrSubstitutorVisitor;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants;
import org.craftercms.studio.impl.v1.repository.git.TreeCopier;
import org.craftercms.studio.impl.v2.utils.GitUtils;
import org.craftercms.studio.impl.v2.utils.git.GitCli;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.RenameBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;

import java.util.*;
import java.util.concurrent.Callable;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.GitRepositories.GLOBAL;
import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.STRING_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.GLOBAL_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_PUBLISHED_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_POSTSCRIPT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_PROLOGUE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_CREATE_AS_ORPHAN_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_CREATE_REPOSITORY_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_CREATE_SANDBOX_BRANCH_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_IGNORE_FILES;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_PUBLISHING_BLACKLIST_REGEX;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_FILE_MODE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_FILE_MODE_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_CORE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;
import static org.eclipse.jgit.lib.Constants.HEAD;

// TODO: AV - Missing Javadoc and shouldn't be in the api root package. Those are only used mainly for interfaces
// TODO: AV - all methods in this class should throw exceptions instead of returning null or a boolean
// not implementations like this one
public class GitRepositoryHelper implements DisposableBean {

    public static final String CONFIG_KEY_RESOURCE = "resource";
    public static final String CONFIG_KEY_FOLDER = "folder";

    private static final Logger logger = LoggerFactory.getLogger(GitRepositoryHelper.class);

    private StudioConfiguration studioConfiguration;
    private TextEncryptor encryptor;
    private SecurityService securityService;
    private UserServiceInternal userServiceInternal;
    private GeneralLockService generalLockService;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    private AuthConfiguratorFactory authConfiguratorFactory;
    private GitCli gitCli;
    private boolean gitCliEnabled;

    private final Cache<String, Repository> repositoryCache = CacheBuilder.newBuilder().build();

    @Override
    public void destroy() throws Exception {
        repositoryCache.asMap().values().forEach(Repository::close);
        repositoryCache.invalidateAll();
        repositoryCache.cleanUp();
    }

    protected String getRepoCacheKey(String siteId, GitRepositories type) {
        return String.join(":", type.toString(), siteId);
    }

    public Repository getRepository(String siteId, GitRepositories gitRepository) {
        return getRepository(siteId, gitRepository, null);
    }

    public Repository getRepository(String siteId, GitRepositories repoType, String sandboxBranch) {
        String cacheKey = getRepoCacheKey(siteId, repoType);
        Repository repo = repositoryCache.getIfPresent(cacheKey);

        if (repo == null) {
            logger.debug("cache miss for site '{}', type '{}'", siteId, repoType);
            switch (repoType) {
                case SANDBOX:
                    if (buildSiteRepo(siteId)) {
                        repo = repositoryCache.getIfPresent(cacheKey);
                        if (StringUtils.isNotEmpty(sandboxBranch)) {
                            checkoutSandboxBranch(siteId, repo, sandboxBranch);
                        }
                    } else {
                        logger.warn("Couldn't get the sandbox repository for site: " + siteId);
                    }
                    break;
                case PUBLISHED:
                    if (buildSiteRepo(siteId)) {
                        repo = repositoryCache.getIfPresent(cacheKey);
                    } else {
                        logger.warn("Couldn't get the published repository for site: " + siteId);
                    }
                    break;
                case GLOBAL:
                    Path globalConfigRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);
                    try {
                        repo = openRepository(globalConfigRepoPath);
                    } catch (IOException e) {
                        logger.error("Error getting the global repository.", e);
                    }
                    break;
            }

            if (repo == null) {
                logger.debug("failure in getting the repository for site: " + siteId);
            }
        }

        return repo;
    }

    public boolean buildSiteRepo(String siteId) {
        boolean toReturn = true;
        Repository sandboxRepo;
        Repository publishedRepo;

        Path siteSandboxRepoPath = buildRepoPath(GitRepositories.SANDBOX, siteId).resolve(GIT_ROOT);
        Path sitePublishedRepoPath = buildRepoPath(GitRepositories.PUBLISHED, siteId).resolve(GIT_ROOT);

        try {
            if (Files.exists(siteSandboxRepoPath)) {
                // Build and put in cache
                sandboxRepo = repositoryCache.getIfPresent(getRepoCacheKey(siteId, SANDBOX));
                if (sandboxRepo == null) {
                    sandboxRepo = openRepository(siteSandboxRepoPath);
                    repositoryCache.put(getRepoCacheKey(siteId, SANDBOX), sandboxRepo);
                }
            }
        } catch (IOException e) {
            toReturn = false;
            logger.error("Failed to create sandbox repo for site: '{}' using path '{}'", siteId, siteSandboxRepoPath, e);
        }

        try {
            if (toReturn && Files.exists(sitePublishedRepoPath)) {
                // Build and put in cache
                publishedRepo = repositoryCache.getIfPresent(getRepoCacheKey(siteId, PUBLISHED));
                if (publishedRepo == null) {
                    publishedRepo = openRepository(sitePublishedRepoPath);
                    repositoryCache.put(getRepoCacheKey(siteId, PUBLISHED), publishedRepo);
                }
            }
        } catch (IOException e) {
            toReturn = false;
            logger.error("Failed to create published repo for site: '{}' using path '{}'", siteId,
                    sitePublishedRepoPath, e);
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
        return buildRepoPath(repoType, EMPTY);
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
     * @throws IOException IO error
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
        setAuthenticationForCommand(lsRemoteCommand, authenticationType, remoteUsername,
                remotePassword, remoteToken, remotePrivateKey, tempKey, false);
        retryingRepositoryOperationFacade.call(lsRemoteCommand);
        Files.deleteIfExists(tempKey);
        return true;
    }

    public void setAuthenticationForCommand(TransportCommand<?,?> gitCommand, String authenticationType,
                                            String username, String password, String token, String privateKey,
                                            Path tempKey, boolean decrypt)
            throws CryptoException, ServiceLayerException, IOException {
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
        TypeBasedAuthConfiguratorBuilder builder = authConfiguratorFactory.forType(authenticationType);
        switch (authenticationType) {
            case AuthenticationType.NONE:
                logger.debug("No authentication");
                break;
            case AuthenticationType.BASIC:
                logger.debug("Basic authentication");
                builder
                    .withUsername(username)
                    .withPassword(passwordValue);
                break;
            case AuthenticationType.TOKEN:
                logger.debug("Token based authentication");
                builder
                    .withUsername(tokenValue);
                break;
            case AuthenticationType.PRIVATE_KEY:
                logger.debug("Private key authentication");
                Files.writeString(tempKey, privateKeyValue);
                tempKey.toFile().deleteOnExit();
                builder
                    .withPrivateKeyPath(tempKey.toString());
                break;
            default:
                throw new ServiceLayerException("Unsupported authentication type " + authenticationType);
        }
        builder.build().configureAuthentication(gitCommand);
    }

    public String getGitPath(String path) {
        if (StringUtils.isEmpty(path)) {
            path = ".";
        } else {
            path = FilenameUtils.normalize(path, true);
            path = StringUtils.stripStart(path, FILE_SEPARATOR);
            path =  FilenameUtils.separatorsToUnix(path);
        }
        return path;
    }

    public String[] getGitPaths(String... paths) {
        if (ArrayUtils.isNotEmpty(paths)) {
            String[] gitPaths = new String[paths.length];
            for (int i = 0; i < paths.length; i++) {
                gitPaths[i] = getGitPath(paths[i]);
            }

            return gitPaths;
        } else {
            return new String[0];
        }
    }

    /**
     * Return the author identity as a jgit PersonIdent
     *
     * @param user author
     * @return author user as a PersonIdent
     */
    public PersonIdent getAuthorIdent(User user) {
        PersonIdent currentUserIdent =
                new PersonIdent(user.getFirstName() + " " + user.getLastName(), user.getEmail());

        return currentUserIdent;
    }

    public RevTree getTreeForCommit(Repository repository, String commitId) throws IOException {
        ObjectId commitObjectId = repository.resolve(commitId);

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitObjectId);

            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            return tree;
        }
    }

    public RevTree getTreeForLastCommit(Repository repository) throws IOException {
        ObjectId lastCommitId = repository.resolve(HEAD);

        // a RevWalk allows to walk over commits based on some filtering
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(lastCommitId);

            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            return tree;
        }
    }

    public List<String> getFilesInCommit(Repository repository, RevCommit commit) {
        List<String> files = new ArrayList<String>();
        RevWalk rw = new RevWalk(repository);
        try (Git git = new Git(repository)) {
            if (commit.getParentCount() > 0) {
                RevCommit parent = rw.parseCommit(commit.getParent(0).getId());

                ObjectId commitId = commit.getId();
                ObjectId parentCommitId = parent.getId();

                RevTree parentTree = getTreeForCommit(repository, parentCommitId.getName());
                RevTree commitTree = getTreeForCommit(repository, commitId.getName());

                if (parentTree != null && commitTree != null) {
                    try (ObjectReader reader = repository.newObjectReader()) {
                        CanonicalTreeParser prevCommitTreeParser = new CanonicalTreeParser();
                        CanonicalTreeParser nextCommitTreeParser = new CanonicalTreeParser();
                        prevCommitTreeParser.reset(reader, parentTree.getId());
                        nextCommitTreeParser.reset(reader, commitTree.getId());

                        // Diff the two commit Ids
                        DiffCommand diffCommand = git.diff()
                                .setOldTree(prevCommitTreeParser)
                                .setNewTree(nextCommitTreeParser);
                        List<DiffEntry> diffEntries = retryingRepositoryOperationFacade.call(diffCommand);
                        for (DiffEntry diffEntry : diffEntries) {
                            if (diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                                files.add(FILE_SEPARATOR + diffEntry.getOldPath());
                            } else {
                                files.add(FILE_SEPARATOR + diffEntry.getNewPath());
                            }
                        }
                    } catch (IOException | GitAPIException e) {
                        logger.error("Error while getting list of files in commit " + commit.getId().getName());
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Error while getting list of files in commit " + commit.getId().getName());
        } finally {
            rw.dispose();
        }
        return files;
    }

    /**
     * Create a site sandbox git repository from scratch
     * @param site site to create
     * @param sandboxBranch sandbox branch name
     * @return true if successful, false otherwise
     */
    public boolean createSandboxRepository(String site, String sandboxBranch) {
        boolean toReturn;
        Repository sandboxRepo = null;

        // Build a path for the site/sandbox
        Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, site);

        // Create Sandbox
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            sandboxRepo = createGitRepository(siteSandboxPath);

            toReturn = (sandboxRepo != null);

            if (toReturn) {
                toReturn = checkoutSandboxBranch(site, sandboxRepo, sandboxBranch);
                if (toReturn) {
                    repositoryCache.put(getRepoCacheKey(site, SANDBOX), sandboxRepo);
                }
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    /**
     * Create a site published git repository from scratch
     * @param siteId site to create
     * @param sandboxBranch sandbox branch name
     * @return true if successful, false otherwise
     */
    public boolean createPublishedRepository(String siteId, String sandboxBranch) {
        // Create Published by cloning Sandbox
        boolean toRet = false;
        // Build a path for the site/sandbox
        Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, siteId);
        // Built a path for the site/published
        Path sitePublishedPath = buildRepoPath(GitRepositories.PUBLISHED, siteId);
        String gitLockKey = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git publishedGit = Git.cloneRepository()
                .setURI(sitePublishedPath.relativize(siteSandboxPath).toString())
                .setDirectory(sitePublishedPath.normalize().toAbsolutePath().toFile())
                .call()) {
            Repository publishedRepo = publishedGit.getRepository();
            optimizeRepository(publishedRepo);
            checkoutSandboxBranch(siteId, publishedRepo, sandboxBranch);
            removePublishBlackList(publishedRepo);
            publishedRepo.close();
            publishedGit.close();
            toRet = true;
        } catch (GitAPIException | IOException e) {
            logger.error("Error adding origin (sandbox) to published repository", e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toRet;
    }

    public Repository createGitRepository(Path path) {
        Repository toReturn;
        path = Paths.get(path.toAbsolutePath().toString(), GIT_ROOT);
        try {
            toReturn = FileRepositoryBuilder.create(path.toFile());
            toReturn.create();

            optimizeRepository(toReturn);
            try (Git git = new Git(toReturn)) {
                CommitCommand commitCommand = git.commit()
                        .setAllowEmpty(true)
                        .setMessage(getCommitMessage(REPO_CREATE_REPOSITORY_COMMIT_MESSAGE));
                retryingRepositoryOperationFacade.call(commitCommand);
            } catch (GitAPIException e) {
                logger.error("Error while creating repository for site with path" + path, e);
                toReturn = null;
            }
        } catch (IOException e) {
            logger.error("Error while creating repository for site with path" + path, e);
            toReturn = null;
        }

        return toReturn;
    }

    private void optimizeRepository(Repository repo) throws IOException {
        // Get git configuration
        StoredConfig config = repo.getConfig();
        // Set compression level (core.compression)
        config.setInt(CONFIG_SECTION_CORE, null, CONFIG_PARAMETER_COMPRESSION,
                CONFIG_PARAMETER_COMPRESSION_DEFAULT);
        // Set big file threshold (core.bigFileThreshold)
        config.setString(CONFIG_SECTION_CORE, null, CONFIG_PARAMETER_BIG_FILE_THRESHOLD,
                CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT);
        // Set fileMode
        config.setBoolean(CONFIG_SECTION_CORE, null, CONFIG_PARAMETER_FILE_MODE,
                CONFIG_PARAMETER_FILE_MODE_DEFAULT);
        // Save configuration changes
        config.save();

        if (gitCliEnabled) {
            // The first git commit of a new repository takes a long time with Git CLI. A git status first seems
            // to fix the issue
            gitCli.isRepoClean(repo.getWorkTree().getAbsolutePath());
        }
    }

    public String getCommitMessage(String commitMessageKey) {
        String prologue = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_PROLOGUE);
        String postscript = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_POSTSCRIPT);
        String message = studioConfiguration.getProperty(commitMessageKey);

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(prologue)) {
            sb.append(prologue).append("\n\n");
        }
        sb.append(message);
        if (StringUtils.isNotEmpty(postscript)) {
            sb.append("\n\n").append(postscript);
        }
        return sb.toString();
    }

    private boolean checkoutSandboxBranch(String site, Repository sandboxRepo, String sandboxBranch) {
        String sandboxBranchName = sandboxBranch;
        if (StringUtils.isEmpty(sandboxBranchName)) {
            sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
        }
        try (Git git = new Git(sandboxRepo)) {
            if (!StringUtils.equals(sandboxRepo.getBranch(), sandboxBranchName)) {
                ListBranchCommand listBranchCommand = git.branchList();
                List<Ref> branchList = retryingRepositoryOperationFacade.call(listBranchCommand);
                boolean createBranch = true;
                for (Ref branch : branchList) {
                    if (StringUtils.equals(branch.getName(), sandboxBranchName) ||
                            StringUtils.equals(branch.getName(), Constants.R_HEADS + sandboxBranchName)) {
                        createBranch = false;
                        break;
                    }
                }
                if (sandboxRepo.isBare() || sandboxRepo.resolve(Constants.HEAD) == null) {
                    CommitCommand commitCommand = git.commit()
                            .setAllowEmpty(true)
                            .setMessage(getCommitMessage(REPO_CREATE_SANDBOX_BRANCH_COMMIT_MESSAGE)
                                    .replaceAll(PATTERN_SANDBOX, sandboxBranchName));
                    retryingRepositoryOperationFacade.call(commitCommand);
                }
                CheckoutCommand checkoutCommand = git.checkout()
                        .setCreateBranch(createBranch)
                        .setName(sandboxBranchName)
                        .setForceRefUpdate(true);
                retryingRepositoryOperationFacade.call(checkoutCommand);
            }
            return true;
        } catch (GitAPIException | IOException e) {
            logger.error("Error checking out sandbox branch " + sandboxBranchName + " for site " + site, e);
            return false;
        }
    }

    private boolean removePublishBlackList(Repository publishedRepo) {
        String blacklistConfig = studioConfiguration.getProperty(CONFIGURATION_PUBLISHING_BLACKLIST_REGEX);
        if (StringUtils.isEmpty(blacklistConfig)) {
            return true;
        }

        List<String> patterns = Arrays.asList(StringUtils.split(blacklistConfig, STRING_SEPARATOR));
        try (Git git = new Git(publishedRepo)) {
            String rootPath = publishedRepo.getWorkTree().getPath();
            RmCommand rmCommand = git.rm();
            Files.walkFileTree(Paths.get(rootPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String sitePath = file.toAbsolutePath().toString().replaceFirst(rootPath, FILE_SEPARATOR);
                    boolean isMatched = ContentUtils.matchesPatterns(sitePath, patterns);
                    if (isMatched) {
                        String gitPath = getGitPath(sitePath);
                        rmCommand.addFilepattern(gitPath);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            retryingRepositoryOperationFacade.call(rmCommand);
            return true;
        } catch (GitAPIException | IOException e) {
            logger.error("Error while removing publishing blacklist pattern", e);
            return false;
        }
    }

    public boolean copyContentFromBlueprint(String blueprintLocation, String site) {
        boolean toReturn = true;

        // Build a path to the Sandbox repo we'll be copying to
        Path siteRepoPath = buildRepoPath(GitRepositories.SANDBOX, site);
        // Build a path to the blueprint
        Path blueprintPath = Paths.get(blueprintLocation);
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        // Let's copy!
        TreeCopier tc = new TreeCopier(blueprintPath, siteRepoPath);
        try {
            Files.walkFileTree(blueprintPath, opts, Integer.MAX_VALUE, tc);
        } catch (IOException err) {
            logger.error("Error copping files from blueprint", err);
            toReturn = false;
        }

        return toReturn;
    }

    public boolean updateSiteNameConfigVar(String site) {
        boolean toReturn = true;
        if (!replaceSiteNameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME)))) {
            toReturn = false;
        } else if (!replaceSiteNameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME)))) {
            toReturn = false;
        } else if (!replaceSiteNameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME)))) {
            toReturn = false;
        }
        return toReturn;
    }

    protected boolean replaceSiteNameVariable(String site, Path path) {
        boolean toReturn = false;
        Charset charset = StandardCharsets.UTF_8;
        String content = null;
        try {
            content = Files.readString(path, charset);
            content = content.replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, site);
            Files.write(path, content.getBytes(charset));
            toReturn = true;
        } catch (IOException e) {
            logger.error("Error replacing sitename variable inside configuration file " + path +
                    " for site " + site);
            toReturn = false;
        }
        return toReturn;
    }

    public boolean replaceParameters(String siteId, Map<String, String> parameters) {
        if (MapUtils.isEmpty(parameters)) {
            logger.debug("Skipping parameter replacement for site '{}'", siteId);
            return true;
        }
        String configRootPath = FilenameUtils.getPath(
                studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH));
        Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, siteId);
        Path configFolder = siteSandboxPath.resolve(configRootPath);
        try {
            Files.walkFileTree(configFolder, new StrSubstitutorVisitor(parameters));
            return true;
        } catch (IOException e) {
            logger.error("Error looking for configuration files for site '{}'", siteId, e);
            return false;
        }
    }

    public boolean addGitIgnoreFiles(String siteId) {
        List<HierarchicalConfiguration<ImmutableNode>> ignores = studioConfiguration.getSubConfigs(REPO_IGNORE_FILES);
        if (CollectionUtils.isEmpty(ignores)) {
            logger.debug("No ignore files will be added to site '{}'", siteId);
            return true;
        }

        logger.debug("Adding ignore files for site '{}'", siteId);
        Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, siteId);

        for (HierarchicalConfiguration<ImmutableNode> ignore : ignores) {
            String ignoreLocation = ignore.getString(CONFIG_KEY_RESOURCE);
            Resource ignoreFile = new ClassPathResource(ignoreLocation);
            if (!ignoreFile.exists()) {
                logger.warn("Couldn't find ignore file at '{}'", ignoreLocation);
                continue;
            }

            String repoFolder = ignore.getString(CONFIG_KEY_FOLDER);
            Path actualFolder = StringUtils.isEmpty(repoFolder)? siteSandboxPath : siteSandboxPath.resolve(repoFolder);
            if (!Files.exists(actualFolder)) {
                logger.debug("Repository doesn't contain a '{}' folder for site '{}'", repoFolder, siteId);
                continue;
            }

            Path actualFile = actualFolder.resolve(GitContentRepositoryConstants.IGNORE_FILE);
            if (!Files.exists(actualFile)) {
                logger.debug("Adding ignore file at '{}' for site '{}'", repoFolder, siteId);
                try (InputStream in = ignoreFile.getInputStream()) {
                    Files.copy(in, actualFile);
                } catch (IOException e) {
                    logger.error("Error writing ignore file at '{}' for site '{}'", e, repoFolder, siteId);
                    return false;
                }
            } else {
                logger.debug("Repository already contains an ignore file at '{}' for site '{}'", actualFolder, siteId);
            }
        }

        return true;
    }

    /**
     * Perform an initial commit after large changes to a site. Will not work against the global config repo.
     * @param site site identifier
     * @param message commit message
     * @param sandboxBranch sandbox branch name
     * @param creator site creator
     * @return true if successful, false otherwise
     */
    public boolean performInitialCommit(String site, String message, String sandboxBranch, String creator) {
        boolean toReturn = true;

        Repository repo = getRepository(site, GitRepositories.SANDBOX, sandboxBranch);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            StatusCommand statusCommand = git.status();
            Status status = retryingRepositoryOperationFacade.call(statusCommand);

            if (status.hasUncommittedChanges() || !status.isClean()) {
                AddCommand addCommand = git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS);
                retryingRepositoryOperationFacade.call(addCommand);
                CommitCommand commitCommand = git.commit()
                        .setMessage(message);
                User user = userServiceInternal.getUserByIdOrUsername(-1, creator);
                if (Objects.nonNull(user)) {
                    commitCommand = commitCommand.setAuthor(getAuthorIdent(user));
                }
                retryingRepositoryOperationFacade.call(commitCommand);
            }
            checkoutSandboxBranch(site, repo, sandboxBranch);
        } catch (GitAPIException | UserNotFoundException | ServiceLayerException err) {
            logger.error("error creating initial commit for site:  " + site, err);
            toReturn = false;
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    public boolean createSiteCloneRemoteGitRepo(String siteId, String sandboxBranch, String remoteName,
                                                String remoteUrl, String remoteBranch, boolean singleBranch,
                                                String authenticationType, String remoteUsername, String remotePassword,
                                                String remoteToken, String remotePrivateKey, boolean createAsOrphan,
                                                String creator)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException {

        boolean toRet = true;
        // prepare a new folder for the cloned repository
        Path siteSandboxPath = buildRepoPath(SANDBOX, siteId);
        File localPath = siteSandboxPath.toFile();
        localPath.delete();
        logger.debug("Add user credentials if provided");
        // then clone
        logger.debug("Cloning from " + remoteUrl + " to " + localPath);
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(localPath)
                .setRemote(remoteName)
                .setCloneAllBranches(!singleBranch);
        if (StringUtils.isNotEmpty(remoteBranch)) {
            cloneCommand.setBranch(remoteBranch);
        }
        Git cloneResult = null;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try {
            Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try {
                setAuthenticationForCommand(cloneCommand, authenticationType, remoteUsername, remotePassword,
                        remoteToken, remotePrivateKey, tempKey, false);

                cloneResult = retryingRepositoryOperationFacade.call(cloneCommand);

                Repository sandboxRepo = checkIfCloneWasOk(cloneResult, remoteName, remoteUrl);

                optimizeRepository(sandboxRepo);

                // Make repository orphan if needed
                if (createAsOrphan) {
                    makeRepoOrphan(sandboxRepo, siteId, creator);
                }

                repositoryCache.put(getRepoCacheKey(siteId, SANDBOX), sandboxRepo);
            } catch (InvalidRemoteException e) {
                logger.error("Invalid remote repository: " + remoteName + " (" + remoteUrl + ")", e);
                throw new InvalidRemoteRepositoryException("Invalid remote repository: " + remoteName + " (" +
                        remoteUrl + ")");
            } catch (TransportException e) {
                GitUtils.translateException(e, logger, remoteName, remoteUrl, remoteUsername);
            } finally {
                Files.deleteIfExists(tempKey);
            }
        } catch (GitAPIException | IOException | UserNotFoundException | CryptoException e) {
            logger.error("Error while creating repository for site with path" + siteSandboxPath, e);
            toRet = false;
        } finally {
            generalLockService.unlock(gitLockKey);
            if (cloneResult != null) {
                cloneResult.close();
            }
        }
        return toRet;
    }

    /**
     * Checks if the clone was executed ok (mostly check for null references and
     * if the clone folder was created as a folder and current user has RW permissions.
     * <b> Never returns null</b>
     * @param cloneResult Clone Result to check.
     * @param remoteName Name of the remote we clone.
     * @param remoteUrl Clone URL
     * @return A {@link Repository} if all checks pass , never returns null.
     * @throws InvalidRemoteRepositoryException If a check does not pass.
     */
    private Repository checkIfCloneWasOk(Git cloneResult, String remoteName, String remoteUrl)
            throws InvalidRemoteRepositoryException {
        // Check if cloneResult is null , if so die.
        if (cloneResult == null) {
            String msg = "Remote Clone Error:: " + remoteName + " (" + remoteUrl + ")  cloneResult object null";
            logger.error(msg);
            throw new InvalidRemoteRepositoryException(msg);
        }
        Repository repository = cloneResult.getRepository();
        // Check if cloneResult is null , if so die.
        if (repository==null){
            String msg = "Remote Clone Error:: " + remoteName + " (" + remoteUrl + ")  sandboxRepo object null";
            logger.error(msg);
            throw new InvalidRemoteRepositoryException(msg);
        }
        File repoDir = repository.getDirectory();
        // Check if  sandbox repo,: exists, is a dir, we can RW to it.
        if (!repoDir.exists() ||  !repoDir.isDirectory() || !repoDir.canRead() || !repoDir.canWrite() ){
            String msg = "Remote Clone Error::  " + repository.getDirectory() + " doesn't exist, is not a dir or user" +
                    " don't have RW permissions";
            logger.error(msg);
            throw new InvalidRemoteRepositoryException(msg);
        }
        return repository;
    }

    private void makeRepoOrphan(Repository repository, String site, String creator) throws IOException, GitAPIException,
            ServiceLayerException, UserNotFoundException {
        logger.debug("Make repository orphan fir site " + site);
        String sandboxBranchName = repository.getBranch();
        if (StringUtils.isEmpty(sandboxBranchName)) {
            sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
        }
        String sandboxBranchOrphanName = sandboxBranchName + "_orphan";

        logger.debug("Shallow clone is not implemented in JGit. Instead we are creating new orphan branch after " +
                "cloning and renaming it to sandbox branch to replace fully cloned branch");
        try (Git git = new Git(repository)) {
            logger.debug("Create temporary orphan branch " + sandboxBranchOrphanName);
            CheckoutCommand checkoutCommand = git.checkout()
                    .setName(sandboxBranchOrphanName)
                    .setStartPoint(sandboxBranchName)
                    .setOrphan(true);
            retryingRepositoryOperationFacade.call(checkoutCommand);

            // Reset everything to simulate first commit as created empty repo
            logger.debug("Soft reset to commit empty repo");
            ResetCommand resetCommand = git.reset();
            retryingRepositoryOperationFacade.call(resetCommand);

            logger.debug("Commit empty repo, because we need to have HEAD to delete old and rename new branch");
            CommitCommand commitCommand = git.commit()
                    .setMessage(getCommitMessage(REPO_CREATE_AS_ORPHAN_COMMIT_MESSAGE));
            User user = userServiceInternal.getUserByIdOrUsername(-1, creator);
            if (Objects.nonNull(user)) {
                commitCommand = commitCommand.setAuthor(getAuthorIdent(user));
            }
            retryingRepositoryOperationFacade.call(commitCommand);

            logger.debug("Delete cloned branch " + sandboxBranchName);
            DeleteBranchCommand deleteBranchCommand =
                    git.branchDelete().setBranchNames(sandboxBranchName).setForce(true);
            retryingRepositoryOperationFacade.call(deleteBranchCommand);

            logger.debug("Rename temporary orphan branch to sandbox branch");
            RenameBranchCommand renameBranchCommand =
                    git.branchRename().setNewName(sandboxBranchName).setOldName(sandboxBranchOrphanName);
            retryingRepositoryOperationFacade.call(renameBranchCommand);
        }
    }

    public void removeSandbox(String siteId) {
        String cacheKey = getRepoCacheKey(siteId, SANDBOX);
        Repository repo = repositoryCache.getIfPresent(cacheKey);
        if (repo != null) {
            repositoryCache.invalidate(cacheKey);
            repo.close();
        }
    }

    // --------------------------------------------------------------------------------------------
    // TODO: Refactor this because it is just copy from V1
    /**
     * Build the global repository as part of system startup and caches it
     * @return true if successful, false otherwise
     * @throws IOException IO error
     */
    // TODO: SJ: This should be redesigned to return the repository instead of setting it as a "side effect"
    public boolean buildGlobalRepo() throws IOException {
        boolean toReturn = false;
        Path siteRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);

        if (Files.exists(siteRepoPath)) {
            Repository repo = openRepository(siteRepoPath);
            repositoryCache.put(getRepoCacheKey(EMPTY, GLOBAL), repo);
            toReturn = true;
        }

        return toReturn;
    }

    public boolean createGlobalRepo() {
        boolean toReturn = false;
        Path globalConfigRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);
        String gitLockKey = GLOBAL_REPOSITORY_GIT_LOCK;
        generalLockService.lock(gitLockKey);
        try {
            if (!Files.exists(globalConfigRepoPath)) {
                // Git repository doesn't exist for global, but the folder might be present, let's delete if exists
                Path globalConfigPath = globalConfigRepoPath.getParent();

                // Create the global repository folder
                try {
                    Files.deleteIfExists(globalConfigPath);
                    logger.info("Bootstrapping repository...");
                    Files.createDirectories(globalConfigPath);
                    Repository repo = createGitRepository(globalConfigPath);
                    repositoryCache.put(getRepoCacheKey(EMPTY, GLOBAL), repo);
                    toReturn = true;
                } catch (IOException e) {
                    // Something very wrong has happened
                    logger.error("Bootstrapping repository failed", e);
                }
            } else {
                logger.info("Detected existing global repository, will not create new one.");
                // unlock if global repository is locked
                String path = globalConfigRepoPath.getParent().toAbsolutePath().toString();
                if (GitUtils.isRepositoryLocked(path)) {
                    try {
                        GitUtils.unlock(path);
                    } catch (IOException e) {
                        logger.warn("Error unlocking git repository '{}'", path, e);
                    }
                }
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    public boolean deleteSiteGitRepo(String site) {
        boolean toReturn;

        // Get the Sandbox Path
        Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, site);
        // Get parent of that (since every site has two repos: Sandbox and Published)
        Path sitePath = siteSandboxPath.getParent();
        // Get a file handle to the parent and delete it
        File siteFolder = sitePath.toFile();

        String gitLockKeySandbox = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        String gitLockKeyPublished = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKeySandbox);
        generalLockService.lock(gitLockKeyPublished);
        try {
            String sandboxCacheKey = getRepoCacheKey(site, SANDBOX);
            Repository sandboxRepo = repositoryCache.getIfPresent(sandboxCacheKey);
            if (sandboxRepo != null) {
                repositoryCache.invalidate(sandboxCacheKey);
                sandboxRepo.close();
            }
            String publishedCacheKey = getRepoCacheKey(site, PUBLISHED);
            Repository publishedRepo = repositoryCache.getIfPresent(publishedCacheKey);
            if (publishedRepo != null) {
                repositoryCache.invalidate(publishedCacheKey);
                publishedRepo.close();
            }
            FileUtils.deleteDirectory(siteFolder);

            toReturn = true;

            logger.debug("Deleted site: " + site + " at path: " + sitePath);
        } catch (IOException e) {
            logger.error("Failed to delete site: " + site + " at path: " + sitePath + " exception " +
                    e);
            toReturn = false;
        } finally {
            generalLockService.unlock(gitLockKeyPublished);
            generalLockService.unlock(gitLockKeySandbox);
        }

        return toReturn;
    }

    public boolean writeFile(Repository repo, String site, String path, InputStream content) {
        boolean result = true;

        try {
            // Create basic file
            File file = new File(repo.getDirectory().getParent(), path);

            // Create parent folders
            File folder = file.getParentFile();
            if (folder != null) {
                if (!folder.exists()) {
                    folder.mkdirs();
                }
            }

            // Create the file if it doesn't exist already
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        logger.error("error creating file: site: " + site + " path: " + path);
                        result = false;
                    }
                } catch (IOException e) {
                    logger.error("error creating file: site: " + site + " path: " + path, e);
                    result = false;
                }
            }

            if (result) {
                logger.debug("Writing file: site: '{}', path: '{}'", site, path);

                // Write the bits
                try (FileChannel outChannel = new FileOutputStream(file.getPath()).getChannel()) {
                    logger.debug("created the file output channel");
                    ReadableByteChannel inChannel = Channels.newChannel(content);
                    logger.debug("created the file input channel");
                    long amount = 1024 * 1024; // 1MB at a time
                    long count;
                    long offset = 0;
                    while ((count = outChannel.transferFrom(inChannel, offset, amount)) > 0) {
                        logger.debug("writing the bits: offset = " + offset + " count: " + count);
                        offset += count;
                    }
                }

                result = addFiles(repo, site, path);
            }
        } catch (IOException e) {
            logger.error("error writing file: site: " + site + ", path: " + path, e);
            result = false;
        }

        return result;
    }

    public boolean addFiles(Repository repo, String site, String... paths) {
        boolean result = false;

        if (ArrayUtils.isNotEmpty(paths)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding files: site: '{}', path: '{}', gitCliEnabled: '{}'", site,
                             ArrayUtils.toString(paths), gitCliEnabled);
            }

            String gitLockKey = getSandboxRepoLockKey(site);
            generalLockService.lock(gitLockKey);
            try {
                if (gitCliEnabled) {
                    retryingRepositoryOperationFacade.call((Callable<Void>) () -> {
                        gitCli.add(repo.getWorkTree().getAbsolutePath(), getGitPaths(paths));
                        return null;
                    });
                } else {
                    try (Git git = new Git(repo)) {
                        AddCommand addCommand = git.add();
                        Arrays.stream(paths).forEach(p -> addCommand.addFilepattern(getGitPath(p)));
                        retryingRepositoryOperationFacade.call(addCommand);
                    }
                }

                result = true;
            } catch (Exception e) {
                logger.error("error adding files to git: site: '{}', paths: '{}'", site,
                             ArrayUtils.toString(paths), e);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }

        return result;
    }

    public String commitFiles(Repository repo, String site, String comment, PersonIdent user, String... paths) {
        String commitId = null;

        if (ArrayUtils.isNotEmpty(paths)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding files: site: '{}', path: '{}', gitCliEnabled: '{}'", site,
                             ArrayUtils.toString(paths), gitCliEnabled);
            }

            String gitLockKey = getSandboxRepoLockKey(site);
            generalLockService.lock(gitLockKey);
            try {
                if (gitCliEnabled) {
                    String author = user.getName() + " <" + user.getEmailAddress() + ">";

                    commitId = retryingRepositoryOperationFacade.call(
                            () -> gitCli.commit(repo.getWorkTree().getAbsolutePath(),
                                                author, comment, getGitPaths(paths)));
                } else {
                    try (Git git = new Git(repo)) {
                        CommitCommand commitCommand = git.commit()
                                                         .setAuthor(user)
                                                         .setCommitter(user)
                                                         .setMessage(comment);
                        Arrays.stream(paths).forEach(p -> commitCommand.setOnly(getGitPath(p)));
                        RevCommit commit = retryingRepositoryOperationFacade.call(commitCommand);
                        commitId = commit.getName();
                    }
                }
            } catch (Exception e) {
                Throwable cause = ExceptionUtils.getRootCause(e);
                if (cause instanceof NoChangesToCommitException ||
                    (cause instanceof JGitInternalException && "no changes".equalsIgnoreCase(cause.getMessage()))) {
                    // we should ignore empty commit errors
                    logger.debug("No changes were committed to git: site: '{}', paths: '{}'", site,
                                 ArrayUtils.toString(paths));
                } else {
                    logger.error("error adding files to git: site: '{}', paths: '{}'", site,
                                 ArrayUtils.toString(paths), e);
                }
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }

        return commitId;
    }

    /**
     * Return the current user identity as a jgit PersonIdent
     *
     * @return current user as a PersonIdent
     *
     * @throws ServiceLayerException general service error
     * @throws UserNotFoundException user not found
     */
    public PersonIdent getCurrentUserIdent() throws ServiceLayerException, UserNotFoundException {
        String userName = securityService.getCurrentUser();
        return getAuthorIdent(userName);
    }

    /**
     * Return the author identity as a jgit PersonIdent
     *
     * @param author author
     * @return author user as a PersonIdent
     *
     * @throws ServiceLayerException general service error
     * @throws UserNotFoundException user not found error
     */
    public PersonIdent getAuthorIdent(String author) throws ServiceLayerException, UserNotFoundException {
        User user = userServiceInternal.getUserByIdOrUsername(-1, author);
        PersonIdent currentUserIdent =
                new PersonIdent(user.getFirstName() + " " + user.getLastName(), user.getEmail());

        return currentUserIdent;
    }

    // TODO: AV - we should use this methods everywhere

    /**
     * Returns the key to use when locking Git operations for a site's sandbox repo
     *
     * @param site the site name
     * @return the lock key to use with the lock service
     */
    public String getSandboxRepoLockKey(String site) {
        return SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
    }

    /**
     * Returns the key to use when locking Git operations for a site's sandbox or global repo
     *
     * @param site the site name
     * @param ifSiteEmptyUseGlobalRepoLockKey `true` to use global repo lock key if site empty
     * @return the lock key to use with the lock service
     */
    public String getSandboxRepoLockKey(String site, boolean ifSiteEmptyUseGlobalRepoLockKey) {
        if (ifSiteEmptyUseGlobalRepoLockKey && StringUtils.isEmpty(site)) {
            return GLOBAL_REPOSITORY_GIT_LOCK;
        }
        return getSandboxRepoLockKey(site);
    }

    /**
     * Returns the key to use when locking Git operations for a site's published repo
     *
     * @param site the site name
     * @return the lock key to use with the lock service
     */
    public String getPublishedRepoLockKey(String site) {
        return SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public TextEncryptor getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public RetryingRepositoryOperationFacade getRetryingRepositoryOperationFacade() {
        return retryingRepositoryOperationFacade;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public void setAuthConfiguratorFactory(AuthConfiguratorFactory authConfiguratorFactory) {
        this.authConfiguratorFactory = authConfiguratorFactory;
    }

    public void setGitCli(GitCli gitCli) {
        this.gitCli = gitCli;
    }

    public void setGitCliEnabled(boolean gitCliEnabled) {
        this.gitCliEnabled = gitCliEnabled;
    }

}
