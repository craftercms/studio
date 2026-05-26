/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v2.exception.repository.RepositoryException;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.impl.v2.utils.spring.event.CleanupRepositoriesEvent;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v2.dal.Site.State.READY;

/**
 * Clean up git repositories on startup
 *
 * @author Phil Nguyen
 * @since 4.0.1
 */

public class RepositoryStartupCleanup {
	private static final Logger logger = LoggerFactory.getLogger(RepositoryStartupCleanup.class);

	protected SitesService siteService;
	protected GeneralLockService generalLockService;
	protected GitRepositoryHelper helper;

	@Order(20)
	@LogExecutionTime
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
		siteService.getSitesByState(READY).forEach(site -> {
			String siteId = site.getSiteId();
			logger.debug("Unlock git lock for site '{}'", siteId);
			String gitLockKeySandbox = helper.getSandboxRepoLockKey(siteId);

			generalLockService.lock(gitLockKeySandbox);
			try {
				unlockRepository(siteId, SANDBOX);
				removeIndexIfCorrupted(siteId, SANDBOX);
			} catch (RepositoryException e) {
				logger.error("Error unlocking git repository for site '{}'", siteId, e);
			} finally {
				generalLockService.unlock(gitLockKeySandbox);
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

	protected void removeIndexIfCorrupted(String siteId, GitRepositories repository) throws RepositoryException {
		logger.debug("Checking if repository '{}' for site '{}' is corrupted", repository, siteId);
		Repository repo = helper.getRepository(siteId, repository);
		if (repo == null) {
			logger.warn("Repository '{}' for site '{}' is not found", repository, siteId);
			return;
		}
		File repoDir = repo.getWorkTree();
		try {
			if (!helper.gitStatusOk(repo)) {
				logger.warn("The local repository '{}' is corrupt, trying to fix it", repoDir.getAbsolutePath());
				helper.removeIndexAndClean(repoDir);
				logger.info(".git/index is deleted from local repository '{}'", repoDir.getAbsolutePath());
			}
		} catch (IOException e) {
			logger.error("Error cleaning up git repository '{}'", repoDir.getAbsolutePath(), e);
		}
	}

	public void setSiteService(final SitesService siteService) {
		this.siteService = siteService;
	}

	public void setGeneralLockService(final GeneralLockService generalLockService) {
		this.generalLockService = generalLockService;
	}

	public void setHelper(final GitRepositoryHelper helper) {
		this.helper = helper;
	}
}
