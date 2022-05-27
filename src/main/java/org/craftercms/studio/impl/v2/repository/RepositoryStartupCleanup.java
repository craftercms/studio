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

import org.springframework.beans.factory.annotation.Required;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.EOFException;
import java.nio.file.Path;
import java.io.File;

import org.craftercms.commons.git.utils.GitUtils;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

/**
 * Clean up git repositories on startup
 *
 * @author Phil Nguyen
 */

public class RepositoryStartupCleanup {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryStartupCleanup.class);

    protected SiteService siteService;
    protected StudioConfiguration studioConfiguration;
    protected SecurityService securityService;
    protected UserServiceInternal userServiceInternal;
    private TextEncryptor encryptor;
    protected GeneralLockService generalLockService;
    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    public void unlockRepositories() {
        logger.debug("Clean up git lock for all repositories.");
        try {
            GitRepositoryHelper helper =  GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            unlockSitesRepositories(helper);
        } catch (Exception e) {
            logger.error("Error cleaning up git lock", e);
        }
    }

    protected void unlockSitesRepositories(GitRepositoryHelper helper) {
        siteService.getAllAvailableSites().forEach(siteId -> {
            logger.debug("Unlock git lock for site '{}'", siteId);
            String gitLockKeySandbox = helper.getSandboxRepoLockKey(siteId);
            String gitLockKeyPublished = helper.getPublishedRepoLockKey(siteId);

            generalLockService.lock(gitLockKeySandbox);
            try {
                unlockRepository(siteId, SANDBOX, helper);
                removeIndexIfCorrupted(siteId, SANDBOX, helper);
            } finally {
                generalLockService.unlock(gitLockKeySandbox);
            }

            generalLockService.lock(gitLockKeyPublished);
            try {
                unlockRepository(siteId, PUBLISHED, helper);
                removeIndexIfCorrupted(siteId, PUBLISHED, helper);
            } finally {
                generalLockService.unlock(gitLockKeyPublished);
            }
        });
    }

    protected void unlockRepository(String siteId, GitRepositories repository, GitRepositoryHelper helper) {
        Path repoPath = helper.buildRepoPath(repository, siteId);
        if (repoPath != null) {
            String path = repoPath.toAbsolutePath().toString();
            if (GitUtils.isRepositoryLocked(path)) {
                try {
                    logger.warn("The local repository '{}' is locked, trying to unlock it", path);
                    GitUtils.unlock(path);
                    logger.info(".git/index.lock is deleted from local repository '{}'", path);
                } catch (IOException e) {
                    logger.warn("Error unlocking git repository '{}'", path, e);
                }
            }
        }
    }

    protected void removeIndexIfCorrupted(String siteId, GitRepositories repository, GitRepositoryHelper helper) {
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

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Required
    public void setSecurityService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    @Required
    public void setUserServiceInternal(final UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    @Required
    public void setEncryptor(final TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Required
    public void setGeneralLockService(final GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    @Required
    public void setRetryingRepositoryOperationFacade(final RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }
}