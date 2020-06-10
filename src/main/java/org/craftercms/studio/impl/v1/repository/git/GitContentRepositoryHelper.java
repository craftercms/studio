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

package org.craftercms.studio.impl.v1.repository.git;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_POSTSCRIPT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_PROLOGUE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_CREATE_REPOSITORY_COMMIT_MESSAGE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_FILE_MODE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_FILE_MODE_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_CORE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

/**
 * Created by Sumer Jabri
 */
public class GitContentRepositoryHelper {
    private static final Logger logger = LoggerFactory.getLogger(GitContentRepositoryHelper.class);

    protected Map<String, Repository> sandboxes = new HashMap<>();
    protected Map<String, Repository> published = new HashMap<>();

    protected Repository globalRepo = null;

    protected StudioConfiguration studioConfiguration;
    protected ServicesConfig servicesConfig;
    protected UserServiceInternal userServiceInternal;
    protected SecurityService securityService;

    public GitContentRepositoryHelper(StudioConfiguration studioConfiguration, ServicesConfig servicesConfig,
                                      UserServiceInternal userServiceInternal, SecurityService securityService) {
        this.studioConfiguration = studioConfiguration;
        this.servicesConfig = servicesConfig;
        this.userServiceInternal = userServiceInternal;
        this.securityService = securityService;
    }

    /**
     * Build the global repository as part of system startup and caches it
     * @return true if successful, false otherwise
     * @throws IOException
     */
    // TODO: SJ: This should be redesigned to return the repository instead of setting it as a "side effect"
    public boolean buildGlobalRepo() throws IOException {
        boolean toReturn = false;
        Path siteRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);

        if (Files.exists(siteRepoPath)) {
            globalRepo = openRepository(siteRepoPath);
            toReturn = true;
        }

