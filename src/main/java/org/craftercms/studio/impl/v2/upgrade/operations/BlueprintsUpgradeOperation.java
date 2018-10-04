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
import org.craftercms.commons.monitoring.VersionMonitor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
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
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.BLUEPRINTS_UPDATED_COMMIT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;

public class BlueprintsUpgradeOperation extends AbstractUpgradeOperation implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BlueprintsUpgradeOperation.class);

    private static final String STUDIO_MANIFEST_LOCATION = "/META-INF/MANIFEST.MF";

    protected ServletContext servletContext;
    protected SecurityProvider securityProvider;
    protected ServicesConfig servicesConfig;

    @Override
    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Required
    public void setSecurityProvider(final SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Required
    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    @Override
    public void execute(final String site) throws UpgradeException {
        try {
            GitContentRepositoryHelper helper =
                new GitContentRepositoryHelper(studioConfiguration, securityProvider, servicesConfig);
            Path globalConfigPath = helper.buildRepoPath(GitRepositories.GLOBAL);
            Path blueprintsPath = Paths.get(globalConfigPath.toAbsolutePath().toString(),
                studioConfiguration.getProperty(BLUE_PRINTS_PATH));

            String studioManifestLocation = servletContext.getRealPath(STUDIO_MANIFEST_LOCATION);
            String blueprintsManifestLocation =
                Paths.get(blueprintsPath.toAbsolutePath().toString(), "BLUEPRINTS.MF").toAbsolutePath().toString();
            boolean blueprintManifestExists = Files.exists(Paths.get(blueprintsManifestLocation));
            InputStream studioManifestStream = FileUtils.openInputStream(new File(studioManifestLocation));
            Manifest studioManifest = new Manifest(studioManifestStream);
            VersionMonitor studioVersion = VersionMonitor.getVersion(studioManifest);
            InputStream blueprintsManifestStream = null;
            Manifest blueprintsManifest = null;
            VersionMonitor blueprintsVersion = null;
            if (blueprintManifestExists) {
                blueprintsManifestStream = FileUtils.openInputStream(new File(blueprintsManifestLocation));
                blueprintsManifest = new Manifest(blueprintsManifestStream);
                blueprintsVersion = VersionMonitor.getVersion(blueprintsManifest);
            }

            if (!blueprintManifestExists || !StringUtils.equals(studioVersion.getBuild(), blueprintsVersion.getBuild())
                || (StringUtils.equals(studioVersion.getBuild(), blueprintsVersion.getBuild()) &&
                !StringUtils.equals(studioVersion.getBuild_date(), blueprintsVersion.getBuild_date()))) {
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
                    git.commit().setAll(true).setMessage(BLUEPRINTS_UPDATED_COMMIT).call();
                }
            } catch (GitAPIException err) {
                logger.error("error creating initial commit for global configuration", err);
            }
        } catch (Exception e) {
            throw new UpgradeException("Error upgrading blueprints in the global repo", e);
        }
    }

}
