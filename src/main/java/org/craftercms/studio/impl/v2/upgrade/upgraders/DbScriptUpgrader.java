package org.craftercms.studio.impl.v2.upgrade.upgraders;

import java.sql.SQLException;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeContext;
import org.craftercms.studio.api.v2.upgrade.Upgrader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class DbScriptUpgrader implements Upgrader {

    private static final Logger logger = LoggerFactory.getLogger(DbScriptUpgrader.class);

    protected Resource dbScript;

    @Override
    public void init(final Configuration config) {
        dbScript = new ClassPathResource(config.getString("filename"));
    }

    @Override
    public void execute(final UpgradeContext context) throws UpgradeException {
        logger.info("Executing db script {0}", dbScript.getFilename());
        try {
            ScriptUtils.executeSqlScript(context.getConnection(), dbScript);
            // TODO: update integrity value
        } catch (SQLException e) {
            logger.error("Error executing db script", e);
            throw new UpgradeException("Error executing sql script " + dbScript.getFilename(), e);
        }
    }

}
