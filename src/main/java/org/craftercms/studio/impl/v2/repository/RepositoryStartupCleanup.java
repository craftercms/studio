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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.impl.v2.utils.GitUtils;

import org.craftercms.studio.api.v1.constant.GitRepositories;
import static org.craftercms.studio.api.v1.constant.GitRepositories.GLOBAL;
import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.GLOBAL_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_PUBLISHED_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;

import org.springframework.beans.factory.annotation.Required;

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
            String gitLockKeySandbox = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
            String gitLockKeyPublished = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);

            generalLockService.lock(gitLockKeySandbox);
            try {
                unlockRepository(siteId, SANDBOX, helper);
            } finally {
                generalLockService.unlock(gitLockKeySandbox);
            }

            generalLockService.lock(gitLockKeyPublished);
            try {
                unlockRepository(siteId, PUBLISHED, helper);
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