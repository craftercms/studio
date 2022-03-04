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

package org.craftercms.studio.impl.v2.upgrade.operations.global;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.monitoring.VersionInfo;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.repository.git.TreeCopier;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_GLOBAL_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BLUEPRINTS_UPDATED_COMMIT_MESSAGE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that syncs the blueprints in the
 * global repository from the bootstrap repo.
 * @author joseross
 */
public class BlueprintsUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(BlueprintsUpgradeOperation.class);

    private static final String STUDIO_MANIFEST_LOCATION = "/META-INF/MANIFEST.MF";

    protected GeneralLockService generalLockService;
    protected GitRepositoryHelper gitRepositoryHelper;
    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    @ConstructorProperties({"studioConfiguration", "generalLockService", "gitRepositoryHelper",
            "retryingRepositoryOperationFacade"})
    public BlueprintsUpgradeOperation(StudioConfiguration studioConfiguration,
                                      GeneralLockService generalLockService,
                                      GitRepositoryHelper gitRepositoryHelper,
                                      RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        super(studioConfiguration);
        this.generalLockService = generalLockService;
        this.gitRepositoryHelper = gitRepositoryHelper;
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            Path globalConfigPath = gitRepositoryHelper.buildRepoPath(GitRepositories.GLOBAL);
            Path blueprintsPath = Paths.get(globalConfigPath.toAbsolutePath().toString(),
                studioConfiguration.getProperty(BLUE_PRINTS_PATH));

            String studioManifestLocation = servletContext.getRealPath(STUDIO_MANIFEST_LOCATION);
            String blueprintsManifestLocation =
                Paths.get(blueprintsPath.toAbsolutePath().toString(), "BLUEPRINTS.MF").toAbsolutePath().toString();
            boolean blueprintManifestExists = Files.exists(Paths.get(blueprintsManifestLocation));
            InputStream studioManifestStream = FileUtils.openInputStream(new File(studioManifestLocation));
            Manifest studioManifest = new Manifest(studioManifestStream);
            VersionInfo studioVersion = VersionInfo.getVersion(studioManifest);
            InputStream blueprintsManifestStream = null;
            Manifest blueprintsManifest = null;
            VersionInfo blueprintsVersion = null;
            if (blueprintManifestExists) {
                blueprintsManifestStream = FileUtils.openInputStream(new File(blueprintsManifestLocation));
                blueprintsManifest = new Manifest(blueprintsManifestStream);
                blueprintsVersion = VersionInfo.getVersion(blueprintsManifest);
            }

            if (!blueprintManifestExists || !StringUtils.equals(studioVersion.getPackageBuild(),
                blueprintsVersion.getPackageBuild())
                || (StringUtils.equals(studioVersion.getPackageBuild(), blueprintsVersion.getPackageBuild()) &&
                !StringUtils.equals(studioVersion.getPackageBuildDate(), blueprintsVersion.getPackageBuildDate()))) {
                String bootstrapBlueprintsFolderPath =
                    servletContext.getRealPath(FILE_SEPARATOR + BOOTSTRAP_REPO_PATH +
                        FILE_SEPARATOR + BOOTSTRAP_REPO_GLOBAL_PATH + FILE_SEPARATOR +
                        studioConfiguration.getProperty(BLUE_PRINTS_PATH));
                File bootstrapBlueprintsFolder = new File(bootstrapBlueprintsFolderPath);
                File[] blueprintFolders = bootstrapBlueprintsFolder.listFiles(File::isDirectory);
                for (File blueprintFolder : blueprintFolders) {
                    String blueprintName = blueprintFolder.getName();
                    FileUtils.deleteDirectory(
                        Paths.get(blueprintsPath.toAbsolutePath().toString(), blueprintName).toFile());
                    TreeCopier tc = new TreeCopier(Paths.get(blueprintFolder.getAbsolutePath()),
                        Paths.get(blueprintsPath.toAbsolutePath().toString(), blueprintName));
                    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                    Files.walkFileTree(Paths.get(blueprintFolder.getAbsolutePath()), opts, Integer.MAX_VALUE, tc);
                }

                FileUtils.copyFile(Paths.get(studioManifestLocation).toFile(),
                    Paths.get(globalConfigPath.toAbsolutePath().toString(),
                        studioConfiguration.getProperty(BLUE_PRINTS_PATH), "BLUEPRINTS.MF").toFile());
            }

            Repository globalRepo = gitRepositoryHelper.getRepository(site, GitRepositories.GLOBAL);
            try (Git git = new Git(globalRepo)) {
                StatusCommand statusCommand = git.status();
                Status status = retryingRepositoryOperationFacade.call(statusCommand);

                if (status.hasUncommittedChanges() || !status.isClean()) {
                    // Commit everything
                    // TODO: Consider what to do with the commitId in the future
                    AddCommand addCommand = git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS);
                    retryingRepositoryOperationFacade.call(addCommand);
                    CommitCommand commitCommand = git.commit()
                            .setAll(true)
                            .setMessage(studioConfiguration.getProperty(REPO_BLUEPRINTS_UPDATED_COMMIT_MESSAGE));
                    retryingRepositoryOperationFacade.call(commitCommand);
                }
            } catch (GitAPIException err) {
                logger.error("error creating initial commit for global configuration", err);
            }
        } catch (Exception e) {
            throw new UpgradeException("Error upgrading blueprints in the global repo", e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

}
