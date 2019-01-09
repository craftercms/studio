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

package org.craftercms.studio.impl.v1.deployment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.deployment.ContentNotFoundForPublishingException;
import org.craftercms.studio.api.v1.service.deployment.UploadFailedException;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class EnvironmentStoreGitBranchDeployer implements Deployer {

    private final static Logger logger = LoggerFactory.getLogger(EnvironmentStoreGitBranchDeployer.class);

    @Override
    public void deployFile(String site, String path) {
        Repository repository = cloneSiteRepository(site);
        if (repository != null) {
            checkoutEnvironment(repository, site);
            createPatch(repository, site, path);
            applyPatch(repository, site);
            pushChanges(repository);
            cleanup(site);
        }
    }

    private Repository cloneSiteRepository(String site) {
        Path siteEnvironmentStoreRepoPath = Paths.get(environmentsStoreRootPath, site);
        File localPath = siteEnvironmentStoreRepoPath.toFile();
        try {
            FileUtils.deleteDirectory(localPath);
        } catch (IOException e) {
            logger.error("Error deleting directory " + localPath.toString());
        }

        Path siteRepoPath = Paths.get(rootPath, "sites", site, ".git");
        try (Git result = Git.cloneRepository()
                .setURI(siteRepoPath.toAbsolutePath().normalize().toString())
                .setDirectory(localPath)
                .call()) {
            return result.getRepository();
        } catch (GitAPIException e) {
            logger.error("Error cloning repository for site " + site, e);
            return null;
        }
    }

    private void checkoutEnvironment(Repository repository, String site) {
        Git git = null;
        try {
            Ref branchRef = repository.findRef(environment);
            git = new Git(repository);
            git.checkout().
                    setCreateBranch(true).
                    setName(environment).
                    setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
                    setStartPoint("origin/" + environment).
                    call();
            git.fetch().call();
            git.pull().call();
        } catch (RefNotFoundException e) {
            try {
                git.checkout()
                        .setOrphan(true)
                        .setName(environment)
                        .call();
                ProcessBuilder pb = new ProcessBuilder();
                pb.command("git", "rm", "-rf", ".");
                pb.directory(repository.getDirectory().getParentFile());
                Process p = pb.start();
                p.waitFor();

                git.commit()
                        .setMessage("initial content")
                        .setAllowEmpty(true)
                        .call();
            } catch (GitAPIException | InterruptedException | IOException e1) {
                logger.error("Error checking out environment store branch for site " + site + " environment " + environment, e1);
            }
        } catch (IOException | GitAPIException e) {
            logger.error("Error checking out environment store branch for site " + site + " environment " + environment, e);
        }
    }

    private void applyPatch(Repository envStoreRepo, String site) {
        String tempPath = System.getProperty("java.io.tmpdir");
        if (tempPath == null) {
            tempPath = "temp";
        }
        Path patchPath = Paths.get(tempPath, "patch" + site +".bin");
        Process p;
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("git", "apply", patchPath.toAbsolutePath().normalize().toString());
            pb.directory(envStoreRepo.getDirectory().getParentFile());
            p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            logger.error("Error applying patch for site: " + site, e);
        }
    }

    private void createPatch(Repository repository, String site, String path) {
        StringBuffer output = new StringBuffer();

        String tempPath = System.getProperty("java.io.tmpdir");
        if (tempPath == null) {
            tempPath = "temp";
        }
        Path patchPath = Paths.get(tempPath, "patch" + site +".bin");

        String gitPath = getGitPath(path);
        Process p = null;
        File file = patchPath.toAbsolutePath().normalize().toFile();
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("git", "diff", "--binary", environment, "master", "--", gitPath);

            pb.redirectOutput(file);
            pb.directory(repository.getDirectory().getParentFile());
            p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            logger.error("Error while creating patch for site: " + site + " path: " + path, e);
        }
    }

    private void pushChanges(Repository repository) {
        Git git = new Git(repository);
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("deployment to environment store").call();
            git.push().call();
        } catch (GitAPIException e) {
            logger.error("Error while pushing workflow changes.", e);
        }
    }

    private void cleanup(String site) {
        Path siteEnvironmentStoreRepoPath = Paths.get(environmentsStoreRootPath, site);
        File localPath = siteEnvironmentStoreRepoPath.toFile();
        try {
            FileUtils.deleteDirectory(localPath);
        } catch (IOException e) {
            logger.error("Error deleting directory " + localPath.toString());
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
            try (Repository repository = FileRepositoryBuilder.create(siteEnvironmentStoreRepoPath.toFile())) {
                repository.create();

                Git git = new Git(repository);
                git.add().addFilepattern(".").call();
                RevCommit commit = git.commit()
                        .setMessage("initial content")
                        .setAllowEmpty(true)
                        .call();
            }
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

    private String getGitPath(String path) {
        String gitPath = FilenameUtils.normalize(path, true);
        gitPath = gitPath.replaceAll("^" + FILE_SEPARATOR, "");
        return gitPath;
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
        try {
            Repository repo = getEnvironmentStoreRepositoryInstance(site);
            Git git = new Git(repo);
            git.rm()
                    .addFilepattern(getGitPath(path))
                    .setCached(false)
                    .call();

            RevCommit commit = git.commit()
                    .setOnly(getGitPath(path))
                    .setMessage(StringUtils.EMPTY)
                    .call();
        } catch (GitAPIException | IOException | JGitInternalException e) {
            logger.error("Error while deleting content from environment store for site: " + site + " path: " + path + " environment: " + environment, e);
        }
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
