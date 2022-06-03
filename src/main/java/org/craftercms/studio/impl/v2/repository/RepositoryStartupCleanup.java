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

package org.craftercms.studio.impl.v2.repository;

import org.springframework.context.event.EventListener;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.io.EOFException;
import java.io.File;

import org.craftercms.commons.git.utils.GitUtils;

import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import org.craftercms.studio.impl.v2.utils.spring.event.CleanupRepositoriesEvent;

/**
 * Clean up git repositories on startup
 *
 * @author Phil Nguyen
 * @since 4.0.1
 */

public class RepositoryStartupCleanup {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryStartupCleanup.class);

    protected SiteService siteService;
    protected GeneralLockService generalLockService;
    protected GitRepositoryHelper helper;

    @EventListener(CleanupRepositoriesEvent.class)
    public void unlockRepositories() {
        logger.debug("Clean up git lock for all repositories.");
        try {
            unlockSitesRepositories();
        } catch (Exception e) {
            logger.error("Error cleaning up git lock", e);
        }
    }

    protected void unlockSitesRepositories() {
        siteService.getAllAvailableSites().forEach(siteId -> {
            logger.debug("Unlock git lock for site '{}'", siteId);
            String gitLockKeySandbox = helper.getSandboxRepoLockKey(siteId);
            String gitLockKeyPublished = helper.getPublishedRepoLockKey(siteId);

            generalLockService.lock(gitLockKeySandbox);
            try {
                unlockRepository(siteId, SANDBOX);
                removeIndexIfCorrupted(siteId, SANDBOX);
            } finally {
                generalLockService.unlock(gitLockKeySandbox);
            }

            generalLockService.lock(gitLockKeyPublished);
            try {
                unlockRepository(siteId, PUBLISHED);
                removeIndexIfCorrupted(siteId, PUBLISHED);
            } finally {
                generalLockService.unlock(gitLockKeyPublished);
            }
        });
    }

    protected void unlockRepository(String siteId, GitRepositories repository) {
        Path repoPath = helper.buildRepoPath(repository, siteId);
        if (repoPath != null) {
            String path = repoPath.toAbsolutePath().toString();
            if (GitUtils.isRepositoryLocked(path)) {
                try {
                    GitUtils.unlock(path);
                } catch (IOException e) {
                    logger.warn("Error unlocking git repository '{}'", path, e);
                }
            }
        }
    }

    protected void removeIndexIfCorrupted(String siteId, GitRepositories repository) {
        Repository repo = helper.getRepository(siteId, repository);
        if (isRepositoryCorrupted(repo)) {
            String repoPath = repo.getWorkTree().getAbsolutePath();
            try {
                logger.warn("The local repository '{}' is corrupt, trying to fix it", repoPath);
                try (Git git = new Git(repo)) {
                    GitUtils.deleteGitIndex(repoPath);

                    ResetCommand resetCommand = git.reset();
                    resetCommand.setMode(ResetCommand.ResetType.HARD);
                    resetCommand.call();

                    CleanCommand cleanupCommand = git.clean();
                    cleanupCommand.setForce(true);
                    cleanupCommand.call();

                    logger.info(".git/index is deleted from local repository '{}'", repoPath);
                } catch (Exception e) {
                    // rollback delete operation of .git/index in case reset/clean commands failed
                    String fileName = GitUtils.GIT_FOLDER_NAME + FILE_SEPARATOR + GitUtils.GIT_INDEX_NAME;
                    File indexFile = new File(repoPath, fileName);
                    if (!indexFile.exists()) {
                        indexFile.createNewFile();
                    }
                }
            } catch (IOException e) {
                logger.error("Error cleaning up git repository '{}'", repoPath, e);
            }
        }
    }

    protected boolean isRepositoryCorrupted(Repository repository) {
        if (repository == null) {
            return false;
        }

        try (Git git = new Git(repository)) {
            git.status().call();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            return cause instanceof CorruptObjectException || cause instanceof EOFException;
        }

        return false;
    }

    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }

    public void setGeneralLockService(final GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setHelper(final GitRepositoryHelper helper) {
        this.helper = helper;
    }
}