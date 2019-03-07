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

package org.craftercms.studio.impl.v2.upgrade.operations;

import java.io.File;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.monitoring.VersionInfo;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryHelper;
import org.craftercms.studio.impl.v1.repository.git.TreeCopier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;

import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_GLOBAL_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_BLUEPRINTS_UPDATED_COMMIT_MESSAGE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that syncs the blueprints in the
 * global repository from the bootstrap repo.
 * @author joseross
 */
public class BlueprintsUpgradeOperation extends AbstractUpgradeOperation implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BlueprintsUpgradeOperation.class);

    private static final String STUDIO_MANIFEST_LOCATION = "/META-INF/MANIFEST.MF";

    protected ServletContext servletContext;
    protected ServicesConfig servicesConfig;
    protected SecurityService securityService;
    protected UserServiceInternal userServiceInternal;

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Required
    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        try {
            GitContentRepositoryHelper helper =
                new GitContentRepositoryHelper(studioConfiguration, servicesConfig, userServiceInternal, securityService);
            Path globalConfigPath = helper.buildRepoPath(GitRepositories.GLOBAL);
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

            Repository globalRepo = helper.getRepository(site, GitRepositories.GLOBAL);
            try (Git git = new Git(globalRepo)) {

                Status status = git.status().call();

                if (status.hasUncommittedChanges() || !status.isClean()) {
                    // Commit everything
                    // TODO: Consider what to do with the commitId in the future
                    git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS).call();
                    git.commit()
                            .setAll(true)
                            .setMessage(studioConfiguration.getProperty(REPO_BLUEPRINTS_UPDATED_COMMIT_MESSAGE)).call();
                }
            } catch (GitAPIException err) {
                logger.error("error creating initial commit for global configuration", err);
            }
        } catch (Exception e) {
            throw new UpgradeException("Error upgrading blueprints in the global repo", e);
        }
    }

}
