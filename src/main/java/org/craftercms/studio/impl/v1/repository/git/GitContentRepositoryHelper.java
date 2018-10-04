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

package org.craftercms.studio.impl.v1.repository.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.git.auth.BasicUsernamePasswordAuthConfigurator;
import org.craftercms.commons.git.auth.SshUsernamePasswordAuthConfigurator;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.RemoteRepository;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;

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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_FILE_MODE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_FILE_MODE_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_CORE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;
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
    protected SecurityProvider securityProvider;
    protected ServicesConfig servicesConfig;

    public GitContentRepositoryHelper(StudioConfiguration studioConfiguration, SecurityProvider securityProvider,
                               ServicesConfig servicesConfig) {
        this.studioConfiguration = studioConfiguration;
        this.securityProvider = securityProvider;
        this.servicesConfig = servicesConfig;
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
                        .setMessage("Create new repository.")
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

    /**
     * Create a site git repository from scratch (Sandbox and Published)
     * @param site
     * @return true if successful, false otherwise
     */
    public boolean createSiteGitRepo(String site, String sandboxBranch) {
        boolean toReturn;
        Repository sandboxRepo = null;

        // Build a path for the site/sandbox
        Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, site);

        // Create Sandbox
        sandboxRepo = createGitRepository(siteSandboxPath);

        toReturn = (sandboxRepo != null);

        if (toReturn) {
            checkoutSandboxBranch(site, sandboxRepo, sandboxBranch);
            sandboxes.put(site, sandboxRepo);
        }

        return toReturn;
    }

    private boolean checkoutSandboxBranch(String site, Repository sandboxRepo, String sandboxBranch) {
        String sandboxBranchName = sandboxBranch;
        if (StringUtils.isEmpty(sandboxBranchName)) {
            sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
        }
        try (Git git = new Git(sandboxRepo)) {
            if (!StringUtils.equals(sandboxRepo.getBranch(), sandboxBranchName)) {
                List<Ref> branchList = git.branchList().call();
                boolean createBranch = true;
                for (Ref branch : branchList) {
                    if (StringUtils.equals(branch.getName(), sandboxBranchName) ||
                            StringUtils.equals(branch.getName(), Constants.R_HEADS + sandboxBranchName)) {
                        createBranch = false;
                        break;
                    }
                }
                if (sandboxRepo.isBare() || sandboxRepo.resolve(Constants.HEAD) == null) {
                    git.commit()
                            .setAllowEmpty(true)
                            .setMessage("Create " + sandboxBranchName + " branch.")
                            .call();
                }
                git.checkout()
                        .setCreateBranch(createBranch)
                        .setName(sandboxBranchName)
                        .setForce(false)
                        .call();
            }
            return true;
        } catch (GitAPIException | IOException e) {
            logger.error("Error checking out sandbox branch " + sandboxBranchName + " for site " + site, e);
            return false;
        }
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

    public boolean copyContentFromBlueprint(String blueprint, String site) {
        boolean toReturn = true;

        // Build a path to the Sandbox repo we'll be copying to
        Path siteRepoPath = buildRepoPath(GitRepositories.SANDBOX, site);
        // Build a path to the blueprint
        Path blueprintPath = buildRepoPath(GitRepositories.GLOBAL).resolve(Paths.get(studioConfiguration.getProperty
            (StudioConfiguration.BLUE_PRINTS_PATH), blueprint));
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
            sboxRepo.close();
            sandboxes.remove(site);
            RepositoryCache.close(sboxRepo);
            sboxRepo = null;
            Repository pubRepo = published.get(site);
            pubRepo.close();
            published.remove(site);
            RepositoryCache.close(pubRepo);
            pubRepo = null;
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

    public boolean updateSitenameConfigVar(String site) {
        boolean toReturn = true;
        String siteConfigFolder = "/config/studio";
        if (!replaceSitenameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME)))) {
            toReturn = false;
        } else if (!replaceSitenameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH),
                studioConfiguration.getProperty(CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME)))) {
            toReturn = false;
        } else if (!replaceSitenameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(),
                        studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH),
                studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME)))) {
            toReturn = false;
        }
        return toReturn;
    }

    protected boolean replaceSitenameVariable(String site, Path path) {
        boolean toReturn = false;
        Charset charset = StandardCharsets.UTF_8;
        String content = null;
        try {
            content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, site);
            Files.write(path, content.getBytes(charset));
            toReturn = true;
        } catch (IOException e) {
            logger.error("Error replacing sitename variable inside configuration file " + path.toString() +
                    " for site " + site);
            toReturn = false;
        }
        return toReturn;
    }

    public boolean bulkImport(String site /* , Map<String, String> filesCommitIds */) {
        // TODO: SJ: Define this further and build it along with API & Content Service equivalent with business logic
        // TODO: SJ: This could be in 2.6.1+ or 2.7.x
        // write all files to disk
        // commit all files
        // return data structure of file name & commit id per file
        // the caller will update the database
        //
        // considerations:
        //   accept a zip file
        //   accept a root folder or allow nesting
        //   content service should call this and then update the database
        //   find an efficient way to bulk write the files and then do a single commit across all
        return false;
    }

    /**
     * Perform an initial commit after large changes to a site. Will not work against the global config repo.
     * @param site
     * @param message
     * @return true if successful, false otherwise
     */
    public boolean performInitialCommit(String site, String message, String sandboxBranch) {
        boolean toReturn = true;

        Repository repo = getRepository(site, GitRepositories.SANDBOX, sandboxBranch);

        try (Git git = new Git(repo)) {

            Status status = git.status().call();

            if (status.hasUncommittedChanges() || !status.isClean()) {
                DirCache dirCache = git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS).call();
                RevCommit commit = git.commit()
                    .setMessage(message)
                    .call();
                // TODO: SJ: Do we need the commit id?
                // commitId = commit.getName();
            }

            checkoutSandboxBranch(site, repo, sandboxBranch);

            // Create Published by cloning Sandbox

            // Build a path for the site/sandbox
            Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, site);
            // Built a path for the site/published
            Path sitePublishedPath = buildRepoPath(GitRepositories.PUBLISHED, site);
            try (Git publishedGit = Git.cloneRepository()
                    .setURI(sitePublishedPath.relativize(siteSandboxPath).toString())
                    .setDirectory(sitePublishedPath.normalize().toAbsolutePath().toFile())
                    .call()) {
                Repository publishedRepo = publishedGit.getRepository();
                publishedRepo = optimizeRepository(publishedRepo);
                checkoutSandboxBranch(site, publishedRepo, sandboxBranch);
                publishedRepo.close();
                publishedGit.close();
            } catch (GitAPIException | IOException e) {
                logger.error("Error adding origin (sandbox) to published repository", e);
            }
            git.close();
        } catch (GitAPIException err) {
            logger.error("error creating initial commit for site:  " + site, err);
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
                        logger.error("error getting the sandbox repository for site: " + site);
                    }
                }
                break;
            case PUBLISHED:
                repo = published.get(site);
                if (repo == null) {
                    if (buildSiteRepo(site)) {
                        repo = published.get(site);
                    } else {
                        logger.error("error getting the published repository for site: " + site);
                    }
                }
                break;
            case GLOBAL:
                if (globalRepo == null) {
                    Path globalConfigRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);
                    try {
                        globalRepo = openRepository(globalConfigRepoPath);
                    } catch (IOException e) {
                        logger.error("error getting the global repository.", e);
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

    public Repository getRepository(String site, GitRepositories gitRepository, String sandboxBranch) {
        Repository repo;

        logger.debug("getRepository invoked with site" + site + "Repository Type: " + gitRepository.toString());

        switch (gitRepository) {
            case SANDBOX:
                repo = sandboxes.get(site);
                if (repo == null) {
                    if (buildSiteRepo(site)) {
                        repo = sandboxes.get(site);
                        checkoutSandboxBranch(site, repo, sandboxBranch);
                    } else {
                        logger.error("error getting the sandbox repository for site: " + site);
                    }
                }
                break;
            case PUBLISHED:
                repo = published.get(site);
                if (repo == null) {
                    if (buildSiteRepo(site)) {
                        repo = published.get(site);
                    } else {
                        logger.error("error getting the published repository for site: " + site);
                    }
                }
                break;
            case GLOBAL:
                if (globalRepo == null) {
                    Path globalConfigRepoPath = buildRepoPath(GitRepositories.GLOBAL).resolve(GIT_ROOT);
                    try {
                        globalRepo = openRepository(globalConfigRepoPath);
                    } catch (IOException e) {
                        logger.error("error getting the global repository.", e);
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
    public RevTree getTreeForLastCommit(Repository repository) throws AmbiguousObjectException,
        IncorrectObjectTypeException,
        IOException, MissingObjectException {
        ObjectId lastCommitId = repository.resolve(Constants.HEAD);

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

    /**
     * Return the current user identity as a jgit PersonIdent
     *
     * @return current user as a PersonIdent
     */
    public PersonIdent getCurrentUserIdent() throws ServiceLayerException, UserNotFoundException {
        String userName = securityProvider.getCurrentUser();
        return getAuthorIdent(userName);
    }

    /**
     * Return the author identity as a jgit PersonIdent
     *
     * @param author author
     * @return author user as a PersonIdent
     */
    public PersonIdent getAuthorIdent(String author) throws ServiceLayerException, UserNotFoundException {
        User user = securityProvider.getUserByIdOrUsername(-1, author);
        PersonIdent currentUserIdent =
                new PersonIdent(user.getFirstName() + " " + user.getLastName(), user.getEmail());

        return currentUserIdent;
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

                try (ObjectReader reader = repository.newObjectReader()) {
                    CanonicalTreeParser prevCommitTreeParser = new CanonicalTreeParser();
                    CanonicalTreeParser nextCommitTreeParser = new CanonicalTreeParser();
                    prevCommitTreeParser.reset(reader, parentTree.getId());
                    nextCommitTreeParser.reset(reader, commitTree.getId());

                    // Diff the two commit Ids
                    List<DiffEntry> diffEntries = git.diff()
                            .setOldTree(prevCommitTreeParser)
                            .setNewTree(nextCommitTreeParser)
                            .call();
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

        } catch (IOException e) {
            logger.error("Error while getting list of files in commit " + commit.getId().getName());
        } finally {
            rw.dispose();
        }
        return files;
    }

    public boolean createSiteCloneRemoteGitRepo(String siteId, String sandboxBranch, String remoteName,
                                                String remoteUrl, String remoteBranch, boolean singleBranch,
                                                String authenticationType, String remoteUsername,
                                                String remotePassword, String remoteToken, String remotePrivateKey)
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
        CloneCommand cloneCommand = Git.cloneRepository();
        Git cloneResult = null;

        try {
            final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(),".tmp");
            switch (authenticationType) {
                case RemoteRepository.AuthenticationType.NONE:
                    logger.debug("No authentication");
                    break;
                case RemoteRepository.AuthenticationType.BASIC:
                    logger.debug("Basic authentication");
                    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(remoteUsername,
                            remotePassword));
                    break;
                case RemoteRepository.AuthenticationType.TOKEN:
                    logger.debug("Token based authentication");
                    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(remoteToken,
                            StringUtils.EMPTY));
                    break;
                case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                    logger.debug("Private key authentication");
                    tempKey.toFile().deleteOnExit();
                    cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
                        @Override
                        public void configure(Transport transport) {
                            SshTransport sshTransport = (SshTransport)transport;
                            sshTransport.setSshSessionFactory(getSshSessionFactory(remotePrivateKey, tempKey));
                        }
                    });

                    break;
                default:
                    throw new ServiceLayerException("Unsupported authentication type " + authenticationType);
            }
            if (StringUtils.isNotEmpty(remoteBranch)) {
                cloneCommand.setBranch(remoteBranch);
            }
            cloneResult = cloneCommand
                    .setURI(remoteUrl)
                    .setDirectory(localPath)
                    .setRemote(remoteName)
                    .setCloneAllBranches(!singleBranch)
                    .call();
            Files.deleteIfExists(tempKey);
            Repository sandboxRepo = checkIfCloneWasOk(cloneResult, remoteName, remoteUrl) ;

            sandboxRepo = optimizeRepository(sandboxRepo);

            sandboxes.put(siteId, sandboxRepo);
        } catch (InvalidRemoteException e) {
            logger.error("Invalid remote repository: " + remoteName + " (" + remoteUrl + ")", e);
            throw new InvalidRemoteRepositoryException("Invalid remote repository: " + remoteName + " (" +
                    remoteUrl + ")");
        } catch (TransportException e) {
            if (StringUtils.endsWithIgnoreCase(e.getMessage(), "not authorized")) {
                logger.error("Bad credentials or read only repository: " + remoteName + " (" + remoteUrl + ")",
                        e);
                throw new InvalidRemoteRepositoryCredentialsException("Bad credentials or read only repository: " +
                        remoteName + " (" + remoteUrl + ") for username " + remoteUsername, e);
            } else {
                logger.error("Remote repository not found: " + remoteName + " (" + remoteUrl + ")", e);
                throw new RemoteRepositoryNotFoundException("Remote repository not found: " + remoteName + " (" +
                        remoteUrl + ")");
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

    private void configureTransportAuthenticaion(final CloneCommand cloneCommand, final String remotePassword,
                                                 final String remoteUsername, final String remoteUrl) {
        // Check if this remote git repository has username/password provided
        if (!StringUtils.isEmpty(remoteUsername)) {
            if (StringUtils.isEmpty(remotePassword)) {
                // Username was provided but password is empty
                logger.debug("Password field is empty while cloning from remote repository: " + remoteUrl);
            }
            // Studio should only support usr/pwd ssh repo.
            // until we add per user + per server private key configuration.
            if (remoteUrl.toLowerCase().contains("ssh://")) {
                new SshUsernamePasswordAuthConfigurator(remotePassword).configureAuthentication(cloneCommand);
            } else {
                new BasicUsernamePasswordAuthConfigurator(remoteUsername, remotePassword)
                        .configureAuthentication(cloneCommand);
            }
        }
    }
}
