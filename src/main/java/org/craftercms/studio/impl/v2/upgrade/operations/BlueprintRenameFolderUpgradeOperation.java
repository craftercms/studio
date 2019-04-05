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

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_BLUEPRINTS_UPDATED_COMMIT_MESSAGE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradeOperation} that renames the blueprint in the
 * global repository.
 * @author Dejan Brkic
 */
public class BlueprintRenameFolderUpgradeOperation extends AbstractUpgradeOperation implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BlueprintRenameFolderUpgradeOperation.class);


    public static final String CONFIG_KEY_BLUEPRINT_FOLDER_NAME = "blueprintFolderName";
    public static final String CONFIG_KEY_NEW_FOLDER_NAME = "newFolderName";

    protected ServletContext servletContext;
    protected ServicesConfig servicesConfig;
    protected SecurityService securityService;
    protected UserServiceInternal userServiceInternal;

    protected String blueprintFolderName;
    protected String newFolderName;

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


    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final Configuration config) {
        blueprintFolderName = config.getString(CONFIG_KEY_BLUEPRINT_FOLDER_NAME);
        newFolderName = config.getString(CONFIG_KEY_NEW_FOLDER_NAME);
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        try {
            GitContentRepositoryHelper helper =
                new GitContentRepositoryHelper(studioConfiguration, servicesConfig, userServiceInternal, securityService);
            Path globalConfigPath = helper.buildRepoPath(GitRepositories.GLOBAL);
            Path blueprintPath = Paths.get(globalConfigPath.toAbsolutePath().toString(),
                    studioConfiguration.getProperty(BLUE_PRINTS_PATH), blueprintFolderName);
            File blueprintFolder = blueprintPath.toFile();
            if (blueprintFolder.exists()) {
                Path newBlueprintPath = Paths.get(globalConfigPath.toAbsolutePath().toString(),
                        studioConfiguration.getProperty(BLUE_PRINTS_PATH), newFolderName);
                File newBlueprintFolder = newBlueprintPath.toFile();
                FileUtils.deleteDirectory(newBlueprintFolder);
                blueprintFolder.renameTo(newBlueprintFolder);
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
