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

package org.craftercms.studio.impl.v1.deployment;

import static org.eclipse.jgit.lib.Constants.OBJECT_ID_STRING_LENGTH;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.*;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.deployment.ContentNotFoundForPublishingException;
import org.craftercms.studio.api.v1.service.deployment.UploadFailedException;
import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.SafeBufferedOutputStream;

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EnvironmentStoreGitDeployer implements Deployer {

    private final static Logger logger = LoggerFactory.getLogger(EnvironmentStoreGitDeployer.class);

    @Override
    public void deployFile(String site, String path) {
        try (Repository envStoreRepo = getEnvironmentStoreRepositoryInstance(site)) {
            fetchFromRemote(site, envStoreRepo);
            InputStream patch = createPatch(envStoreRepo, site, path);
            Git git = new Git(envStoreRepo);
            applyPatch(envStoreRepo, site, path, patch);
            /*
            ApplyResult result = git.apply()
                    .setPatch(patch)
                    .call();
                    */
            git.add().addFilepattern(".").call();
            git.commit().setMessage("deployment").call();

        } catch (/*IOException | GitAPIException | */Exception e ) {
            logger.error("Error while deploying file for site: " + site + " path: " + path, e);
        }
    }

    private void applyPatch(Repository envStoreRepo, String site, String path, InputStream patch) {
        String tempPath = System.getProperty("java.io.tmpdir");
        if (tempPath == null) {
            tempPath = "temp";
        }
        Path patchPath = Paths.get(tempPath, "patch" + site +".bin");
        //Path patchPath = Paths.get(tempPath, "patch" + UUID.randomUUID().toString() +".bin");
        /*logger.error("Patch path : " + patchPath.toAbsolutePath().normalize().toString());
        try {
            Files.deleteIfExists(patchPath);
            Files.copy(patch, patchPath);
        } catch (IOException e) {
            logger.error("Error saving patch content to " + patchPath.toAbsolutePath().normalize().toString());
        } finally {

        }*/

        String command = "git apply " + patchPath.toAbsolutePath().normalize().toString();
        logger.error("GIT APPLY : " + command);
        Process p;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("git", "apply", patchPath.toAbsolutePath().normalize().toString());
            pb.directory(envStoreRepo.getDirectory());
            p = pb.start();
            //p = Runtime.getRuntime().exec(command, null, envStoreRepo.getDirectory());
            p.waitFor();
            p.exitValue();
        } catch (Exception e) {
            logger.error("Error applying patch for site: " + site, e);
        }
    }

    private void fetchFromRemote(String site, Repository repository) {
        StringBuffer output = new StringBuffer();

        String command = "git fetch work-area";
        Process p;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("git", "fetch", "work-area");
            pb.directory(repository.getDirectory());
            p = pb.start();
            //p = Runtime.getRuntime().exec(command, null, repository.getDirectory());
            p.waitFor();
            p.exitValue();
        } catch (Exception e) {
            logger.error("Error while fetching from work-area  for site: " + site, e);
        }
    }

    private void fetchFromRemote_old(String site, Repository repository) {
        try (Git git = new Git(repository)) {
            FetchResult result = git.fetch()
                    .setRemote("work-area")
                    .setCheckFetchedObjects(true)
                    .call();

        } catch (GitAPIException e) {
            logger.error("Error while fetching updates for repository: " + repository.getDirectory().getAbsolutePath(), e);
        }
    }

    private InputStream createPatch(Repository repository, String site, String path) {
        StringBuffer output = new StringBuffer();

        String tempPath = System.getProperty("java.io.tmpdir");
        if (tempPath == null) {
            tempPath = "temp";
        }
        Path patchPath = Paths.get(tempPath, "patch" + site +".bin");

        String gitPath = getGitPath(path);
        String command = "/usr/bin/git diff --binary HEAD FETCH_HEAD -- " + gitPath + " > " + patchPath.toAbsolutePath().normalize().toString();
        Process p;
        try {
            logger.error("Repo dir: " + repository.getDirectory().getAbsolutePath());
            logger.error("Processing path : " + path);
            logger.error("CREATE PATCH COMMAND : " + command);
            //p = Runtime.getRuntime().exec(command, null, repository.getDirectory());
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("git", "diff", "--binary", "HEAD", "FETCH_HEAD", "--", gitPath);
            //pb.command()
            pb.redirectOutput(patchPath.toAbsolutePath().normalize().toFile());
            pb.directory(repository.getDirectory());
            logger.error("COMMAND : " + pb.toString());
            p = pb.start();
            logger.error("COMMAND : " + p.toString());

            int code = p.waitFor();
            int code2 = p.exitValue();
            logger.error("EXIT WITH CODE: " + code + " " + code2);
            //return p.getInputStream();

        } catch (Exception e) {
            logger.error("Error while creating patch for site: " + site + " path: " + path, e);
        }

        return null;
    }

    private InputStream createPatch_old(Repository repository, String site, String path) {
        try (Git git = new Git(repository)) {

            // the diff works on TreeIterators, we prepare two for the two branches
            AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, "refs/heads/master");
            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, "FETCH_HEAD");//"refs/remotes/work-area/master");

            // then the procelain diff-command returns a list of diff entries
            List<DiffEntry> diff = git.diff()
                    .setOldTree(oldTreeParser)
                    .setNewTree(newTreeParser)
                    .setPathFilter(getTreeFilter(path))
                    .call();

            //PipedInputStream pin = new PipedInputStream();
            OutputStream out = new ByteArrayOutputStream();

            for (DiffEntry diffEntry : diff) {
                DiffEntry.ChangeType ct = diffEntry.getChangeType();
                DiffFormatter df = new DiffFormatter(out);
                FileHeader fh = df.toFileHeader(diffEntry);
                if (fh.getPatchType().equals(FileHeader.PatchType.BINARY)) {
                    logger.error("ERRRRRRRRRRROR");
                }
            }


            //OutputStream os = new FileOutputStream("/Users/dejanbrkic/gitpatchtest.diff");
            DiffFormatter df = new DiffFormatter(out);
            df.setRepository(repository);
            //df.setBinaryFileThreshold();
            df.setPathFilter(getTreeFilter(path));
            df.setAbbreviationLength(OBJECT_ID_STRING_LENGTH);
            df.format(diff);
            df.flush();
            df.close();
            String content = out.toString();
            logger.error("++++++++++++++++");
            logger.error(content);
            logger.error("++++++++++++++++");
            InputStream in = IOUtils.toInputStream(content);
            return in;
        } catch (GitAPIException | IOException e) {
            logger.error("Error while creating patch for site: " + site + " path: " + path, e);
        }
        return null;
    }

    private TreeFilter getTreeFilter(String path) {
        TreeFilter tf = PathFilterGroup.createFromStrings(getGitPath(path));
        return tf;
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

    private Repository getEnvironmentStoreRepositoryInstance(String site) throws IOException {

        Path siteRepoPath = Paths.get(environmentsStoreRootPath, site, environment, ".git").toAbsolutePath();
        if (!Files.exists(siteRepoPath)) {
            createEnvironmentStoreRepository(site);
        }
        Repository envStoreRepo = openGitRepository(siteRepoPath);
        if (!checkIfWorkAreaAddedAsRemote(envStoreRepo)) {
            addWorkAreaRemote(site, envStoreRepo);
        }
        return envStoreRepo;
    }

    private boolean createEnvironmentStoreRepository(String site) {
        boolean success = true;
        Path siteEnvironmentStoreRepoPath = Paths.get(environmentsStoreRootPath, site, environment);
        try {
            Files.deleteIfExists(siteEnvironmentStoreRepoPath);
            siteEnvironmentStoreRepoPath = Paths.get(environmentsStoreRootPath, site, environment, ".git");
            Repository repository = FileRepositoryBuilder.create(siteEnvironmentStoreRepoPath.toFile());
            repository.create();

            Git git = new Git(repository);
            git.add().addFilepattern(".").call();
            RevCommit commit = git.commit()
                    .setMessage("initial content")
                    .setAllowEmpty(true)
                    .call();
        } catch (IOException | GitAPIException e) {
            logger.error("Error while creating repository for site " + site, e);
            success = false;
        }
        return success;
    }

    private void addWorkAreaRemote(String site, Repository envStoreRepo) {
        envStoreRepo.getRemoteName("work-area");
        Git git = new Git(envStoreRepo);
        StoredConfig config = git.getRepository().getConfig();
        Path siteRepoPath = Paths.get(rootPath, "sites", site, ".git");
        config.setString("remote", "work-area", "url", siteRepoPath.normalize().toAbsolutePath().toString());
        try {
            config.save();
        } catch (IOException e) {
            logger.error("Error adding work area as remote for environment store.", e);
        }
    }

    private boolean checkIfWorkAreaAddedAsRemote(Repository repository) {
        boolean exists = false;
        try {
            Config storedConfig = repository.getConfig();
            Set<String> remotes = storedConfig.getSubsections("remote");

            for (String remoteName : remotes) {
                logger.debug("Remote: " + remoteName);
                if (remoteName.equals("work-area")) {
                    exists = true;
                    break;
                }
            }
        } catch (Exception err) {
            logger.error("Error while reading remotes info.", err);
        }
        return exists;
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

    private String getGitPath(String path) {
        String gitPath = path.replaceAll("/+", "/");
        gitPath = gitPath.replaceAll("^/", "");
        return gitPath;
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

    @Override
    public void deployFiles(String site, List<String> paths) {

    }

    @Override
    public void deployFiles(String site, List<String> paths, List<String> deletedFiles) throws ContentNotFoundForPublishingException, UploadFailedException {

    }

    @Override
    public void deleteFile(String site, String path) {

    }

    @Override
    public void deleteFiles(String site, List<String> paths) {

    }

    public String getEnvironmentsStoreRootPath() { return environmentsStoreRootPath; }
    public void setEnvironmentsStoreRootPath(String environmentsStoreRootPath) { this.environmentsStoreRootPath = environmentsStoreRootPath; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getRootPath() { return rootPath; }
    public void setRootPath(String path) { rootPath = path; }

    protected String environmentsStoreRootPath;
    protected ContentService contentService;
    protected String environment;
    protected String rootPath;
}
