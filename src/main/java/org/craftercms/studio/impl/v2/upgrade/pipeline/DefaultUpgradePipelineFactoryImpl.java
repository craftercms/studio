package org.craftercms.studio.impl.v2.upgrade.pipeline;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.YamlConfiguration;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.UpgradePipelineFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.CONFIG_KEY_OPERATIONS;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.CONFIG_KEY_TYPE;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.CONFIG_KEY_VERSION;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.CONFIG_PREFIX_PIPELINE;

public class DefaultUpgradePipelineFactoryImpl implements UpgradePipelineFactory, ApplicationContextAware {

    protected Resource configurationFile;
    protected ApplicationContext appContext;

    protected String pipelineName;

    @Required
    public void setPipelineName(final String pipelineName) {
        this.pipelineName = pipelineName;
    }

    @Required
    public void setConfigurationFile(final Resource configurationFile) {
        this.configurationFile = configurationFile;
    }

    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

    public HierarchicalConfiguration loadUpgradeConfiguration() throws UpgradeException {
        YamlConfiguration configuration = new YamlConfiguration();
        try (InputStream is = configurationFile.getInputStream()) {
            configuration.read(is);
        } catch (Exception e) {
            throw  new UpgradeException("Error reading configuration file", e);
        }
        return configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UpgradePipeline getPipeline(final String currentVersion) throws UpgradeException {
        List<UpgradeOperation> operations = new LinkedList<>();
        HierarchicalConfiguration config = loadUpgradeConfiguration();
        List<HierarchicalConfiguration> pipeline = config.configurationsAt(CONFIG_PREFIX_PIPELINE + pipelineName);

        boolean versionFound = false;
        for(HierarchicalConfiguration version : pipeline) {
            if(!version.getString(CONFIG_KEY_VERSION).equals(currentVersion)) {
                if(versionFound) {
                    List<HierarchicalConfiguration> operationsConfig = version.configurationsAt(CONFIG_KEY_OPERATIONS);
                    operationsConfig.forEach(operationConfig -> {
                        UpgradeOperation operation =
                            appContext.getBean(operationConfig.getString(CONFIG_KEY_TYPE), UpgradeOperation.class);
                        operation.init(operationConfig);
                        operations.add(operation);
                    });
                }
            } else {
                versionFound = true;
            }
        }
        return new DefaultUpgradePipelineImpl(operations);
    }

}
