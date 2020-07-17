/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.upgrade.operations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.GLOBAL_REPO_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SANDBOX_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

/**
 * Provides access to system components for all upgrade operations.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>currentVersion</strong>: (required) the version number that will be upgraded</li>
 *     <li><strong>nextVersion</strong> (required) the version number to use after the upgrade</li>
 *     <li><strong>commitDetails</strong>(optional) any additional details to include in the commits if there are
 *     repository changes</li>
 * </ul>
 *
 * @author joseross
 */
public abstract class AbstractUpgradeOperation implements UpgradeOperation, ServletContextAware,
    ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(AbstractUpgradeOperation.class);

    public static final String CONFIG_KEY_COMMIT_DETAILS = "commitDetails";

    /**
     * The current version.
     */
    protected String currentVersion;

    /**
     * The next version.
     */
    protected String nextVersion;

    /**
     * Additional details for the commit message (optional)
     */
    protected String commitDetails;

    /**
     * The Studio configuration.
     */
    protected StudioConfiguration studioConfiguration;

    /**
     * The database data source.
     */
    protected DataSource dataSource;

    /**
     * The content repository.
     */
    protected ContentRepository contentRepository;

    /**
     * The servlet context.
     */
    protected ServletContext servletContext;

    /**
     * The application context
     */
    protected ApplicationContext applicationContext;

    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public String getProperty(String key) {
        return studioConfiguration.getProperty(key);
    }

    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void init(final String sourceVersion, final String targetVersion,
                     final HierarchicalConfiguration<ImmutableNode> config) throws UpgradeException {
        this.currentVersion = sourceVersion;
        this.nextVersion = targetVersion;
        this.commitDetails = config.getString(CONFIG_KEY_COMMIT_DETAILS);

        try {
            doInit(config);
        } catch (ConfigurationException e) {
            throw new UpgradeException("Error initializing operation", e);
        }
    }

    protected void doInit(final HierarchicalConfiguration<ImmutableNode> config) throws ConfigurationException {
        // do nothing by default
    }

    protected void writeToRepo(String site, String path, InputStream content) {
        try {
            Path repositoryPath = getRepositoryPath(site);
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repo = builder
                .setGitDir(repositoryPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();

            // Create basic file
            File file = new File(repo.getDirectory().getParent(), path);

            String gitPath = getGitPath(path);

            // Create parent folders
            File folder = file.getParentFile();
            if (folder != null && !folder.exists()) {
                folder.mkdirs();
            }

            // Create the file if it doesn't exist already
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        logger.error("error creating file: site: " + site + " path: " + path);
                    }
                } catch (IOException e) {
                    logger.error("error creating file: site: " + site + " path: " + path, e);
                }
            }

            // Write the bits
            try (FileChannel outChannel = new FileOutputStream(file.getPath()).getChannel()) {
                logger.debug("created the file output channel");
                ReadableByteChannel inChannel = Channels.newChannel(content);
                logger.debug("created the file input channel");
                long amount = 1024 * 1024; // 1MB at a time
                long count;
                long offset = 0;
                while ((count = outChannel.transferFrom(inChannel, offset, amount)) > 0) {
                    logger.debug("writing the bits: offset = " + offset + " count: " + count);
                    offset += count;
                }
            }


            // Add the file to git
            try (Git git = new Git(repo)) {
                git.add().addFilepattern(gitPath).call();

                Status status = git.status().addPath(gitPath).call();

                // TODO: SJ: Below needs more thought and refactoring to detect issues with git repo and report them
                if (status.hasUncommittedChanges() || !status.isClean()) {
                    RevCommit commit;
                    commit = git.commit().setOnly(gitPath).setMessage(getCommitMessage()).call();
                    commit.getName();
                }
            } catch (GitAPIException e) {
                logger.error("error adding file to git: site: " + site + " path: " + path, e);
            }

        } catch (IOException e) {
            logger.error("error writing file: site: " + site + " path: " + path, e);
        }
    }

    protected void commitAllChanges(String site) throws UpgradeException {
        try {
            Path repositoryPath = getRepositoryPath(site);
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repo = builder
                .setGitDir(repositoryPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();

            try (Git git = new Git(repo)) {
                git.add().addFilepattern(".").call();

                Status status = git.status().call();

                if (status.hasUncommittedChanges() || !status.isClean()) {
                    git.commit()
                        .setAll(true)
                        .setMessage(getCommitMessage())
                        .call();
                }
            }
        } catch (IOException | GitAPIException e) {
            throw new UpgradeException("Error committing changes for site " + site, e);
        }
    }

    protected String getCommitMessage() {
        String header = "Site upgrade from v" + currentVersion + " to v" + nextVersion;
        if(StringUtils.isNotEmpty(commitDetails)) {
            return header + ":\n" + commitDetails;
        } else {
            return header;
        }
    }

    private static String getGitPath(String path) {
        Path gitPath = Paths.get(path);
        gitPath = gitPath.normalize();
        try {
            gitPath = Paths.get(FILE_SEPARATOR).relativize(gitPath);
        } catch (IllegalArgumentException e) {
            logger.debug("Path: " + path + " is already relative path.");
        }
        if (StringUtils.isEmpty(gitPath.toString())) {
            return ".";
        }
        String toRet = gitPath.toString();
        toRet = FilenameUtils.separatorsToUnix(toRet);
        return toRet;
    }

    protected Path getRepositoryPath(String site) {
        if(StringUtils.isEmpty(site)) {
            return Paths.get(
                studioConfiguration.getProperty(REPO_BASE_PATH),
                studioConfiguration.getProperty(GLOBAL_REPO_PATH),
                GIT_ROOT
            );
        } else {
            return Paths.get(
                studioConfiguration.getProperty(REPO_BASE_PATH),
                studioConfiguration.getProperty(SITES_REPOS_PATH),
                site,
                studioConfiguration.getProperty(SANDBOX_PATH),
                GIT_ROOT
            );
        }
    }

    protected Resource loadResource(String path) {
        return applicationContext.getResource(path);
    }

}
