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
package org.craftercms.studio.impl.v2.upgrade;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNoneEmpty;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.GLOBAL_REPO_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SANDBOX_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

/**
 * Extension of {@link UpgradeContext} that holds all relevant information for a system or site upgrade.
 *
 * The {@code target} object is the name of the site being upgraded.
 *
 * @author joseross
 * @since 4.0.0
 */
public class StudioUpgradeContext extends UpgradeContext<String> {

    public static final String COMMIT_IDENTIFIER_FORMAT = "%s,%s,%s";

    /**
     * Studio configuration
     */
    protected StudioConfiguration studioConfiguration;

    /**
     * The database data source.
     */
    protected DataSource dataSource;

    /**
     * The instance service
     */
    protected InstanceService instanceService;

    /**
     * The name of the config file being upgraded
     */
    protected String currentConfigName;

    /**
     * The path of the config file being upgraded
     */
    protected String currentConfigPath;

    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    public StudioUpgradeContext(String target, StudioConfiguration studioConfiguration, DataSource dataSource,
                                InstanceService instanceService,
                                RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        super(target);
        this.studioConfiguration = studioConfiguration;
        this.dataSource = dataSource;
        this.instanceService = instanceService;
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public String getCurrentConfigName() {
        return currentConfigName;
    }

    public void setCurrentConfigName(String currentConfigName) {
        this.currentConfigName = currentConfigName;
    }

    public String getCurrentConfigPath() {
        return currentConfigPath;
    }

    public void setCurrentConfigPath(String currentConfigPath) {
        this.currentConfigPath = currentConfigPath;
    }

    /**
     * Indicates if the upgrade is for a specific configuration file.
     */
    public boolean isConfigPresent() {
        return isNoneEmpty(currentConfigName, currentConfigPath);
    }

    public void clearCurrentConfig() {
        currentConfigName = null;
        currentConfigPath = null;
    }

    /**
     * Returns the absolute path of the repository being upgraded.
     */
    public Path getRepositoryPath() {
        Path path;
        if(isEmpty(target)) {
            path = Paths.get(
                    studioConfiguration.getProperty(REPO_BASE_PATH),
                    studioConfiguration.getProperty(GLOBAL_REPO_PATH)
            );
        } else {
            path = Paths.get(
                    studioConfiguration.getProperty(REPO_BASE_PATH),
                    studioConfiguration.getProperty(SITES_REPOS_PATH),
                    target,
                    studioConfiguration.getProperty(SANDBOX_PATH)
            );
        }
        return path.toAbsolutePath();
    }

    /**
     * Returns the relative path of the file based on the site repository
     */
    public String getRelativePath(Path file) {
        return getRepositoryPath().relativize(file).toString();
    }

    /**
     * Commits all changes for the given files in the repository of the site being upgraded.
     * @param message the commit message
     * @param changedFiles the list of changed files
     * @param deletedFiles the list of deleted files
     */
    public void commitChanges(String message, List<String> changedFiles, List<String> deletedFiles) throws Exception {
        Path repositoryPath = getRepositoryPath();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .setGitDir(repositoryPath.resolve(GIT_ROOT).toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {
            // Add new & updated files
            if (CollectionUtils.isNotEmpty(changedFiles)) {
                AddCommand add = git.add();
                changedFiles.stream().map(path -> removeStart(path, File.separator)).forEach(add::addFilepattern);
                retryingRepositoryOperationFacade.call(add);
            }

            // Add deleted files
            if (CollectionUtils.isNotEmpty(deletedFiles)) {
                AddCommand add = git.add();
                add.setUpdate(true);
                deletedFiles.stream().map(path -> removeStart(path, File.separator)).forEach(add::addFilepattern);
                retryingRepositoryOperationFacade.call(add);
            }

            StatusCommand statusCommand = git.status();
            Status status = retryingRepositoryOperationFacade.call(statusCommand);

            if (!status.isClean()) {
                CommitCommand commitCommand = git.commit()
                        .setMessage(message + "\n\n" + getIdentifier());
                retryingRepositoryOperationFacade.call(commitCommand);
            }
        }
    }

    /**
     * Returns the identifier for this particular Studio instance
     */
    protected String getIdentifier() {
        var activeEnvironment = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
        var identifier=  format(COMMIT_IDENTIFIER_FORMAT, instanceService.getInstanceId(), activeEnvironment,
                System.getProperty("user.name"));
        return Base64.getEncoder().encodeToString(identifier.getBytes(UTF_8));
    }

    /**
     * Returns the file as an absolute path
     */
    public Path getFile(String path) {
        return getRepositoryPath().resolve(removeStart(path, File.separator));
    }

    @Override
    public String toString() {
        return (isConfigPresent()? getCurrentConfigPath() + " @ " : "") + (isEmpty(target)? "global repo" : target);
    }

}