        return toReturn;
    }

    /**
     * Builds a site's repository objects and caches them (Sandbox and Published)
     * @param site path to repository
     * @return true if successful, false otherwise
     */
    public boolean buildSiteRepo(String site) {
        boolean toReturn = false;
        Repository sandboxRepo;
        Repository publishedRepo;

        Path siteSandboxRepoPath = buildRepoPath(GitRepositories.SANDBOX, site).resolve(GIT_ROOT);
        Path sitePublishedRepoPath = buildRepoPath(GitRepositories.PUBLISHED, site).resolve(GIT_ROOT);

        try {
            if (Files.exists(siteSandboxRepoPath)) {
                // Build and put in cache
                sandboxRepo = openRepository(siteSandboxRepoPath);
                sandboxes.put(site, sandboxRepo);
                toReturn = true;
            }
        } catch (IOException e) {
            logger.error("Failed to create sandbox repo for site: " + site + " using path " + siteSandboxRepoPath
                .toString(), e);
        }

        try {
            if (toReturn && Files.exists(sitePublishedRepoPath)) {
                // Build and put in cache
                publishedRepo = openRepository(sitePublishedRepoPath);
                published.put(site, publishedRepo);

                toReturn = true;
            }
        } catch (IOException e) {
            logger.error("Failed to create published repo for site: " + site + " using path " +
                    sitePublishedRepoPath.toString(), e);
        }

        return toReturn;
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

    public String getGitPath(String path) {
        Path gitPath = Paths.get(path);
        gitPath = gitPath.normalize();
        try {
            gitPath = Paths.get(FILE_SEPARATOR).relativize(gitPath);
        } catch (IllegalArgumentException e) {
            logger.debug("Path: " + path + " is already relative path.");
        }
        if (StringUtils.isEmpty(gitPath.toString())) {
            return ".";
        }
        String toRet = gitPath.toString();
        toRet = FilenameUtils.separatorsToUnix(toRet);
        return toRet;
    }

    public Repository createGitRepository(Path path) {
        Repository toReturn;
        path = Paths.get(path.toAbsolutePath().toString(), GIT_ROOT);
        try {
            toReturn = FileRepositoryBuilder.create(path.toFile());
            toReturn.create();

            toReturn = optimizeRepository(toReturn);
            try (Git git = new Git(toReturn)) {
                git.commit()
                        .setAllowEmpty(true)
                        .setMessage(getCommitMessage(REPO_CREATE_REPOSITORY_COMMIT_MESSAGE))
                        .call();
            } catch (GitAPIException e) {
                logger.error("Error while creating repository for site with path" + path.toString(), e);
                toReturn = null;
            }
        } catch (IOException e) {
            logger.error("Error while creating repository for site with path" + path.toString(), e);
            toReturn = null;
        }

        return toReturn;
    }

    private Repository optimizeRepository(Repository repo) throws IOException {
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

        return repo;
    }

    public Path buildRepoPath(GitRepositories repoType) {
        return buildRepoPath(repoType, StringUtils.EMPTY);
    }

    public Path buildRepoPath(GitRepositories repoType, String site) {
        Path path;
        switch (repoType) {
            case SANDBOX:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                        studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH));
                break;
            case PUBLISHED:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                        studioConfiguration.getProperty(StudioConfiguration.PUBLISHED_PATH));
                break;
            case GLOBAL:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH));
                break;
            default:
                path = null;
        }

        return path;
    }

    public boolean createGlobalRepo() {
        boolean toReturn = false;
        Path globalConfigRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);

        if (!Files.exists(globalConfigRepoPath)) {
            // Git repository doesn't exist for global, but the folder might be present, let's delete if exists
            Path globalConfigPath = globalConfigRepoPath.getParent();

            // Create the global repository folder
            try {
                Files.deleteIfExists(globalConfigPath);
                logger.info("Bootstrapping repository...");
                Files.createDirectories(globalConfigPath);
                globalRepo = createGitRepository(globalConfigPath);
                toReturn = true;
            } catch (IOException e) {
                // Something very wrong has happened
                logger.error("Bootstrapping repository failed", e);
            }
        } else {
            logger.info("Detected existing global repository, will not create new one.");
            toReturn = false;
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

        try {
            Repository sboxRepo = sandboxes.get(site);
            if (sboxRepo != null) {
                sboxRepo.close();
                sandboxes.remove(site);
                RepositoryCache.close(sboxRepo);
                sboxRepo = null;
            }
            Repository pubRepo = published.get(site);
            if (pubRepo != null) {
                pubRepo.close();
                published.remove(site);
                RepositoryCache.close(pubRepo);
                pubRepo = null;
            }
            FileUtils.deleteDirectory(siteFolder);

            toReturn = true;

            logger.debug("Deleted site: " + site + " at path: " + sitePath);
        } catch (IOException e) {
            logger.error("Failed to delete site: " + site + " at path: " + sitePath + " exception " +
                    e.toString());
            toReturn = false;
        }

        return toReturn;
    }

    // SJ: Helper methods

    public Repository getRepository(String site, GitRepositories gitRepository) {
        Repository repo;

        logger.debug("getRepository invoked with site" + site + "Repository Type: " + gitRepository.toString());

        switch (gitRepository) {
            case SANDBOX:
                repo = sandboxes.get(site);
                if (repo == null) {
                    if (buildSiteRepo(site)) {
                        repo = sandboxes.get(site);
                    } else {
                        logger.warn("Couldn't get the sandbox repository for site: " + site);
                    }
                }
                break;
            case PUBLISHED:
                repo = published.get(site);
                if (repo == null) {
                    if (buildSiteRepo(site)) {
                        repo = published.get(site);
                    } else {
                        logger.warn("Couldn't get the published repository for site: " + site);
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
        }

        if (repo != null) {
            logger.debug("success in getting the repository for site: " + site);
        } else {
            logger.debug("failure in getting the repository for site: " + site);
        }

        return repo;
    }

    // TODO: SJ: Fix the exception handling in this method
    public RevTree getTreeForLastCommit(Repository repository) throws IOException {
        ObjectId lastCommitId = repository.resolve(Constants.HEAD);
        if (lastCommitId == null) {
            return null;
        }
        // a RevWalk allows to walk over commits based on some filtering
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(lastCommitId);

            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            return tree;
        }
    }

    // TODO: SJ: Fix the exception handling in this method
    public RevTree getTreeForCommit(Repository repository, String commitId) throws IOException {
        ObjectId commitObjectId = repository.resolve(commitId);
        if (commitObjectId == null) {
            return null;
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitObjectId);

            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            return tree;
        }
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

                // Add the file to git
                try (Git git = new Git(repo)) {
                    git.add().addFilepattern(getGitPath(path)).call();

                    git.close();
                    result = true;
                } catch (GitAPIException e) {
                    logger.error("error adding file to git: site: " + site + " path: " + path, e);
                    result = false;
                }
            }
        } catch (IOException e) {
            logger.error("error writing file: site: " + site + " path: " + path, e);
            result = false;
        }

        return result;
    }

    public String commitFile(Repository repo, String site, String path, String comment, PersonIdent user) {
        String commitId = null;
        String gitPath = getGitPath(path);
        Status status;

        try (Git git = new Git(repo)) {
            status = git.status().addPath(gitPath).call();

            // TODO: SJ: Below needs more thought and refactoring to detect issues with git repo and report them
            if (status.hasUncommittedChanges() || !status.isClean()) {
                RevCommit commit;
                commit = git.commit().setOnly(gitPath).setAuthor(user).setCommitter(user).setMessage(comment).call();
                commitId = commit.getName();
            }

            git.close();
        } catch (GitAPIException e) {
            logger.error("error adding and committing file to git: site: " + site + " path: " + path, e);
        }

        return commitId;
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

    /**
     * Return the current user identity as a jgit PersonIdent
     *
     * @return current user as a PersonIdent
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
     */
    public PersonIdent getAuthorIdent(String author) throws ServiceLayerException, UserNotFoundException {
        User user = userServiceInternal.getUserByIdOrUsername(-1, author);
        PersonIdent currentUserIdent =
                new PersonIdent(user.getFirstName() + " " + user.getLastName(), user.getEmail());

        return currentUserIdent;
    }
}
