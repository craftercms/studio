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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.ServletContextResource;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

public abstract class AbstractUpgradeOperation implements UpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(AbstractUpgradeOperation.class);

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

    protected Resource getServletResource(final String path) {
        return new ServletContextResource(servletContext, path);
    }

    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    protected void writeToRepo(String site, String path, InputStream content, String message) {
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
                    commit = git.commit().setOnly(gitPath).setMessage(message).call();
                    commit.getName();
                }
            } catch (GitAPIException e) {
                logger.error("error adding file to git: site: " + site + " path: " + path, e);
            }

        } catch (IOException e) {
            logger.error("error writing file: site: " + site + " path: " + path, e);
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

    private Path getRepositoryPath(String site) {
        if(StringUtils.isEmpty(site)) {
            return Paths.get(
                studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH),
                GIT_ROOT
            );
        } else {
            return Paths.get(
                studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH),
                site,
                studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH),
                GIT_ROOT
            );
        }
    }

}
