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

import jakarta.servlet.ServletContext;
import org.apache.commons.io.FileUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.repository.git.TreeCopier;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.ServletContextAware;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import static java.lang.Integer.MAX_VALUE;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.GitRepositories.GLOBAL;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;

/**
 * Bootstrap the global repository.
 */
public class GlobalRepoBootstrap implements ServletContextAware {
	private static final Logger logger = LoggerFactory.getLogger(GlobalRepoBootstrap.class);
	private static final String STUDIO_MANIFEST_LOCATION = "/META-INF/MANIFEST.MF";
	private static final String BLUEPRINTS_MF_FILENAME = "BLUEPRINTS.MF";

	private final GitRepositoryHelper helper;
	private final StudioConfiguration studioConfiguration;

	private final RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
	private ServletContext servletContext;

	@ConstructorProperties({"helper", "retryingRepositoryOperationFacade", "studioConfiguration"})
	public GlobalRepoBootstrap(final GitRepositoryHelper helper,
							   final RetryingRepositoryOperationFacade retryingRepositoryOperationFacade, final
							   StudioConfiguration studioConfiguration) {
		this.helper = helper;
		this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
		this.studioConfiguration = studioConfiguration;
	}

	/**
	 * bootstrap the global repository
	 */
	@Order(1)
	// the condition is needed to avoid a repeated event from a child app context
	@EventListener(value = ContextRefreshedEvent.class, condition = "event.applicationContext.parent == null")
	public void bootstrap() throws Exception {
		logger.debug("Bootstrap the Global repository");

		boolean bootstrapRepo = Boolean.parseBoolean(studioConfiguration.getProperty(BOOTSTRAP_REPO));

		if (bootstrapRepo && helper.createGlobalRepo()) {
			populateGlobalRepo();
		}

		// Create global repository object
		if (!helper.buildGlobalRepo()) {
			logger.error("Failed to create the global repository");
		}
	}

	/**
	 * Populate the global repository with the default configuration
	 *
	 * @throws IOException if an error occurs while copying the files
	 */
	private void populateGlobalRepo() throws IOException {
		// Copy the global config defaults to the global site
		// Build a path to the bootstrap repo (the repo that ships with Studio)
		String bootstrapFolderPath = this.servletContext.getRealPath(FILE_SEPARATOR + BOOTSTRAP_REPO_PATH +
				FILE_SEPARATOR + BOOTSTRAP_REPO_GLOBAL_PATH);
		Path source = java.nio.file.FileSystems.getDefault().getPath(bootstrapFolderPath);

		logger.info("Bootstrap with baseline @'{}'", source.toFile());

		// Copy the bootstrap repo to the global repo
		Path globalConfigPath = helper.buildGlobalRepoPath();
		TreeCopier tc = new TreeCopier(source,
				globalConfigPath);
		EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
		Files.walkFileTree(source, opts, MAX_VALUE, tc);

		Path studioManifestLocation = Paths.get(this.servletContext.getRealPath(STUDIO_MANIFEST_LOCATION));
		if (Files.exists(studioManifestLocation)) {
			FileUtils.copyFile(studioManifestLocation.toFile(),
					Paths.get(globalConfigPath.toAbsolutePath().toString(),
							studioConfiguration.getProperty(BLUE_PRINTS_PATH), BLUEPRINTS_MF_FILENAME).toFile());
		}
		try {
			Repository globalConfigRepo = helper.getRepository(EMPTY, GLOBAL);
			try (Git git = new Git(globalConfigRepo)) {
				StatusCommand statusCommand = git.status();
				Status status = retryingRepositoryOperationFacade.call(statusCommand);

				if (status.hasUncommittedChanges() || !status.isClean()) {
					// Commit everything
					// TODO: Consider what to do with the commitId in the future
					AddCommand addCommand = git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS);
					retryingRepositoryOperationFacade.call(addCommand);

					helper.commitFiles(globalConfigRepo, EMPTY, helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE),
							helper.getAuthorIdent(GIT_REPO_USER_USERNAME), EMPTY);
				}
			}
		} catch (GitAPIException | ServiceLayerException | UserNotFoundException e) {
			logger.error("Failed to create the initial commit for the global repository", e);
		}
	}

	@Override
	public void setServletContext(@NonNull final ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
