/*
 * Crafter Studio
 *
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.google.gdata.util.common.base.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_COMPRESSION_DEFAULT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_CORE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

/**
 * Created by Sumer Jabri
 */
public class GitContentRepositoryHelper {
    private static final Logger logger = LoggerFactory.getLogger(GitContentRepositoryHelper.class);

    Map<String, Repository> sandboxes = new HashMap<>();
    Map<String, Repository> published = new HashMap<>();

    Repository globalRepo = null;

    StudioConfiguration studioConfiguration;
    SecurityProvider securityProvider;

    GitContentRepositoryHelper(StudioConfiguration studioConfiguration, SecurityProvider securityProvider) {
        this.studioConfiguration = studioConfiguration;
        this.securityProvider = securityProvider;
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
        Path sitePublishedRepoPath = buildRepoPath(GitRepositories.SANDBOX, site).resolve(GIT_ROOT);

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
            logger.error("Failed to create published repo for site: " + site + " using path " + sitePublishedRepoPath
                .toString(), e);
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
            gitPath = Paths.get(File.separator).relativize(gitPath);
        } catch (IllegalArgumentException e) {
            logger.debug("Path: " + path + " is already relative path.");
        }
        if (StringUtils.isEmpty(gitPath.toString())) {
            return ".";
        }
        return gitPath.toString();
    }

    public Repository createGitRepository(Path path) {
        Repository toReturn;
        path = Paths.get(path.toAbsolutePath().toString(), GIT_ROOT);
        try {
            toReturn = FileRepositoryBuilder.create(path.toFile());
            toReturn.create();

            // Get git configuration
            StoredConfig config = toReturn.getConfig();
            // Set compression level (core.compression)
            config.setInt(CONFIG_SECTION_CORE, null, CONFIG_PARAMETER_COMPRESSION, CONFIG_PARAMETER_COMPRESSION_DEFAULT);
            // Set big file threshold (core.bigFileThreshold)
            config.setString(CONFIG_SECTION_CORE,null,CONFIG_PARAMETER_BIG_FILE_THRESHOLD, CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT);
            // Save configuration changes
            config.save();
        } catch (IOException e) {
            logger.error("Error while creating repository for site with path" + path.toString(), e);
            toReturn = null;
        }

        return toReturn;
    }

    public Path buildRepoPath(GitRepositories repoType) {
        return buildRepoPath(repoType, StringUtil.EMPTY_STRING);
    }
    public Path buildRepoPath(GitRepositories repoType, String site) {
        Path path;
        switch (repoType) {
            case SANDBOX:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH), studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site, studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH));
                break;
            case PUBLISHED:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH), studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site, studioConfiguration.getProperty(StudioConfiguration.PUBLISHED_PATH));
                break;
            case GLOBAL:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH), studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH));
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
    public boolean createSiteGitRepo(String site) {
        boolean toReturn;
        Repository sandboxRepo = null;
        Repository publishedRepo = null;

        // Build a path for the site/sandbox
        Path siteSandboxPath = buildRepoPath(GitRepositories.SANDBOX, site);
        // Built a path for the site/published
        Path sitePublishedPath = buildRepoPath(GitRepositories.PUBLISHED, site);

        // Create Sandbox
        sandboxRepo = createGitRepository(siteSandboxPath);

        // Create Published
        if (sandboxRepo != null) {
            publishedRepo = createGitRepository(sitePublishedPath);
        }

        toReturn = (sandboxRepo != null) && (publishedRepo != null);

        if (toReturn) {
            sandboxes.put(site, sandboxRepo);
            published.put(site, publishedRepo);
        }

        return toReturn;
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
        // Get parent of that (since every site has two repos: Sandbox and Published
        Path sitePath = siteSandboxPath.getParent();
        // Get a file handle to the parent and delete it
        File siteFolder = sitePath.toFile();
        toReturn = siteFolder.delete();

        // If delete successful, remove from in-memory cache
        if (toReturn) {
            sandboxes.remove(site);
            published.remove(site);
        }

        return toReturn;
    }

    public boolean updateSitenameConfigVar(String site) {
        boolean toReturn = true;
        String siteConfigFolder = "/config/studio";
        if (!replaceSitenameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(), studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_SITE_GENERAL_CONFIG_FILE_NAME)))) {
            toReturn = false;
        } else if (!replaceSitenameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(), studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME)))) {
            toReturn = false;
        } else if (!replaceSitenameVariable(site,
                Paths.get(buildRepoPath(GitRepositories.SANDBOX, site).toAbsolutePath().toString(), studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME)))) {
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
            logger.error("Error replacing sitename variable inside configuration file " + path.toString() + " for site " + site);
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
    public boolean performInitialCommit(String site, String message) {
        boolean toReturn = true;

        try {
            Repository repo = getRepository(site, GitRepositories.SANDBOX);

            Git git = new Git(repo);

            Status status = git.status().call();

            if (status.hasUncommittedChanges() || !status.isClean()) {
                DirCache dirCache = git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS).call();
                RevCommit commit = git.commit()
                    .setMessage(message)
                    .call();
                // TODO: SJ: Do we need the commit id?
                // commitId = commit.getName();
            }
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
                        logger.error("error getting the repository for site: " + site);
                    }
                }
                break;
            case PUBLISHED:
                repo = published.get(site);
                if (repo == null) {
                    if (buildSiteRepo(site)) {
                        repo = published.get(site);
                    } else {
                        logger.error("error getting the repository for site: " + site);
                    }
                }
                break;
            case GLOBAL:
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
                FileChannel outChannel = new FileOutputStream(file.getPath()).getChannel();
                logger.debug("created the file output channel");
                ReadableByteChannel inChannel = Channels.newChannel(content);
                logger.debug("created the file input channel");
                long amount = 1024*1024; // 1MB at a time
                long count;
                long offset = 0;
                while ((count = outChannel.transferFrom(inChannel, offset, amount)) > 0) {
                    logger.debug("writing the bits: offset = " + offset + " count: " + count);
                    offset += count;
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
    public PersonIdent getCurrentUserIdent() {
        String userName = securityProvider.getCurrentUser();
        return getAuthorIdent(userName);
    }

    /**
     * Return the author identity as a jgit PersonIdent
     *
     * @param author author
     * @return author user as a PersonIdent
     */
    public PersonIdent getAuthorIdent(String author) {
        Map<String, Object> currentUserProfile = securityProvider.getUserProfile(author);
        PersonIdent currentUserIdent = new PersonIdent
                (currentUserProfile.get("first_name").toString() + " " + currentUserProfile.get("last_name").toString(),
                        currentUserProfile.get("email").toString());

        return currentUserIdent;
    }
}
