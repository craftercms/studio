package org.craftercms.studio.impl.v2.upgrade.upgraders;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeContext;
import org.craftercms.studio.api.v2.upgrade.Upgrader;
import org.springframework.core.io.Resource;

import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.GLOBAL_REPO_PATH;

public class GlobalRepoUpgrader implements Upgrader {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRepoUpgrader.class);

    protected String[] files;

    @Override
    public void init(final Configuration config) {
        files = (String[]) config.getArray(String.class, "files");
    }

    @Override
    public void execute(final UpgradeContext context) throws UpgradeException {
        Resource globalConfigurationBootstrap = context.getServletResource(UrlUtils.concat(
            FILE_SEPARATOR,
            BOOTSTRAP_REPO_PATH,
            context.getProperty(GLOBAL_REPO_PATH),
            FILE_SEPARATOR)
        );

        logger.info("Upgrading global repo files");

        for(String file : files) {
            logger.debug("Upgrading configuration file: {0}", file);
            try (InputStream is = globalConfigurationBootstrap.createRelative(file).getInputStream()) {

                context.writeToRepo(StringUtils.EMPTY, file, is, "Global Repo Upgrade");

            } catch (IOException e) {
                throw new UpgradeException("Upgrade for global repo failed", e);
            }
        }

    }
}
