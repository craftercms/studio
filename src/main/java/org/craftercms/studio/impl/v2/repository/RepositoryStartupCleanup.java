/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.git.utils.GitUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.impl.v2.utils.spring.event.CleanupRepositoriesEvent;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.nio.file.Path;

import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;

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
		logger.debug("Unlock repository '{}' for site '{}'", repository, siteId);
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
		logger.debug("Checking if repository '{}' for site '{}' is corrupted", repository, siteId);
		Repository repo = helper.getRepository(siteId, repository);
		if (repo == null) {
			logger.warn("Repository '{}' for site '{}' is not found", repository, siteId);
			return;
		}
		String repoPath = repo.getWorkTree().getAbsolutePath();
		try {
			if (!helper.gitStatusOk(repo)) {
				logger.warn("The local repository '{}' is corrupt, trying to fix it", repoPath);
				helper.removeIndexAndClean(repoPath);
				logger.info(".git/index is deleted from local repository '{}'", repoPath);
			}
		} catch (IOException e) {
			logger.error("Error cleaning up git repository '{}'", repoPath, e);
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
