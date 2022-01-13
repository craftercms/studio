/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.GitLogDAO;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryDAO;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotBareException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.dal.RetryingOperationFacade;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.cluster.StudioClusterUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.springframework.web.context.ServletContextAware;

import static java.lang.Integer.MAX_VALUE;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.time.ZoneOffset.UTC;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.GitRepositories.GLOBAL;
import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_GLOBAL_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.GLOBAL_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_FROM_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_TO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REPO_COMMIT_MESSAGE_PATH_VAR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REPO_COMMIT_MESSAGE_USERNAME_VAR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_PUBLISHED_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v2.dal.RemoteRepository.AuthenticationType.BASIC;
import static org.craftercms.studio.api.v2.dal.RemoteRepository.AuthenticationType.NONE;
import static org.craftercms.studio.api.v2.dal.RemoteRepository.AuthenticationType.PRIVATE_KEY;
import static org.craftercms.studio.api.v2.dal.RemoteRepository.AuthenticationType.TOKEN;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.BOOTSTRAP_REPO;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COPY_CONTENT_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_CREATE_FOLDER_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_DELETE_CONTENT_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_INITIAL_COMMIT_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_MOVE_CONTENT_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_WRITE_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.EMPTY_FILE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK;
import static org.eclipse.jgit.api.ListBranchCommand.ListMode.REMOTE;
import static org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME;
import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.eclipse.jgit.lib.Constants.OBJ_TREE;
import static org.eclipse.jgit.lib.Constants.R_REMOTES;
import static org.eclipse.jgit.merge.MergeStrategy.THEIRS;
import static org.eclipse.jgit.revwalk.RevSort.REVERSE;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_NODELETE;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_OTHER_REASON;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED;

