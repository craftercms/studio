package org.craftercms.studio.impl.v2.upgrade.upgraders;

import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.configuration2.Configuration;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeContext;
import org.craftercms.studio.api.v2.upgrade.Upgrader;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DbScriptUpgrader implements Upgrader {

    private static final Logger logger = LoggerFactory.getLogger(DbScriptUpgrader.class);

    protected Resource dbScript;
    protected boolean updateIntegrity;

    protected DbIntegrityValidator integrityValidator;

    public void setUpdateIntegrity(final boolean updateIntegrity) {
        this.updateIntegrity = updateIntegrity;
    }

    @Required
    public void setIntegrityValidator(final DbIntegrityValidator integrityValidator) {
        this.integrityValidator = integrityValidator;
    }

    @Override
    public void init(final Configuration config) {
        dbScript = new ClassPathResource(config.getString("script"));
        updateIntegrity = config.getBoolean("updateIntegrity", true);
    }

    @Override
    public void execute(final UpgradeContext context) throws UpgradeException {
        logger.info("Executing db script {0}", dbScript.getFilename());
        try (Reader reader = new InputStreamReader(dbScript.getInputStream())) {
            ScriptRunner scriptRunner = new ScriptRunner(context.getConnection());
            scriptRunner.setDelimiter(" ;");
            scriptRunner.setStopOnError(true);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(reader);
            context.getConnection().commit();
            if(updateIntegrity) {
                integrityValidator.store(context.getConnection());
            }
        } catch (Exception e) {
            logger.error("Error executing db script", e);
            throw new UpgradeException("Error executing sql script " + dbScript.getFilename(), e);
        }
    }

}
