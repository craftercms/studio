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

import org.craftercms.studio.api.v2.annotation.RetryingDatabaseOperation;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;

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
import org.springframework.web.context.ServletContextAware;

import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_PUBLISHED_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;

/**
 * Clean up git repositories on startup
 *
 * @author Phil Nguyen
 */

public class RepositoryStartupCleanup {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryStartupCleanup.class);

    protected SiteService siteService;
    protected GeneralLockService generalLockService;
    protected GitRepositoryHelper helper;

    @Order(1)
    @EventListener(ContextRefreshedEvent.class)
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
            String gitLockKeySandbox = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
            String gitLockKeyPublished = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);

            generalLockService.lock(gitLockKeySandbox);
            try {
                unlockRepository(siteId, SANDBOX);
            } finally {
                generalLockService.unlock(gitLockKeySandbox);
            }

            generalLockService.lock(gitLockKeyPublished);
            try {
                unlockRepository(siteId, PUBLISHED);
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
                    logger.warn("The local repository '{}' is locked, trying to unlock it", path);
                    GitUtils.unlock(path);
                    logger.info(".git/index.lock is deleted from local repository '{}'", path);
                } catch (IOException e) {
                    logger.warn("Error unlocking git repository '{}'", path, e);
                }
            }
        }
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