public class GitContentRepository implements ContentRepository, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);
    private TextEncryptor encryptor;

    private static final String STUDIO_MANIFEST_LOCATION = "/META-INF/MANIFEST.MF";

    protected ServletContext ctx;
    protected StudioConfiguration studioConfiguration;
    protected ServicesConfig servicesConfig;
    protected GitLogDAO gitLogDao;
    protected RemoteRepositoryDAO remoteRepositoryDAO;
    protected UserServiceInternal userServiceInternal;
    protected SecurityService securityService;
    protected SiteFeedMapper siteFeedMapper;
    protected ClusterDAO clusterDao;
    protected GeneralLockService generalLockService;
    protected RetryingOperationFacade retryingOperationFacade;
    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    protected StudioClusterUtils studioClusterUtils;

    @Override
    public boolean contentExists(String site, String path) {
        boolean toReturn = false;
        try {
            GitRepositoryHelper helper =  GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repo != null ) {

                RevTree tree = helper.getTreeForLastCommit(repo);
                try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                    // Check if the array of items is not null, and since we have an absolute path to the item,
                    // pick the first item in the list
                    if (tw != null && tw.getObjectId(0) != null) {
                        toReturn = true;
                        tw.close();
                    } else if (tw == null) {
                        String gitPath = helper.getGitPath(path);
                        if (StringUtils.isEmpty(gitPath) || gitPath.equals(".")) {
                            toReturn = true;
                        }
                    }
                } catch (IOException e) {
                    logger.info("Content not found for site: " + site + " path: " + path, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }
        return toReturn;
    }

    @Override
    public InputStream getContent(String site, String path) throws ContentNotFoundException, CryptoException {
        InputStream toReturn = null;
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
        Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
        if (repo == null) {
            throw new ContentNotFoundException("Repository not found for site " + site);
        }

        try {
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                // Check if the array of items is not null, and since we have an absolute path to the item,
                // pick the first item in the list
                if (tw != null && tw.getObjectId(0) != null) {
                    ObjectId id = tw.getObjectId(0);
                    ObjectLoader objectLoader = repo.open(id);
                    toReturn = objectLoader.openStream();
                    tw.close();
                }
            } catch (IOException e) {
                logger.error("Error while getting content for file at site: " + site + " path: " + path, e);
            }
        } catch (IOException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }

        return toReturn;
    }

    @Override
    public long getContentSize(final String site, final String path) {
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                if (tw != null && tw.getObjectId(0) != null) {
                    ObjectId id = tw.getObjectId(0);
                    ObjectLoader objectLoader = repo.open(id);
                    return objectLoader.getSize();
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error while getting content for file at site: " + site + " path: " + path, e);
        }
        return -1L;
    }

    @Override
    public String writeContent(String site, String path, InputStream content) {
        // Write content to git and commit it
        String commitId = null;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                Repository repo =
                        helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
                if (repo != null) {
                    if (helper.writeFile(repo, site, path, content)) {
                        PersonIdent user = helper.getCurrentUserIdent();
                        String username = securityService.getCurrentUser();
                        String comment = helper.getCommitMessage(REPO_SANDBOX_WRITE_COMMIT_MESSAGE)
                                .replace(REPO_COMMIT_MESSAGE_USERNAME_VAR, username)
                                .replace(REPO_COMMIT_MESSAGE_PATH_VAR, path);
                        commitId = helper.commitFile(repo, site, path, comment, user);
                    } else {
                        logger.error("Failed to write content site: " + site + " path: " + path);
                    }
                } else {
                    logger.error("Missing repository during write for site: " + site + " path: " + path);
                }
            }
        } catch (ServiceLayerException | UserNotFoundException | CryptoException e) {
            logger.error("Unknown service error during write for site: " + site + " path: " + path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return commitId;
    }

    @Override
    public String createFolder(String site, String path, String name) {
        // SJ: Git doesn't care about empty folders, so we will create the folders and put a 0 byte file in them
        String commitId = null;
        boolean result;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                Path emptyFilePath = Paths.get(path, name, EMPTY_FILE);
                Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

                try {
                    // Create basic file
                    File file = new File(repo.getDirectory().getParent(), emptyFilePath.toString());

                    // Create parent folders
                    File folder = file.getParentFile();
                    if (folder != null) {
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                    }

                    // Create the file
                    if (!file.createNewFile()) {
                        logger.error("error writing file: site: " + site + " path: " + emptyFilePath);
                        result = false;
                    } else {
                        // Add the file to git
                        try (Git git = new Git(repo)) {
                            AddCommand addCommand =
                                    git.add().addFilepattern(helper.getGitPath(emptyFilePath.toString()));
                            retryingRepositoryOperationFacade.call(addCommand);

                            git.close();
                            result = true;
                        } catch (GitAPIException e) {
                            logger.error("error adding file to git: site: " + site + " path: " + emptyFilePath, e);
                            result = false;
                        }
                    }
                } catch (IOException e) {
                    logger.error("error writing file: site: " + site + " path: " + emptyFilePath, e);
                    result = false;
                }

                if (result) {
                    try {
                        commitId = helper.commitFile(repo, site, emptyFilePath.toString(),
                                helper.getCommitMessage(REPO_CREATE_FOLDER_COMMIT_MESSAGE)
                                        .replaceAll(PATTERN_SITE, site)
                                        .replaceAll(PATTERN_PATH, path + FILE_SEPARATOR + name),
                                helper.getCurrentUserIdent());
                    } catch (ServiceLayerException | UserNotFoundException e) {
                        logger.error("Unknown service error during commit for site: " + site + " path: "
                                + emptyFilePath, e);
                    }
                }
            }
        } catch (CryptoException e) {
            logger.error("Unexpected service error creating folder " + name + " for site: " + site + " path: "
                    + path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return commitId;
    }

    @Override
    public String deleteContent(String site, String path, String approver) {
        String commitId = null;
        boolean isPage = path.endsWith(FILE_SEPARATOR + INDEX_FILE);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

                try (Git git = new Git(repo)) {
                    String pathToDelete = helper.getGitPath(path);
                    Path parentToDelete = Paths.get(pathToDelete).getParent();
                    RmCommand rmCommand = git.rm().addFilepattern(pathToDelete).setCached(false);
                    retryingRepositoryOperationFacade.call(rmCommand);

                    String pathToCommit = pathToDelete;
                    if (isPage) {
                        pathToCommit = deleteParentFolder(git, parentToDelete, true);
                    }

                    // TODO: SJ: we need to define messages in a string table of sorts
                    commitId = helper.commitFile(repo, site, pathToCommit,
                            helper.getCommitMessage(REPO_DELETE_CONTENT_COMMIT_MESSAGE)
                                    .replaceAll(PATTERN_PATH, path),
                            StringUtils.isEmpty(approver) ? helper.getCurrentUserIdent() : helper.getAuthorIdent(approver));

                }
            }
        } catch (GitAPIException | UserNotFoundException | IOException e) {
            logger.error("Error while deleting content for site: " + site + " path: " + path, e);
        } catch (ServiceLayerException | CryptoException e) {
            logger.error("Unknown service error during delete for site: " + site + " path: " + path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return commitId;
    }

    private String deleteParentFolder(Git git, Path parentFolder, boolean wasPage)
            throws GitAPIException, CryptoException, IOException {
        String parent = parentFolder.toString();
        String toRet = parent;
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
        String folderToDelete = helper.getGitPath(parent);
        Path toDelete = Paths.get(git.getRepository().getDirectory().getParent(), parent);
        if (toDelete.toFile().exists()) {
            List<String> dirs = Files.walk(toDelete).filter(x -> !x.equals(toDelete)).filter(Files::isDirectory)
                        .map(y -> y.getFileName().toString()).collect(Collectors.toList());
            List<String> files = Files.walk(toDelete, 1).filter(x -> !x.equals(toDelete)).filter(Files::isRegularFile)
                        .map(y -> y.getFileName().toString()).collect(Collectors.toList());
            if (wasPage ||
                    (CollectionUtils.isEmpty(dirs) &&
                            (CollectionUtils.isEmpty(files) || files.size() < 2 && files.get(0).equals(EMPTY_FILE)))) {
                if (CollectionUtils.isNotEmpty(dirs)) {
                    for (String child : dirs) {
                        Path childToDelete = Paths.get(folderToDelete, child);
                        deleteParentFolder(git, childToDelete, false);
                        RmCommand rmCommand = git.rm()
                                .addFilepattern(folderToDelete + FILE_SEPARATOR + child + FILE_SEPARATOR + "*")
                                .setCached(false);
                        retryingRepositoryOperationFacade.call(rmCommand);

                    }
                }

                if (CollectionUtils.isNotEmpty(files)) {
                    for (String child : files) {
                        git.rm()
                                .addFilepattern(folderToDelete + FILE_SEPARATOR + child)
                                .setCached(false)
                                .call();

                    }
                }
            }
        }
        return toRet;
    }

    @Override
    public Map<String, String> moveContent(String site, String fromPath, String toPath, String newName) {
        Map<String, String> toRet = new TreeMap<String, String>();
        String commitId;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

                String gitFromPath = helper.getGitPath(fromPath);
                String gitToPath;
                if (StringUtils.isEmpty(newName)) {
                    gitToPath = helper.getGitPath(toPath);
                } else {
                    gitToPath = helper.getGitPath(toPath + FILE_SEPARATOR + newName);
                }

                try (Git git = new Git(repo)) {
                    // Check if destination is a file, then this is a rename operation
                    // Perform rename and exit
                    Path sourcePath = Paths.get(repo.getDirectory().getParent(), gitFromPath);
                    File sourceFile = sourcePath.toFile();
                    Path targetPath = Paths.get(repo.getDirectory().getParent(), gitToPath);
                    File targetFile = targetPath.toFile();

                    if (sourceFile.getCanonicalFile().equals(targetFile.getCanonicalFile())) {
                        sourceFile.renameTo(targetFile);
                    } else {
                        if (targetFile.isFile()) {
                            if (sourceFile.isFile()) {
                                sourceFile.renameTo(targetFile);
                            } else {
                                // This is not a valid operation
                                logger.error("Invalid move operation: Trying to rename a directory to a file " +
                                        "for site: " + site + " fromPath: " + fromPath + " toPath: " + toPath +
                                        " newName: " + newName);
                            }
                        } else if (sourceFile.isDirectory()) {
                            // Check if we're moving a single file or whole subtree
                            File[] dirList = sourceFile.listFiles();
                            for (File child : dirList) {
                                if (!child.equals(sourceFile)) {
                                    FileUtils.moveToDirectory(child, targetFile, true);
                                }
                            }
                            FileUtils.deleteDirectory(sourceFile);
                        } else {
                            if (sourceFile.isFile()) {
                                FileUtils.moveFile(sourceFile, targetFile);
                            } else {
                                FileUtils.moveToDirectory(sourceFile, targetFile, true);
                            }
                        }
                    }

                    // The operation is done on disk, now it's time to commit
                    AddCommand addCommand = git.add().addFilepattern(gitToPath);
                    retryingRepositoryOperationFacade.call(addCommand);

                    StatusCommand statusCommand = git.status().addPath(gitToPath);
                    Status gitStatus = retryingRepositoryOperationFacade.call(statusCommand);
                    Set<String> changeSet = gitStatus.getAdded();

                    for (String pathToCommit : changeSet) {
                        String pathRemoved = pathToCommit.replace(gitToPath, gitFromPath);
                        CommitCommand commitCommand = git.commit()
                                .setOnly(pathToCommit)
                                .setOnly(pathRemoved)
                                .setAuthor(helper.getCurrentUserIdent())
                                .setCommitter(helper.getCurrentUserIdent())
                                .setMessage(helper.getCommitMessage(REPO_MOVE_CONTENT_COMMIT_MESSAGE)
                                        .replaceAll(PATTERN_FROM_PATH, fromPath)
                                        .replaceAll(PATTERN_TO_PATH, toPath +
                                                (StringUtils.isNotEmpty(newName) ? newName : EMPTY)));
                        RevCommit commit = retryingRepositoryOperationFacade.call(commitCommand);
                        commitId = commit.getName();
                        toRet.put(pathToCommit, commitId);
                    }
                }
            }
        } catch (IOException | GitAPIException | ServiceLayerException | UserNotFoundException | CryptoException e) {
            logger.error("Error while moving content for site: " + site + " fromPath: " + fromPath +
                    " toPath: " + toPath + " newName: " + newName);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toRet;
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        String commitId = null;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

                String gitFromPath = helper.getGitPath(fromPath);
                String gitToPath = helper.getGitPath(toPath);

                try (Git git = new Git(repo)) {
                    Path sourcePath = Paths.get(repo.getDirectory().getParent(), fromPath);
                    File sourceFile = sourcePath.toFile();
                    Path targetPath = Paths.get(repo.getDirectory().getParent(), toPath);
                    File targetFile = targetPath.toFile();

                    // Check if we're copying a single file or whole subtree
                    FileUtils.copyDirectory(sourceFile, targetFile);

                    // The operation is done on disk, now it's time to commit
                    git.add().addFilepattern(gitToPath).call();
                    CommitCommand commitCommand = git.commit()
                            .setOnly(gitFromPath)
                            .setOnly(gitToPath)
                            .setAuthor(helper.getCurrentUserIdent())
                            .setCommitter(helper.getCurrentUserIdent())
                            .setMessage(helper.getCommitMessage(REPO_COPY_CONTENT_COMMIT_MESSAGE)
                                    .replaceAll(PATTERN_FROM_PATH, fromPath).replaceAll(PATTERN_TO_PATH, toPath));
                    RevCommit commit = retryingRepositoryOperationFacade.call(commitCommand);
                    commitId = commit.getName();

                }
            }
        } catch (IOException | GitAPIException | ServiceLayerException | UserNotFoundException | CryptoException e) {
            logger.error("Error while copying content for site: " + site + " fromPath: " + fromPath +
                    " toPath: " + toPath + " newName: ");
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return commitId;
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        // TODO: SJ: Rethink this API call for 3.1+
        final List<RepositoryItem> retItems = new ArrayList<RepositoryItem>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {

                if (tw != null) {
                    // Loop for all children and gather path of item excluding the item, file/folder name, and
                    // whether or not it's a folder
                    ObjectLoader loader = repo.open(tw.getObjectId(0));
                    if (loader.getType() == OBJ_TREE) {
                        int depth = tw.getDepth();
                        tw.enterSubtree();
                        while (tw.next()) {
                            if (tw.getDepth() == depth + 1) {

                                RepositoryItem item = new RepositoryItem();
                                item.name = tw.getNameString();

                                String visitFolderPath = FILE_SEPARATOR + tw.getPathString();
                                loader = repo.open(tw.getObjectId(0));
                                item.isFolder = loader.getType() == OBJ_TREE;
                                int lastIdx = visitFolderPath.lastIndexOf(FILE_SEPARATOR + item.name);
                                if (lastIdx > 0) {
                                    item.path = visitFolderPath.substring(0, lastIdx);
                                }

                                if (!ArrayUtils.contains(IGNORE_FILES, item.name)) {
                                    retItems.add(item);
                                }
                            }
                        }
                        tw.close();
                    } else {
                        logger.debug("Object is not tree for site: " + site + " path: " + path +
                                " - it does not have children");
                    }
                } else {
                    String gitPath = helper.getGitPath(path);
                    if (StringUtils.isEmpty(gitPath) || gitPath.equals(".")) {
                        try (TreeWalk treeWalk = new TreeWalk(repo)) {
                            treeWalk.addTree(tree);

                            while (treeWalk.next()) {

                                ObjectLoader loader = repo.open(treeWalk.getObjectId(0));

                                RepositoryItem item = new RepositoryItem();
                                item.name = treeWalk.getNameString();

                                String visitFolderPath = FILE_SEPARATOR + treeWalk.getPathString();
                                loader = repo.open(treeWalk.getObjectId(0));
                                item.isFolder = loader.getType() == OBJ_TREE;
                                int lastIdx = visitFolderPath.lastIndexOf(FILE_SEPARATOR + item.name);
                                if (lastIdx > 0) {
                                    item.path = visitFolderPath.substring(0, lastIdx);
                                } else {
                                    item.path = EMPTY;
                                }

                                if (!ArrayUtils.contains(IGNORE_FILES, item.name)) {
                                    retItems.add(item);
                                }
                            }

                        } catch (IOException e) {
                            logger.error("Error while getting children for site: " + site + " path: " + path, e);
                        }

                    }
                }
            } catch (IOException e) {
                logger.error("Error while getting children for site: " + site + " path: " + path, e);
            }
        } catch (IOException | CryptoException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }

        RepositoryItem[] items = new RepositoryItem[retItems.size()];
        items = retItems.toArray(items);
        return items;
    }

    @Override
    public VersionTO[] getContentVersionHistory(String site, String path) {
        List<VersionTO> versionHistory = new ArrayList<VersionTO>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

                ObjectId head = repo.resolve(HEAD);
                String gitPath = helper.getGitPath(path);
                try (Git git = new Git(repo)) {
                    Iterable<RevCommit> commits = git.log().add(head).addPath(gitPath).call();
                    Iterator<RevCommit> iterator = commits.iterator();
                    while (iterator.hasNext()) {
                        RevCommit revCommit = iterator.next();
                        VersionTO versionTO = new VersionTO();
                        versionTO.setVersionNumber(revCommit.getName());
                        versionTO.setLastModifier(revCommit.getAuthorIdent().getName());
                        versionTO.setLastModifiedDate(
                                Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(UTC));
                        versionTO.setComment(revCommit.getFullMessage());
                        versionHistory.add(versionTO);
                    }
                } catch (IOException e) {
                    logger.error("error while getting history for content item " + path);
                }
            }
        } catch (IOException | GitAPIException | CryptoException e) {
            logger.error("Failed to create Git repo for site: " + site + " path: " + path, e);
        }

        VersionTO[] toRet = new VersionTO[versionHistory.size()];
        return versionHistory.toArray(toRet);
    }

    @Override
    public String createVersion(String site, String path, boolean majorVersion) {
        return createVersion(site, path, EMPTY, majorVersion);
    }

    @Override
    public String createVersion(String site, String path, String comment, boolean majorVersion) {
        // SJ: Will ignore minor revisions since git handles that via write/commit
        // SJ: Major revisions become git tags
        // TODO: SJ: Redesign/refactor the whole approach in 3.1+
        String toReturn = EMPTY;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            if (majorVersion) {
                synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : PUBLISHED)) {

                    Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : PUBLISHED);
                    // Tag the repository with a date-time based version label
                    String gitPath = helper.getGitPath(path);
    
                    try (Git git = new Git(repo)) {
                        PersonIdent currentUserIdent = helper.getCurrentUserIdent();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmssX");
                        Calendar cal = Calendar.getInstance();
                        String versionLabel = dateFormat.format(cal.getTime());

                        TagCommand tagCommand = git.tag()
                                .setName(versionLabel)
                                .setMessage(comment)
                                .setTagger(currentUserIdent);

                        retryingRepositoryOperationFacade.call(tagCommand);

                        toReturn = versionLabel;

                    } catch (GitAPIException | ServiceLayerException | UserNotFoundException err) {
                        logger.error("error creating new version for site:  " + site + " path: " + path, err);
                    }
                }
            } else {
                logger.info("request to create minor revision ignored for site: " + site + " path: " + path);
            }

        } catch (CryptoException e) {
            logger.error("Unexpected error creating new version for site:  " + site + " path: " + path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toReturn;
    }

    @Override
    public String revertContent(String site, String path, String version, boolean major, String comment) {
        // TODO: SJ: refactor to remove the notion of a major/minor for 3.1+
        String commitId = null;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            InputStream versionContent = getContentVersion(site, path, version);
            commitId = writeContent(site, path, versionContent);
            createVersion(site, path, major);
        } catch (ContentNotFoundException err) {
            logger.error("error reverting content for site:  " + site + " path: " + path, err);
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return commitId;
    }

    @Override
    public InputStream getContentVersion(String site, String path, String version) throws ContentNotFoundException {
        InputStream toReturn = null;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

            RevTree tree = helper.getTreeForCommit(repo, version);
            if (tree != null) {
                try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                    if (tw != null) {
                        ObjectId id = tw.getObjectId(0);
                        ObjectLoader objectLoader = repo.open(id);
                        toReturn = objectLoader.openStream();
                        tw.close();
                    }
                } catch (IOException e) {
                    logger.error("Error while getting content for file at site: " + site + " path: " + path +
                            " version: " + version, e);
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path + " version: " +
                    version, e);
        }

        return toReturn;
    }

    @Override
    public void lockItem(String site, String path) {
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                try (TreeWalk tw = new TreeWalk(repo)) {
                    RevTree tree = helper.getTreeForLastCommit(repo);
                    tw.addTree(tree); // tree ‘0’
                    tw.setRecursive(false);
                    tw.setFilter(PathFilter.create(path));

                    if (!tw.next()) {
                        return;
                    }

                    File repoRoot = repo.getWorkTree();
                    Paths.get(repoRoot.getPath(), tw.getPathString());
                    File file = new File(tw.getPathString());
                    LockFile lock = new LockFile(file);
                    lock.lock();
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error while locking file for site: " + site + " path: " + path, e);
        }
    }

    @Override
    public void lockItemForPublishing(String site, String path) {
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, PUBLISHED);

            synchronized (repo) {
                try (TreeWalk tw = new TreeWalk(repo)) {
                    RevTree tree = helper.getTreeForLastCommit(repo);
                    tw.addTree(tree); // tree ‘0’
                    tw.setRecursive(false);
                    tw.setFilter(PathFilter.create(path));

                    if (!tw.next()) {
                        return;
                    }

                    File repoRoot = repo.getWorkTree();
                    Paths.get(repoRoot.getPath(), tw.getPathString());
                    File file = new File(tw.getPathString());
                    LockFile lock = new LockFile(file);
                    lock.lock();
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error while locking file for site: " + site + " path: " + path, e);
        }
    }

    @Override
    public void unLockItem(String site, String path) {
        try {
            GitRepositoryHelper helper =  GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

            synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
                try (TreeWalk tw = new TreeWalk(repo)) {
                    RevTree tree = helper.getTreeForLastCommit(repo);
                    tw.addTree(tree); // tree ‘0’
                    tw.setRecursive(false);
                    tw.setFilter(PathFilter.create(path));

                    if (!tw.next()) {
                        return;
                    }

                    File repoRoot = repo.getWorkTree();
                    Paths.get(repoRoot.getPath(), tw.getPathString());
                    File file = new File(tw.getPathString());
                    LockFile lock = new LockFile(file);
                    lock.unlock();
                }
            }
        }  catch (IOException | CryptoException e) {
            logger.error("Error while unlocking file for site: " + site + " path: " + path, e);
        }
    }

    @Override
    public void unLockItemForPublishing(String site, String path) {
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, PUBLISHED);

            synchronized (repo) {
                try (TreeWalk tw = new TreeWalk(repo)) {
                    RevTree tree = helper.getTreeForLastCommit(repo);
                    tw.addTree(tree); // tree ‘0’
                    tw.setRecursive(false);
                    tw.setFilter(PathFilter.create(path));

                    if (!tw.next()) {
                        return;
                    }

                    File repoRoot = repo.getWorkTree();
                    Paths.get(repoRoot.getPath(), tw.getPathString());
                    File file = new File(tw.getPathString());
                    LockFile lock = new LockFile(file);
                    lock.unlock();
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error while unlocking file for site: " + site + " path: " + path, e);
        }
    }

    /**
     * bootstrap the repository
     */
    public void bootstrap() throws Exception {
        logger.debug("Bootstrap global repository.");

        boolean bootstrapRepo = Boolean.parseBoolean(studioConfiguration.getProperty(BOOTSTRAP_REPO));
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);

        boolean isCreated = false;

        HierarchicalConfiguration<ImmutableNode> registrationData = studioClusterUtils.getClusterConfiguration();
        if (bootstrapRepo && registrationData != null && !registrationData.isEmpty()) {
            String firstCommitId = getRepoFirstCommitId(StringUtils.EMPTY);
            String localAddress = studioClusterUtils.getClusterNodeLocalAddress();
            List<ClusterMember> clusterNodes = studioClusterUtils.getClusterNodes(localAddress);
            if (StringUtils.isEmpty(firstCommitId)) {
                logger.debug("Creating global repository as cluster clone");
                isCreated = studioClusterUtils.cloneGlobalRepository(clusterNodes);
            } else {
                logger.debug("Global repository exists syncing with cluster siblings");
                isCreated = true;
                Repository repo = helper.getRepository(EMPTY, GLOBAL);
                try (Git git = new Git(repo)) {
                    for (ClusterMember remoteNode : clusterNodes) {
                        syncFromRemote(git, remoteNode);
                    }
                }
            }
        }

        if (bootstrapRepo && !isCreated && helper.createGlobalRepo()) {
            // Copy the global config defaults to the global site
            // Build a path to the bootstrap repo (the repo that ships with Studio)
            String bootstrapFolderPath = this.ctx.getRealPath(FILE_SEPARATOR + BOOTSTRAP_REPO_PATH +
                    FILE_SEPARATOR + BOOTSTRAP_REPO_GLOBAL_PATH);
            Path source = java.nio.file.FileSystems.getDefault().getPath(bootstrapFolderPath);

            logger.info("Bootstrapping with baseline @ " + source.toFile().toString());

            // Copy the bootstrap repo to the global repo
            Path globalConfigPath = helper.buildRepoPath(GLOBAL);
            TreeCopier tc = new TreeCopier(source,
                    globalConfigPath);
            EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
            Files.walkFileTree(source, opts, MAX_VALUE, tc);

            String studioManifestLocation = this.ctx.getRealPath(STUDIO_MANIFEST_LOCATION);
            if (Files.exists(Paths.get(studioManifestLocation))) {
                FileUtils.copyFile(Paths.get(studioManifestLocation).toFile(),
                        Paths.get(globalConfigPath.toAbsolutePath().toString(),
                                studioConfiguration.getProperty(BLUE_PRINTS_PATH), "BLUEPRINTS.MF").toFile());
            }
            Repository globalConfigRepo = helper.getRepository(EMPTY, GLOBAL);
            try (Git git = new Git(globalConfigRepo)) {

                Status status = git.status().call();

                if (status.hasUncommittedChanges() || !status.isClean()) {
                    // Commit everything
                    // TODO: Consider what to do with the commitId in the future
                    AddCommand addCommand = git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS);
                    retryingRepositoryOperationFacade.call(addCommand);
                    CommitCommand commitCommand =
                            git.commit().setMessage(helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE));
                    retryingRepositoryOperationFacade.call(commitCommand);
                }
            } catch (GitAPIException err) {
                logger.error("error creating initial commit for global configuration", err);
            }
        }

        // Create global repository object
        if (!helper.buildGlobalRepo()) {
            logger.error("Failed to create global repository!");
        }
    }

    private void syncFromRemote(Git git, ClusterMember remoteNode) throws CryptoException, GitAPIException,
            IOException, ServiceLayerException {
        if (generalLockService.tryLock(GLOBAL_REPOSITORY_GIT_LOCK)) {
            try {
                final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                PullCommand pullCommand = git.pull();
                pullCommand.setRemote(remoteNode.getGitRemoteName());
                pullCommand = studioClusterUtils.configureAuthenticationForCommand(remoteNode, pullCommand, tempKey);
                pullCommand.call();

                Files.delete(tempKey);
            } finally {
                generalLockService.unlock(GLOBAL_REPOSITORY_GIT_LOCK);
            }
        } else {
            logger.debug("Failed to get lock " + GLOBAL_REPOSITORY_GIT_LOCK);
        }
    }

    @Override
    public boolean deleteSite(String site) {
        boolean toReturn;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                synchronized (repository) {
                    Repository publishedRepository = helper.getRepository(site, PUBLISHED);
                    if (publishedRepository != null) {
                        synchronized (publishedRepository) {
                            toReturn = helper.deleteSiteGitRepo(site);
                        }
                    } else {
                        toReturn = helper.deleteSiteGitRepo(site);
                    }
                }
            } else {
                Path sitePath = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                        studioConfiguration.getProperty(SITES_REPOS_PATH), site);
                try {
                    FileUtils.deleteDirectory(sitePath.toFile());
                    toReturn = true;
                } catch (IOException e) {
                    logger.error("Error while deleting site " + site, e);
                    toReturn = false;
                }
            }
        } catch (CryptoException e) {
            logger.error("Error while deleting site " + site, e);
            toReturn = false;
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    @Override
    public void initialPublish(String site, String sandboxBranch, String environment, String author, String comment)
            throws DeploymentException, CryptoException {
        String gitLockKey = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
        Repository repo = helper.getRepository(site, PUBLISHED);
        String commitId = EMPTY;

        String sandboxBranchName = sandboxBranch;
        if (StringUtils.isEmpty(sandboxBranchName)) {
            sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
        }
        generalLockService.lock(gitLockKey);
        synchronized (repo) {

            try (Git git = new Git(repo)) {

                // fetch "origin/master"
                logger.debug("Fetch from sandbox for site " + site);
                git.fetch().call();

                // checkout master and pull from sandbox
                logger.debug("Checkout published/master branch for site " + site);
                try {

                    CheckoutCommand checkoutCommand = git.checkout().setName(sandboxBranchName);
                    retryingRepositoryOperationFacade.call(checkoutCommand);
                    PullCommand pullCommand = git.pull()
                            .setRemote(DEFAULT_REMOTE_NAME)
                            .setRemoteBranchName(sandboxBranchName)
                            .setStrategy(THEIRS);
                    retryingRepositoryOperationFacade.call(pullCommand);
                } catch (RefNotFoundException e) {
                    logger.error("Failed to checkout published master and to pull content from sandbox for site "
                            + site, e);
                    throw new DeploymentException("Failed to checkout published master and to pull content from " +
                            "sandbox for site " + site);
                }

                // checkout environment branch
                logger.debug("Checkout environment branch " + environment + " for site " + site);
                try {
                    CheckoutCommand checkoutCommand =
                            git.checkout().setCreateBranch(true).setForce(true).setStartPoint(sandboxBranchName)
                            .setUpstreamMode(TRACK)
                            .setName(environment);
                    retryingRepositoryOperationFacade.call(checkoutCommand);
                } catch (RefNotFoundException e) {
                    logger.info("Not able to find branch " + environment + " for site " + site +
                            ". Creating new branch");
                }

                // tag
                PersonIdent authorIdent = helper.getAuthorIdent(author);
                ZonedDateTime publishDate = ZonedDateTime.now(UTC);
                String tagName = publishDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssSSSX")) +
                        "_published_on_" + publishDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssSSSX"));
                TagCommand tagCommand = git.tag().setTagger(authorIdent).setName(tagName).setMessage(comment);
                retryingRepositoryOperationFacade.call(tagCommand);
            } catch (Exception e) {
                logger.error("Error when publishing site " + site + " to environment " + environment, e);
                throw new DeploymentException("Error when publishing site " + site + " to environment " +
                        environment + " [commit ID = " + commitId + "]");
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }

    }

    @Override
    public String getRepoLastCommitId(final String site) {
        String toReturn = EMPTY;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                synchronized (repository) {
                    Repository repo = helper.getRepository(site, SANDBOX);
                    ObjectId commitId = repo.resolve(HEAD);
                    if (commitId != null) {
                        toReturn = commitId.getName();
                    }
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error getting last commit ID for site " + site, e);
        }
        return toReturn;
    }

    @Override
    public String getRepoFirstCommitId(final String site) {
        String toReturn = EMPTY;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                synchronized (repository) {
                    Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
                    if (repo != null) {
                        try (RevWalk rw = new RevWalk(repo)) {
                            ObjectId head = repo.resolve(HEAD);
                            if (head != null) {
                                RevCommit root = rw.parseCommit(head);
                                rw.sort(REVERSE);
                                rw.markStart(root);
                                ObjectId first = rw.next();
                                toReturn = first.getName();
                                logger.debug("getRepoFirstCommitId for site: " + site + " First commit ID: " + toReturn);
                            }
                        }
                    }
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error getting first commit ID for site " + site, e);
        }
        return toReturn;
    }

    @Override
    public List<String> getEditCommitIds(String site, String path, String commitIdFrom, String commitIdTo) {
        List<String> commitIds = new ArrayList<String>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(site, SANDBOX)) {
                // Get the sandbox repo, and then get a reference to the commitId we received and another for head
                Repository repo = helper.getRepository(site, SANDBOX);
                if (StringUtils.isEmpty(commitIdFrom)) {
                    commitIdFrom = getRepoFirstCommitId(site);
                }
                if (StringUtils.isEmpty(commitIdTo)) {
                    commitIdTo = getRepoLastCommitId(site);
                }
                ObjectId objCommitIdFrom = repo.resolve(commitIdFrom);
                ObjectId objCommitIdTo = repo.resolve(commitIdTo);

                try (Git git = new Git(repo)) {

                    // If the commitIdFrom is the same as commitIdTo, there is nothing to calculate, otherwise,
                    // let's do it
                    if (!objCommitIdFrom.equals(objCommitIdTo)) {
                        // Compare HEAD with commitId we're given
                        // Get list of commits between commitId and HEAD in chronological order

                        // Get the log of all the commits between commitId and head
                        Iterable<RevCommit> commits = git.log()
                                .addPath(helper.getGitPath(path))
                                .addRange(objCommitIdFrom, objCommitIdTo)
                                .call();

                        // Reverse orders of commits
                        Iterator<RevCommit> iterator = commits.iterator();
                        while (iterator.hasNext()) {

                            RevCommit commit = iterator.next();
                            commitIds.add(0, commit.getId().getName());
                        }
                    }
                } catch (GitAPIException e) {
                    logger.error("Error getting commit ids for site " + site + " and path " + path +
                            " from commit ID: " + commitIdFrom + " to commit ID: " + commitIdTo, e);
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error getting operations for site " + site + " and path " + path +
                    " from commit ID: " + commitIdFrom + " to commit ID: " + commitIdTo, e);
        }
        return commitIds;
    }

    @Override
    public void insertFullGitLog(String siteId, int processed) {
        List<GitLog> gitLogs = new ArrayList<>();
        try {
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            synchronized (helper.getRepository(siteId, SANDBOX)) {
                Repository repo = helper.getRepository(siteId, SANDBOX);
                try (Git git = new Git(repo)) {
                    Iterable<RevCommit> logs = git.log().call();
                    for (RevCommit rev : logs) {
                        GitLog gitLog = new GitLog();
                        gitLog.setCommitId(rev.getId().getName());
                        gitLog.setProcessed(processed);
                        gitLog.setSiteId(siteId);
                        gitLogs.add(gitLog);
                    }
                }
            }
        } catch (GitAPIException | CryptoException e) {
                logger.error("Error getting full git log for site " + siteId, e);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("gitLogs", gitLogs);
        params.put("processed", 1);
        gitLogDao.insertGitLogList(params);
    }


    @RetryingOperation
    @Override
    public void deleteGitLogForSite(String siteId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        gitLogDao.deleteGitLogForSite(params);
    }

    @Override
    public boolean createSitePushToRemote(String siteId, String remoteName, String remoteUrl, String authenticationType,
                                          String remoteUsername, String remotePassword, String remoteToken,
                                          String remotePrivateKey, boolean createAsOrphan)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, RemoteRepositoryNotBareException, ServiceLayerException {
        boolean toRet = true;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            try (Repository repo = helper.getRepository(siteId, SANDBOX)) {
                try (Git git = new Git(repo)) {
                    boolean pkauth = false;
                    final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                    PushCommand pushCommand = git.push();
                    switch (authenticationType) {
                        case NONE:
                            logger.debug("No authentication");
                            break;
                        case BASIC:
                            logger.debug("Basic authentication");
                            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(remoteUsername,
                                    remotePassword));
                            break;
                        case TOKEN:
                            logger.debug("Token based authentication");
                            pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(remoteToken, EMPTY));
                            break;
                        case PRIVATE_KEY:
                            logger.debug("Private key authentication");

                            tempKey.toFile().deleteOnExit();
                            pushCommand.setTransportConfigCallback(new TransportConfigCallback() {
                                @Override
                                public void configure(Transport transport) {
                                    SshTransport sshTransport = (SshTransport) transport;
                                    sshTransport.setSshSessionFactory(getSshSessionFactory(remotePrivateKey, tempKey));
                                }
                            });
                            pkauth = true;
                            break;
                        default:
                            throw new ServiceLayerException("Unsupported authentication type " + authenticationType);
                    }

                    logger.debug("Push site " + siteId + " to remote repository " + remoteName + "(" + remoteUrl + ")");
                    pushCommand.setPushAll().setRemote(remoteName);
                    Iterable<PushResult> result = retryingRepositoryOperationFacade.call(pushCommand);
                    if (pkauth) Files.delete(tempKey);

                    logger.debug("Check push result to verify it was success");
                    Iterator<PushResult> resultIter = result.iterator();
                    if (resultIter.hasNext()) {
                        PushResult pushResult = resultIter.next();
                        Iterator<RemoteRefUpdate> remoteRefUpdateIterator = pushResult.getRemoteUpdates().iterator();
                        if (remoteRefUpdateIterator.hasNext()) {
                            RemoteRefUpdate update = remoteRefUpdateIterator.next();
                            if (update.getStatus().equals(REJECTED_NONFASTFORWARD)) {
                                logger.error("Remote repository: " + remoteName + " (" + remoteUrl +
                                        ") is not bare repository");
                                throw new RemoteRepositoryNotBareException("Remote repository: " + remoteName +
                                        " (" + remoteUrl + ") is not bare repository");
                            }
                        }
                    }
                } catch (InvalidRemoteException e) {
                    logger.error("Invalid remote repository: " + remoteName + " (" + remoteUrl + ")", e);
                    throw new InvalidRemoteRepositoryException("Invalid remote repository: " + remoteName + " (" +
                            remoteUrl + ")");
                } catch (TransportException e) {
                    if (StringUtils.endsWithIgnoreCase(e.getMessage(), "not authorized")) {
                        logger.error("Bad credentials or read only repository: " + remoteName +
                                " (" + remoteUrl + ")", e);
                        throw new InvalidRemoteRepositoryCredentialsException("Bad credentials or read only repository: " +
                                remoteName + " (" + remoteUrl + ") for username " + remoteUsername, e);
                    } else {
                        logger.error("Remote repository not found: " + remoteName + " (" + remoteUrl + ")", e);
                        throw new RemoteRepositoryNotFoundException("Remote repository not found: " + remoteName + " (" +
                                remoteUrl + ")");
                    }
                } catch (ClassCastException e) {
                    logger.error("Wrong protocol used to access repository: " + remoteName +
                            " (" + remoteUrl + ")", e);
                    throw new InvalidRemoteRepositoryCredentialsException("Wrong protocol used to access repository: " +
                            remoteName + " (" + remoteUrl + ")", e);
                } catch (ServiceLayerException | IOException  e) {
                    logger.error("Failed to push newly created site " + siteId + " to remote repository " +
                            remoteUrl, e);
                    throw new ServiceLayerException(e);
                }
            }
        } catch (GitAPIException | CryptoException e) {
                logger.error("Failed to push newly created site " + siteId + " to remote repository " +
                        remoteUrl, e);
                toRet = false;
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toRet;
    }

    @Override
    public boolean addRemote(String siteId, String remoteName, String remoteUrl,
                             String authenticationType, String remoteUsername, String remotePassword,
                             String remoteToken, String remotePrivateKey)
            throws InvalidRemoteUrlException, ServiceLayerException {
        boolean isValid = false;
        try {
            logger.debug("Add remote " + remoteName + " to the sandbox repo for the site " + siteId);
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(siteId, SANDBOX);
            try (Git git = new Git(repo)) {

                Config storedConfig = repo.getConfig();
                Set<String> remotes = storedConfig.getSubsections("remote");

                if (remotes.contains(remoteName)) {
                    throw new RemoteAlreadyExistsException(remoteName);
                }

                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName(remoteName);
                remoteAddCommand.setUri(new URIish(remoteUrl));
                retryingRepositoryOperationFacade.call(remoteAddCommand);

                try {
                    isValid = isRemoteValid(git, remoteName, authenticationType, remoteUsername, remotePassword,
                            remoteToken, remotePrivateKey);
                } finally {
                    if (!isValid) {
                        RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
                        remoteRemoveCommand.setRemoteName(remoteName);
                        retryingRepositoryOperationFacade.call(remoteRemoveCommand);

                        List<Ref> resultRemoteBranches = git.branchList()
                                .setListMode(ListBranchCommand.ListMode.REMOTE)
                                .call();

                        List<String> branchesToDelete = new ArrayList<String>();
                        for (Ref remoteBranchRef : resultRemoteBranches) {
                            if (remoteBranchRef.getName().startsWith(Constants.R_REMOTES + remoteName)) {
                                branchesToDelete.add(remoteBranchRef.getName());
                            }
                        }
                        if (CollectionUtils.isNotEmpty(branchesToDelete)) {
                            DeleteBranchCommand delBranch = git.branchDelete();
                            String[] array = new String[branchesToDelete.size()];
                            delBranch.setBranchNames(branchesToDelete.toArray(array));
                            delBranch.setForce(true);
                            retryingRepositoryOperationFacade.call(delBranch);
                        }
                    }
                }

            } catch (URISyntaxException | ClassCastException e) {
                logger.error("Remote URL is invalid " + remoteUrl, e);
                throw new InvalidRemoteUrlException("Remote URL is invalid " + remoteUrl, e);
            } catch (GitAPIException | IOException e) {
                logger.error("Error while adding remote " + remoteName + " (url: " + remoteUrl + ") for site " +
                        siteId, e);
                throw new ServiceLayerException("Error while adding remote " + remoteName + " (url: " + remoteUrl +
                        ") for site " + siteId, e);
            }

            if (isValid) {
                insertRemoteToDb(siteId, remoteName, remoteUrl, authenticationType, remoteUsername, remotePassword,
                        remoteToken, remotePrivateKey);
            }
        } catch (CryptoException e) {
            throw new ServiceLayerException(e);
        }
        return isValid;
    }

    private boolean isRemoteValid(Git git, String remote, String authenticationType,
                                  String remoteUsername, String remotePassword, String remoteToken,
                                  String remotePrivateKey)
            throws CryptoException, IOException, ServiceLayerException, GitAPIException {
        LsRemoteCommand lsRemoteCommand = git.lsRemote();
        lsRemoteCommand.setRemote(remote);
        switch (authenticationType) {
            case NONE:
                logger.debug("No authentication");
                break;
            case BASIC:
                logger.debug("Basic authentication");
                lsRemoteCommand.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(remoteUsername, remotePassword));
                break;
            case TOKEN:
                logger.debug("Token based authentication");
                lsRemoteCommand.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(remoteToken, EMPTY));
                break;
            case PRIVATE_KEY:
                logger.debug("Private key authentication");
                final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                tempKey.toFile().deleteOnExit();
                lsRemoteCommand.setTransportConfigCallback(
                        new TransportConfigCallback() {
                            @Override
                            public void configure(Transport transport) {
                                SshTransport sshTransport = (SshTransport) transport;
                                sshTransport.setSshSessionFactory(getSshSessionFactory(remotePrivateKey, tempKey));
                            }
                        });
                Files.delete(tempKey);
                break;
            default:
                throw new ServiceLayerException("Unsupported authentication type " + authenticationType);
        }
        lsRemoteCommand.call();
        return true;
    }

    private void insertRemoteToDb(String siteId, String remoteName, String remoteUrl,
                                  String authenticationType, String remoteUsername, String remotePassword,
                                  String remoteToken, String remotePrivateKey) throws CryptoException {
        logger.debug("Inserting remote " + remoteName + " for site " + siteId + " into database.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        params.put("remoteUrl", remoteUrl);
        params.put("authenticationType", authenticationType);
        params.put("remoteUsername", remoteUsername);

        if (StringUtils.isNotEmpty(remotePassword)) {
            logger.debug("Encrypt password before inserting to database");
            String hashedPassword = encryptor.encrypt(remotePassword);
            params.put("remotePassword", hashedPassword);
        } else {
            params.put("remotePassword", remotePassword);
        }
        if (StringUtils.isNotEmpty(remoteToken)) {
            logger.debug("Encrypt token before inserting to database");
            String hashedToken = encryptor.encrypt(remoteToken);
            params.put("remoteToken", hashedToken);
        } else {
            params.put("remoteToken", remoteToken);
        }
        if (StringUtils.isNotEmpty(remotePrivateKey)) {
            logger.debug("Encrypt private key before inserting to database");
            String hashedPrivateKey = encryptor.encrypt(remotePrivateKey);
            params.put("remotePrivateKey", hashedPrivateKey);
        } else {
            params.put("remotePrivateKey", remotePrivateKey);
        }

        logger.debug("Insert site remote record into database");
        remoteRepositoryDAO.insertRemoteRepository(params);
        params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);
        if (remoteRepository != null) {
            insertClusterRemoteRepository(remoteRepository);
        }
    }

    public void insertClusterRemoteRepository(RemoteRepository remoteRepository) {
        HierarchicalConfiguration<ImmutableNode> registrationData =
                studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
        if (registrationData != null && !registrationData.isEmpty()) {
            String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
            ClusterMember member = clusterDao.getMemberByLocalAddress(localAddress);
            if (member != null) {
                retryingOperationFacade.addClusterRemoteRepository(member.getId(), remoteRepository.getId());
            }
        }
    }

    @RetryingOperation
    @Override
    public void removeRemoteRepositoriesForSite(String siteId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        remoteRepositoryDAO.deleteRemoteRepositoriesForSite(params);
    }

    @Override
    public List<RemoteRepositoryInfoTO> listRemote(String siteId, String sandboxBranch)
            throws ServiceLayerException, CryptoException {
        List<RemoteRepositoryInfoTO> res = new ArrayList<RemoteRepositoryInfoTO>();
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
        try (Repository repo = helper.getRepository(siteId, SANDBOX)) {

            try (Git git = new Git(repo)) {
                List<RemoteConfig> resultRemotes = git.remoteList().call();
                if (CollectionUtils.isNotEmpty(resultRemotes)) {
                    for (RemoteConfig conf : resultRemotes) {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("siteId", siteId);
                        params.put("remoteName", conf.getName());
                        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);
                        if (remoteRepository != null) {
                            switch (remoteRepository.getAuthenticationType()) {
                                case NONE:
                                    logger.debug("No authentication");
                                    git.fetch().setRemote(conf.getName()).call();
                                    break;
                                case BASIC:
                                    logger.debug("Basic authentication");
                                    String hashedPassword = remoteRepository.getRemotePassword();
                                    String password = encryptor.decrypt(hashedPassword);
                                    git.fetch().setRemote(conf.getName()).setCredentialsProvider(
                                            new UsernamePasswordCredentialsProvider(
                                                    remoteRepository.getRemoteUsername(), password)).call();
                                    break;
                                case TOKEN:
                                    logger.debug("Token based authentication");
                                    String hashedToken = remoteRepository.getRemoteToken();
                                    String remoteToken = encryptor.decrypt(hashedToken);
                                    git.fetch().setRemote(conf.getName()).setCredentialsProvider(
                                            new UsernamePasswordCredentialsProvider(remoteToken, EMPTY)).call();
                                    break;
                                case PRIVATE_KEY:
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
                                                    sshTransport.setSshSessionFactory(
                                                            getSshSessionFactory(privateKey, tempKey));
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
                            .setListMode(REMOTE)
                            .call();
                    Map<String, List<String>> remoteBranches = new HashMap<String, List<String>>();
                    for (Ref remoteBranchRef : resultRemoteBranches) {
                        String branchFullName = remoteBranchRef.getName().replace(R_REMOTES, "");
                        String remotePart = EMPTY;
                        String branchNamePart = EMPTY;
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
                        RemoteRepositoryInfoTO rri = new RemoteRepositoryInfoTO();
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
                        rri.setPush_url(sbPushUrl.toString());
                        res.add(rri);
                    }
                }
            } catch (GitAPIException | CryptoException | IOException e) {
                logger.error("Error getting remote repositories for site " + siteId, e);
            }
        }
        return res;
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException, CryptoException {
        logger.debug("Get remote data from database for remote " + remoteName + " and site " + siteId);
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);

        logger.debug("Prepare push command.");
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            Iterable<PushResult> pushResultIterable = null;
            PushCommand pushCommand = git.push();
            logger.debug("Set remote " + remoteName);
            pushCommand.setRemote(remoteRepository.getRemoteName());
            logger.debug("Set branch to be " + remoteBranch);
            RefSpec r = new RefSpec();
            r = r.setSourceDestination(Constants.R_HEADS + repo.getBranch(),
                    Constants.R_HEADS +  remoteBranch);
            pushCommand.setRefSpecs(r);
            switch (remoteRepository.getAuthenticationType()) {
                case NONE:
                    logger.debug("No authentication");
                    pushResultIterable = pushCommand.call();
                    break;
                case BASIC:
                    logger.debug("Basic authentication");
                    String hashedPassword = remoteRepository.getRemotePassword();
                    String password = encryptor.decrypt(hashedPassword);
                    pushCommand.setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(remoteRepository.getRemoteUsername(), password));

                    pushResultIterable = pushCommand.call();
                    break;
                case TOKEN:
                    logger.debug("Token based authentication");
                    String hashedToken = remoteRepository.getRemoteToken();
                    String remoteToken = encryptor.decrypt(hashedToken);
                    pushCommand.setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(remoteToken, EMPTY));
                    pushResultIterable = pushCommand.call();
                    break;
                case PRIVATE_KEY:
                    logger.debug("Private key authentication");
                    final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(),".tmp");
                    String hashedPrivateKey = remoteRepository.getRemotePrivateKey();
                    String privateKey = encryptor.decrypt(hashedPrivateKey);
                    tempKey.toFile().deleteOnExit();
                    pushCommand.setTransportConfigCallback(new TransportConfigCallback() {
                        @Override
                        public void configure(Transport transport) {
                            SshTransport sshTransport = (SshTransport)transport;
                            sshTransport.setSshSessionFactory(getSshSessionFactory(privateKey, tempKey));
                        }
                    });
                    pushResultIterable = retryingRepositoryOperationFacade.call(pushCommand);
                    Files.delete(tempKey);
                    break;
                default:
                    throw new ServiceLayerException("Unsupported authentication type " +
                            remoteRepository.getAuthenticationType());
            }

            boolean toRet = true;
            List<RemoteRefUpdate.Status> failure = Arrays.asList(REJECTED_NODELETE, REJECTED_NONFASTFORWARD,
                    REJECTED_REMOTE_CHANGED, REJECTED_OTHER_REASON);
            for (PushResult pushResult : pushResultIterable) {
                Collection<RemoteRefUpdate> updates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate remoteRefUpdate : updates) {
                    toRet = toRet && !failure.contains(remoteRefUpdate.getStatus());
                    if (!toRet) break;
                }
                if (!toRet) break;
            }
            return toRet;
        } catch (InvalidRemoteException e) {
            logger.error("Remote is invalid " + remoteName, e);
            throw new InvalidRemoteUrlException();
        } catch (IOException | JGitInternalException | GitAPIException | CryptoException e) {
            logger.error("Error while pushing to remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
            throw new ServiceLayerException("Error while pushing to remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
        }
    }

    @Override
    public boolean pullFromRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException, CryptoException {
        logger.debug("Get remote data from database for remote " + remoteName + " and site " + siteId);
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);

        logger.debug("Prepare pull command");
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            PullResult pullResult = null;
            PullCommand pullCommand = git.pull();
            logger.debug("Set remote " + remoteName);
            pullCommand.setRemote(remoteRepository.getRemoteName());
            logger.debug("Set branch to be " + remoteBranch);
            pullCommand.setRemoteBranchName(remoteBranch);
            switch (remoteRepository.getAuthenticationType()) {
                case NONE:
                    logger.debug("No authentication");
                    pullResult = pullCommand.call();
                    break;
                case BASIC:
                    logger.debug("Basic authentication");
                    String hashedPassword = remoteRepository.getRemotePassword();
                    String password = encryptor.decrypt(hashedPassword);
                    pullCommand.setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(remoteRepository.getRemoteUsername(), password));

                    pullResult = pullCommand.call();
                    break;
                case TOKEN:
                    logger.debug("Token based authentication");
                    String hashedToken = remoteRepository.getRemoteToken();
                    String token = encryptor.decrypt(hashedToken);
                    pullCommand.setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(token, EMPTY));
                    pullResult = pullCommand.call();
                    break;
                case PRIVATE_KEY:
                    logger.debug("Private key authentication");
                    final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                    String hashedPrivateKey = remoteRepository.getRemotePrivateKey();
                    String privateKey = encryptor.decrypt(hashedPrivateKey);
                    tempKey.toFile().deleteOnExit();
                    pullCommand.setTransportConfigCallback(new TransportConfigCallback() {
                        @Override
                        public void configure(Transport transport) {
                            SshTransport sshTransport = (SshTransport)transport;
                            sshTransport.setSshSessionFactory(getSshSessionFactory(privateKey, tempKey));
                        }
                    });
                    pullResult = retryingRepositoryOperationFacade.call(pullCommand);
                    Files.delete(tempKey);
                    break;
                default:
                    throw new ServiceLayerException("Unsupported authentication type " +
                            remoteRepository.getAuthenticationType());
            }
            return pullResult != null ? pullResult.isSuccessful() : false;
        } catch (InvalidRemoteException e) {
            logger.error("Remote is invalid " + remoteName, e);
            throw new InvalidRemoteUrlException();
        } catch (GitAPIException e) {
            logger.error("Error while pulling from remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
            throw new ServiceLayerException("Error while pulling from remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
        } catch (CryptoException | IOException e) {
            throw new ServiceLayerException(e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    private SshSessionFactory getSshSessionFactory(String privateKey, final Path tempKey)  {
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

    @Override
    public boolean isFolder(String siteId, String path) {
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Path p = Paths.get(helper.buildRepoPath(StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX, siteId)
                    .toAbsolutePath().toString(), path);
            File file = p.toFile();
            return file.isDirectory();
        } catch (CryptoException e) {
            logger.error("Unexpected error checking if path is folder for site: " + siteId + " and path: " + path);
            return false;
        }
    }

    @Override
    public void resetStagingRepository(String siteId) throws ServiceLayerException, CryptoException {
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
        Repository repo = helper.getRepository(siteId, PUBLISHED);
        String stagingName = servicesConfig.getStagingEnvironment(siteId);
        String liveName = servicesConfig.getLiveEnvironment(siteId);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        synchronized (repo) {
            try (Git git = new Git(repo)) {
                logger.debug("Checkout live first becuase it is not allowed to delete checkedout branch");
                CheckoutCommand checkoutCommand = git.checkout().setName(liveName);
                retryingRepositoryOperationFacade.call(checkoutCommand);
                logger.debug("Delete staging branch in order to reset it for site: " + siteId);
                DeleteBranchCommand deleteBranchCommand =
                        git.branchDelete().setBranchNames(stagingName).setForce(true);
                retryingRepositoryOperationFacade.call(deleteBranchCommand);

                logger.debug("Create new branch for staging with live HEAD as starting point");
                CreateBranchCommand createBranchCommand = git.branchCreate()
                        .setName(stagingName)
                        .setStartPoint(liveName);
                retryingRepositoryOperationFacade.call(createBranchCommand);
            } catch (GitAPIException e) {
                logger.error("Error while reseting staging environment for site: " + siteId);
                throw new ServiceLayerException(e);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }
    }

    @Override
    public void reloadRepository(String siteId) {
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            helper.removeSandbox(siteId);
            helper.getRepository(siteId, SANDBOX);
        } catch (CryptoException e) {
            logger.error("Unexpected error reloading repository for site " + siteId);
        }
    }

    protected void cleanup(String siteId, GitRepositories repository) {
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository sandbox = helper.getRepository(siteId, repository);
            try (Git git = new Git(sandbox)) {
                GarbageCollectCommand garbageCollectCommand = git.gc();
                retryingRepositoryOperationFacade.call(garbageCollectCommand);
            }
        } catch (Exception e) {
            logger.warn("Error cleaning up repository for site " + siteId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupRepositories(final String siteId) {
        if(StringUtils.isEmpty(siteId)) {
            logger.info("Cleaning up global repository");
            String gitLockKey = GLOBAL_REPOSITORY_GIT_LOCK;
            generalLockService.lock(gitLockKey);
            try {
                cleanup(siteId, GLOBAL);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        } else {
            logger.info("Cleaning up repositories for site {0}", siteId);
            String gitLockKeySandbox = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
            String gitLockKeyPublished = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
            generalLockService.lock(gitLockKeySandbox);
            try {
                cleanup(siteId, SANDBOX);
            } finally {
                generalLockService.unlock(gitLockKeySandbox);
            }
            generalLockService.lock(gitLockKeyPublished);
            try {
                cleanup(siteId, PUBLISHED);
            } finally {
                generalLockService.unlock(gitLockKeyPublished);
            }
        }
    }

    public void setServletContext(ServletContext ctx) {
        this.ctx = ctx;
    }

    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public GitLogDAO getGitLogDao() {
        return gitLogDao;
    }

    public void setGitLogDao(GitLogDAO gitLogDao) {
        this.gitLogDao = gitLogDao;
    }

    public RemoteRepositoryDAO getRemoteRepositoryDAO() {
        return remoteRepositoryDAO;
    }

    public void setRemoteRepositoryDAO(RemoteRepositoryDAO remoteRepositoryDAO) {
        this.remoteRepositoryDAO = remoteRepositoryDAO;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public RetryingOperationFacade getRetryingOperationFacade() {
        return retryingOperationFacade;
    }

    public void setRetryingOperationFacade(RetryingOperationFacade retryingOperationFacade) {
        this.retryingOperationFacade = retryingOperationFacade;
    }

    public RetryingRepositoryOperationFacade getRetryingRepositoryOperationFacade() {
        return retryingRepositoryOperationFacade;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public StudioClusterUtils getStudioClusterUtils() {
        return studioClusterUtils;
    }

    public void setStudioClusterUtils(StudioClusterUtils studioClusterUtils) {
        this.studioClusterUtils = studioClusterUtils;
    }
}