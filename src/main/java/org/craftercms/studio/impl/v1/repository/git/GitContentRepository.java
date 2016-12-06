/*
 * Crafter Studio Web-content authoring solution
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

import com.google.gdata.data.Person;
import com.google.gdata.util.common.base.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.User;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.SafeBufferedOutputStream;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class GitContentRepository implements ContentRepository, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);

    Map<String, Repository> sandboxes = new HashMap<>();
    Map<String, Repository> published = new HashMap<>();
    Repository global = null; // TODO: SJ: TODAY: Must get this initialized in an init or bootstrap

    // TODO: SJ: Pull these strings from properties
    private static String[] IGNORE_FILES = new String[] { ".keep", ".DS_Store" };

    // TODO: SJ: Inject the security provider to get information needed for writing and version creation (like author)

    @Override
    public boolean contentExists(String site, String path) {
        Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
            site);
        try {
            RevTree tree = getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, getGitPath(path), tree);
            if (tw != null && tw.getObjectId(0) != null) {
                return true;
            }
        } catch (IOException e) {
            logger.info("Content not found for site: " + site + " path: " + path, e);
        }
        return false;
    }

    @Override
    public InputStream getContent(String site, String path) throws ContentNotFoundException {
        InputStream toRet = null;
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);

            RevTree tree = getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, getGitPath(path), tree);

            ObjectId id = tw.getObjectId(0);
            ObjectLoader objectLoader = repo.open(id);
            toRet = objectLoader.openStream();

        } catch (IOException e) {
            logger.error("Error while getting content for file at site: " + site + " path: " + path, e);
        }
        return toRet;
    }

    @Override
    public String writeContent(String site, String path, InputStream content) {
        // Write content to git and commit it
        String result = null;

        Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX, site);

        if (repo != null) {
            // TODO: SJ: TODAY: handle errors and exceptions
            if (writeFile(repo, site, path, content))
                result = commitFile(repo, site, path);
        }

        return result;
    }

    @Override
    public boolean createFolder(String site, String path, String name) {
        boolean success = true;

        try {
            Repository repo = getRepository(GitRepositories.SANDBOX, site);
            RevTree tree = getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, getGitPath(path), tree);

            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path folderPath = Paths.get(fs.normalize(repoRoot.getPath()), tw.getPathString(), name);
            Path keepPath = Paths.get(folderPath.toString(), ".keep");
            Files.createDirectories(folderPath);
            File keep = Files.createFile(keepPath).toFile();
            String gitPath = Paths.get(tw.getPathString(), name, ".keep").toString();
            Git git = new Git(repo);
            git.add()
                    .addFilepattern(gitPath)
                    .call();

            PersonIdent currentUserIdent = getCurrentUserIdent();

            RevCommit commit = git.commit()
                    .setOnly(getGitPath(gitPath))
                    .setMessage(StringUtils.EMPTY)
                    .setCommitter(currentUserIdent)
                    .call();
        } catch (IOException | GitAPIException e) {
            logger.error("Error creating folder " + name + " for site " + site + " at path " + path, e);
            success = false;
        }

        return success;
    }

    @Override
    public boolean deleteContent(String site, String path) {
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            Git git = new Git(repo);
            git.rm()
                    .addFilepattern(getGitPath(path))
                    .setCached(false)
                    .call();

            RevCommit commit = git.commit()
                    .setOnly(getGitPath(path))
                    .setMessage(StringUtils.EMPTY)
                    .call();
            return true;
        } catch (GitAPIException e) {
            logger.error("Error while deleting content for site: " + site + " path: " + path, e);
        }
        return false;
    }

    @Override
    public boolean moveContent(String site, String fromPath, String toPath) {
        return moveContent(site, fromPath, toPath, null);
    }

    @Override
    public boolean moveContent(String site, String fromPath, String toPath, String newName) {
        boolean success = true;

        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            String gitFromPath = getGitPath(fromPath);
            RevTree fromTree = getTree(repo);
            TreeWalk fromTw = TreeWalk.forPath(repo, gitFromPath, fromTree);

            String gitToPath = getGitPath(toPath);
            RevTree toTree = getTree(repo);
            TreeWalk toTw = TreeWalk.forPath(repo, gitToPath, toTree);

            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path sourcePath = null;
            if (fromTw == null) {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), gitFromPath);
            } else {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), fromTw.getPathString());
            }
            Path targetPath = null;
            if (toTw == null) {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), gitToPath);
            } else {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), toTw.getPathString());
            }

            File source = sourcePath.toFile();
            File destDir = targetPath.toFile();
            File dest = destDir;
            if (StringUtils.isNotEmpty(newName)) {
                dest = new File(destDir, newName);
            }
            if (source.isDirectory()) {
                File[] dirList = source.listFiles();
                for (File file : dirList) {
                    if (file.isDirectory()) {
                        FileUtils.moveDirectoryToDirectory(file, dest, true);
                    } else {
                        FileUtils.moveFileToDirectory(file, dest, true);
                    }
                }
                source.delete();
            } else {
                if (dest.isDirectory()) {
                    FileUtils.moveFileToDirectory(source, dest, true);
                } else {
                    source.renameTo(dest);
                }
            }
            Git git = new Git(repo);
            git.add()
                    .addFilepattern(gitToPath)
                    .call();
            git.rm()
                    .addFilepattern(gitFromPath)
                    .call();
            RevCommit commit = git.commit()
                    .setOnly(gitFromPath)
                    .setOnly(gitToPath)
                    .setMessage(StringUtils.EMPTY)
                    .call();
        } catch (IOException | GitAPIException err) {
            // log this error
            logger.error("Error while moving content from {0} to {1}", err, fromPath, toPath);
            success = false;
        }

        return success;
    }

    @Override
    public boolean copyContent(String site, String fromPath, String toPath) {
        boolean success = true;

        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            String gitFromPath = getGitPath(fromPath);
            RevTree fromTree = getTree(repo);
            TreeWalk fromTw = TreeWalk.forPath(repo, gitFromPath, fromTree);

            String gitToPath = getGitPath(toPath);
            RevTree toTree = getTree(repo);
            TreeWalk toTw = TreeWalk.forPath(repo, gitToPath, toTree);

            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path sourcePath = null;
            if (fromTw == null) {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), gitFromPath);
            } else {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), fromTw.getPathString());
            }
            Path targetPath = null;
            if (toTw == null) {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), gitToPath);
            } else {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), toTw.getPathString());
            }
            File sourceFile = sourcePath.toFile();
            if (sourceFile.isDirectory()) {
                FileUtils.copyDirectory(sourceFile, targetPath.toFile());
            } else {
                FileUtils.copyFileToDirectory(sourceFile, targetPath.toFile());
            }
            Git git = new Git(repo);
            git.add()
                    .addFilepattern(gitToPath)
                    .call();
        } catch (IOException | GitAPIException err) {
            // log this error
            logger.error("Error while copping content from {0} to {1}", err, fromPath, toPath);
            success = false;
        }

        return success;
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        final List<RepositoryItem> retItems = new ArrayList<RepositoryItem>();
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            RevTree tree = getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, getGitPath(path), tree);
            if (tw != null) {
                ObjectLoader loader = repo.open(tw.getObjectId(0));
                if (loader.getType() == Constants.OBJ_TREE) {
                    int depth = tw.getDepth();
                    tw.enterSubtree();
                    while (tw.next()) {
                        if (tw.getDepth() == depth + 1) {

                            RepositoryItem item = new RepositoryItem();
                            item.name = tw.getNameString();

                            String visitFolderPath = "/" + tw.getPathString();
                            loader = repo.open(tw.getObjectId(0));
                            item.isFolder = loader.getType() == Constants.OBJ_TREE;
                            int lastIdx = visitFolderPath.lastIndexOf(File.separator + item.name);
                            if (lastIdx > 0) {
                                item.path = visitFolderPath.substring(0, lastIdx);
                            }

                            if (!ArrayUtils.contains(IGNORE_FILES, item.name)) {
                                retItems.add(item);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error while getting children for site: " + site + " path: " + path, e);
        }

        RepositoryItem[] items = new RepositoryItem[retItems.size()];
        items = retItems.toArray(items);
        return items;
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path, boolean ignoreCache) {
        return getContentChildren(site, path);
    }

    @Override
    public VersionTO[] getContentVersionHistory(String site, String path) {
        List<VersionTO> versionHistory = new ArrayList<VersionTO>();
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            ObjectId head = repo.resolve(Constants.HEAD);
            String gitPath = getGitPath(path);
            Git git = new Git(repo);
            Iterable<RevCommit> commits = git.log()
                    .add(head)
                    .addPath(gitPath)
                    .call();
            Iterator<RevCommit> iterator = commits.iterator();
            while (iterator.hasNext()) {
                RevCommit revCommit = iterator.next();
                VersionTO versionTO = new VersionTO();
                versionTO.setVersionNumber(revCommit.getId().toString());
                versionTO.setLastModifier(revCommit.getAuthorIdent().getName());
                versionTO.setLastModifiedDate(new Date(revCommit.getCommitTime()*1000));
                versionTO.setComment(revCommit.getShortMessage());
                versionHistory.add(versionTO);
            }
        } catch (IOException | GitAPIException err) {
            logger.error("error while getting history for content item " + path);
        }
        VersionTO[] toRet = new VersionTO[versionHistory.size()];
        return versionHistory.toArray(toRet);
    }

    @Override
    public String createVersion(String site, String path, boolean majorVersion) {
        return createVersion(site, path, StringUtils.EMPTY, majorVersion);
    }

    @Override
    public String createVersion(String site, String path, String comment, boolean majorVersion) {
        // SJ: Will ignore minor revisions since git handles that via write/commit
        // SJ: Major revisions become git tags
        String toRet = StringUtils.EMPTY;

        if (majorVersion) {
            try {
                // Tag the repository with a date-time based version label
                Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
                        .PUBLISHED, site);

                String gitPath = getGitPath(path);
                Git git = new Git(repo);

                PersonIdent currentUserIdent = getCurrentUserIdent();
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                String versionLabel = dateFormat.format(cal);

                TagCommand tagCommand = git.tag().setName(versionLabel).setMessage(comment).setTagger(currentUserIdent);
                tagCommand.call();

                toRet = versionLabel;
            } catch (GitAPIException err) {
                logger.error("error creating new version for site:  " + site + " path: " + path, err);
            }
        } else {
            logger.error("request to create minor revision ignored for site: " + site + " path: " + path);
        }
        return toRet;

    }

    @Override
    public boolean revertContent(String site, String path, String version, boolean major, String comment) {
        boolean success = false;
        try {
            InputStream versionContent = getContentVersion(site, path, version);
            writeContent(site, path, versionContent);
            createVersion(site, path, major);
            success = true;
        } catch (ContentNotFoundException err) {
            logger.error("error reverting content for site:  " + site + " path: " + path, err);
        }
        return success;
    }

    @Override
    public InputStream getContentVersion(String site, String path, String version) throws ContentNotFoundException {
        InputStream toRet = null;
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            RevTree tree = getTreeForCommit(repo, version);
            TreeWalk tw = TreeWalk.forPath(repo, getGitPath(path), tree);

            ObjectId id = tw.getObjectId(0);
            ObjectLoader objectLoader = repo.open(id);
            toRet = objectLoader.openStream();

        } catch (IOException err) {
            logger.error("Error while getting content for file at site: " + site + " path: " + path, err);
        }
        return toRet;
    }

    @Override
    public Date getModifiedDate(String site, String path) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void lockItem(String site, String path) {
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            TreeWalk tw = new TreeWalk(repo);
            RevTree tree = getTree(repo);
            tw.addTree(tree); // tree ‘0’
            tw.setRecursive(false);
            tw.setFilter(PathFilter.create(path));

            if (!tw.next()) {
                return;
            }

            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path path1 = Paths.get(fs.normalize(repoRoot.getPath()), tw.getPathString());
            File file = new File(tw.getPathString());
            LockFile lock = new LockFile(file, fs); // TODO: fix me: deprecated method
            lock.lock();

            tw.close();

        } catch (IOException e) {
            logger.error("Error while locking file for site: " + site + " path: " + path, e);
        }
    }

    @Override
    public void unLockItem(String site, String path) {
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);
            RevTree tree = getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, getGitPath(path), tree);
            if (tw != null) {
                FS fs = FS.detect();
                File file = new File(tw.getPathString());
                LockFile lock = new LockFile(file, fs);
                lock.unlock();

                tw.close();
            }
        } catch (IOException e) {
            logger.error("Error while locking file for site: " + site + " path: " + path, e);
        }
    }

    private Repository getGlobalConfigurationRepositoryInstance() throws IOException {
        // TODO: SJ: Fix this: We must log and deal with failure here
        Path siteRepoPath = Paths.get(rootPath, "global-configuration", ".git");
        if (Files.exists(siteRepoPath)) {
            return openGitRepository(siteRepoPath);
        } else {
            Files.deleteIfExists(siteRepoPath);
            //return cloneRemoteRepository(siteConfiguration.getGitRepositoryUrl(), siteConfiguration.getLocalRepositoryRoot());
        }
        return null;
    }

    private Repository getSiteRepositoryInstance(String site) throws IOException {

        Path siteRepoPath = Paths.get(rootPath, "sites", site, ".git");
        if (Files.exists(siteRepoPath)) {
            return openGitRepository(siteRepoPath);
        } else {
            Files.deleteIfExists(siteRepoPath);
            //return cloneRemoteRepository(siteConfiguration.getGitRepositoryUrl(), siteConfiguration.getLocalRepositoryRoot());
        }
        return null;
    }

    private Repository openGitRepository(Path repositoryPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
                .setGitDir(repositoryPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();
        return repository;
    }

    private RevTree getTree(Repository repository) throws AmbiguousObjectException, IncorrectObjectTypeException,
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

    private RevTree getTreeForCommit(Repository repository, String commitId) throws AmbiguousObjectException, IncorrectObjectTypeException,
            IOException, MissingObjectException {
        ObjectId commitObjectId = repository.resolve(commitId);


        // a RevWalk allows to walk over commits based on some filtering
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitObjectId);

            // and using commit's tree find the path
            RevTree tree = commit.getTree();
            return tree;
        }
    }

    private String getGitPath(String path) {
        String gitPath = path.replaceAll("/+", "/");
        gitPath = gitPath.replaceAll("^/", "");
        return gitPath;
    }

    public void createPatch() {
        try (Repository repository = getSiteRepositoryInstance("gitreposite")) {
            try (Git git = new Git(repository)) {

                // the diff works on TreeIterators, we prepare two for the two branches
                AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, "refs/remotes/origin/master");
                AbstractTreeIterator newTreeParser = prepareTreeParser(repository, "refs/heads/master");

                // then the procelain diff-command returns a list of diff entries
                List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();
                OutputStream os = new FileOutputStream("/Users/dejanbrkic/gitpatchtest.diff");
                DiffFormatter df = new DiffFormatter(new SafeBufferedOutputStream(os));
                df.setRepository(repository);
                df.format(diff);
                df.flush();
                df.close();
                os.close();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws IOException,
            MissingObjectException,
            IncorrectObjectTypeException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        Ref head = repository.exactRef(ref);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repository.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }

            walk.dispose();

            return oldTreeParser;
        }
    }

    @Override
    public boolean createSiteFromBlueprint(String blueprintName, String siteId) {
        // create git repository for site content
        createSiteRepository(siteId);
        // copy files from blueprint
        copyContentFromBlueprint(blueprintName, siteId);
        // commit everything so it is visible
        createInitialCommit(siteId);

        return true;
    }

    private boolean createSiteRepository(String site) {
        boolean success = true;
        Path siteRepoPath = Paths.get(rootPath, "sites", site);
        try {
            Files.deleteIfExists(siteRepoPath);
            siteRepoPath = Paths.get(siteRepoPath.toAbsolutePath().toString(), ".git");
            Repository repository = FileRepositoryBuilder.create(siteRepoPath.toFile());
            repository.create();
            // TODO: SJ: We need to create two repositories: Sandbox and Published and put them in the Maps
        } catch (IOException e) {
            logger.error("Error while creating repository for site " + site, e);
            success = false;
        }
        return success;
    }

    private void copyContentFromBlueprint(String blueprint, String site) {
        Path siteRepoPath = Paths.get(rootPath, "sites", site);
        Path blueprintPath = Paths.get(rootPath, "global-configuration", "blueprints", blueprint);
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        TreeCopier tc = new TreeCopier(blueprintPath, siteRepoPath);
        try {
            Files.walkFileTree(blueprintPath, opts, Integer.MAX_VALUE, tc);
        } catch (IOException err) {
            logger.error("Error copping files from blueprint", err);
        }
    }

    class TreeCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;

        TreeCopier(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            CopyOption[] options = new CopyOption[0];

            Path newdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newdir, options);
            } catch (FileAlreadyExistsException x) {
                // ignore
            } catch (IOException err) {
                logger.error("Unable to create: %s: %s%n", newdir, err);
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            CopyOption[] options = new CopyOption[] { REPLACE_EXISTING };
            try {
                Files.copy(file, target.resolve(source.relativize(file)), options);
            } catch (IOException err) {
                logger.error("Unable to copy: " + source + " to " + target.resolve(source.relativize(file)), err);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (exc instanceof FileSystemLoopException) {
                logger.error("cycle detected: " + file);
            } else {
                logger.error("Unable to copy: %s: %s%n", file, exc);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return CONTINUE;
        }
    }

    private void createInitialCommit(String site) {
        String toRet = StringUtils.EMPTY;
        try {
            Repository repo = getRepository(StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories.SANDBOX,
                site);

            Git git = new Git(repo);

            Status status = git.status().call();

            if (status.hasUncommittedChanges() || !status.isClean()) {
                DirCache dirCache = git.add().addFilepattern(".").call();
                RevCommit commit = git.commit()
                        .setMessage("initial content")
                        .call();
                toRet = commit.getId().toString();
            }
        } catch (GitAPIException err) {
            logger.error("error creating initial commit for site:  " + site, err);
        }
    }

    // SJ: Helper methods
    private Repository getRepository(GitRepositories gitRepository, String site) {
        Repository repo = null;

        try {
            switch (gitRepository) {
                case SANDBOX:
                    repo = sandboxes.get(site);
                    if (repo == null) {
                        repo = getSiteRepositoryInstance(site);
                        if (repo != null) {
                            sandboxes.put(site, repo);
                        }
                    }
                    break;
                case PUBLISHED:
                    repo = published.get(site);
                    if (repo == null) {
                        repo = getSiteRepositoryInstance(site);
                        if (repo != null) {
                            published.put(site, repo);
                        }
                    }
                    break;
                case GLOBAL:
                    repo = getGlobalConfigurationRepositoryInstance();
                    break;
                default:
                    repo = null;
            }
        } catch (IOException e) {
            logger.error("error writing file: site: " + site, e);
        }

        return repo;
    }
    private boolean writeFile(Repository repo, String site, String path, InputStream content) {
        // Create folder structure, create file, and write the bits

        boolean result;
        try {
            String gitPath = getGitPath(path);
            RevTree tree = getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, gitPath, tree);

            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path filePath;
            if (tw == null) {
                filePath = Paths.get(fs.normalize(repoRoot.getPath()), gitPath);
            } else {
                filePath = Paths.get(fs.normalize(repoRoot.getPath()), tw.getPathString());
            }

            File file = filePath.toFile();

            // Create parent folders
            File folder = file.getParentFile();
            if (folder != null) {
                if (!folder.exists()) {
                    folder.mkdirs();
                }
            }

            // Add the file to the index
            if (!Files.exists(filePath)) {
                filePath = Files.createFile(filePath);
            }

            // Write the bits
            FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(content));

            // Add the file to git
            // TODO: SJ: See if this is the fastest way to do this or if keeping a repo + git around would be faster
            Git git = new Git(repo);
            git.add()
                .addFilepattern(gitPath)
                .call();

            result = true;
        } catch (IOException | GitAPIException err) {
            logger.error("error writing file: site: " + site + " path: " + path, err);
            result = false;
        }

        return result;
    }

    private String commitFile(Repository repo, String site, String path) {
        String commitId = null;
        String comment = "Save file " + path;

        // TODO: SJ: TODAY: Complete this
        String gitPath = getGitPath(path);
        Git git = new Git(repo);

        Status status = null;
        try {
            status = git.status().addPath(gitPath).call();
        } catch (GitAPIException e) {
            logger.error("error adding file to git: site: " + site + " path: " + path, e);
        }

        if (status.hasUncommittedChanges() || !status.isClean()) {
            RevCommit commit = null;
            try {
                commit = git.commit().setOnly(gitPath).setMessage(comment).call();
            } catch (GitAPIException e) {
                logger.error("error committing file to git: site: " + site + " path: " + path, e);
            }
            commitId = commit.getId().toString();
        }

        return commitId;
    }

    /**
     * Return the current user identity as a jgit PersonIdent
     *
     * @return current user as a PersonIdent
     */
    PersonIdent getCurrentUserIdent() {
        String userName = securityProvider.getCurrentUser();
        Map<String, String> currentUserProfile = securityProvider.getUserProfile(userName);
        PersonIdent currentUserIdent = new PersonIdent
            (currentUserProfile.get("firstName") + " " + currentUserProfile.get("lastName"),
                currentUserProfile.get("email"));

        return currentUserIdent;
    }

    /**
     * bootstrap the repository
     */
    public void bootstrap() throws Exception {
        Path globalConfigFolder = Paths.get(rootPath, "global-configuration");
        boolean bootstrapCheck = Files.exists(globalConfigFolder);

        if(bootstrapEnabled && !bootstrapCheck) {
            try{
                logger.error("Bootstrapping repository for Crafter CMS");
                Files.createDirectories(globalConfigFolder);
            }
            catch(Exception alreadyExistsErr){
                // do nothing.
            }
            try {
                Path globalConfigRepoPath = Paths.get(globalConfigFolder.toAbsolutePath().toString(), ".git");
                Repository repository = FileRepositoryBuilder.create(globalConfigRepoPath.toFile());
                repository.create();
            } catch (IOException e) {
                logger.error("Error while creating global configuration repository", e);
            }

            String bootstrapFolderPath = this.ctx.getRealPath(File.separator + "gitrepo-bootstrap");
            Path source = java.nio.file.FileSystems.getDefault().getPath(bootstrapFolderPath);

            logger.info("Bootstrapping with baseline @ " + source.toFile().toString());

            Path target = Paths.get(rootPath);

            TreeCopier tc = new TreeCopier(source, target);
            EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);

            try {
                Repository globalConfigRepo = getGlobalConfigurationRepositoryInstance();

                Git git = new Git(globalConfigRepo);

                Status status = git.status().call();

                if (status.hasUncommittedChanges() || !status.isClean()) {
                    DirCache dirCache = git.add().addFilepattern(".").call();
                    RevCommit commit = git.commit()
                            .setMessage("initial content")
                            .call();
                    String tmp = commit.getId().toString();
                }
            } catch (IOException | GitAPIException err) {
                logger.error("error creating initial commit for global configuration", err);
            }
        }
    }

    public String getRootPath() { return rootPath; }
    public void setRootPath(String path) { rootPath = path; }

    public boolean isBootstrapEnabled() { return bootstrapEnabled; }
    public void setBootstrapEnabled(boolean bootstrapEnabled) { this.bootstrapEnabled = bootstrapEnabled; }

    public void setServletContext(ServletContext ctx) { this.ctx = ctx; }

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(final SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    String rootPath;
    boolean bootstrapEnabled;
    ServletContext ctx;
    SecurityProvider securityProvider;
}
