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

package org.craftercms.studio.impl.v2.upgrade.operations.plugin;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.config.DisableClassLoadingConstructor;
import org.craftercms.commons.plugin.PluginDescriptorReader;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author joseross
 * @since 3.1.1
 */
public abstract class AbstractPluginDescriptorUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPluginDescriptorUpgradeOperation.class);

    public static final String CONFIG_KEY_DESCRIPTOR_PATH = "descriptorPath";
    public static final String CONFIG_KEY_DESCRIPTOR_VERSION = "descriptorVersion";

    protected PluginDescriptorReader descriptorReader;

    protected String descriptorPath;
    protected String descriptorVersion;

    public AbstractPluginDescriptorUpgradeOperation(StudioConfiguration studioConfiguration,
                                                    PluginDescriptorReader descriptorReader) {
        super(studioConfiguration);
        this.descriptorReader = descriptorReader;
    }

    @Override
    protected void doInit(final HierarchicalConfiguration config) {
        descriptorPath = config.getString(CONFIG_KEY_DESCRIPTOR_PATH);
        descriptorVersion = config.getString(CONFIG_KEY_DESCRIPTOR_VERSION);
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        Path descriptorFile = context.getRepositoryPath().resolve(descriptorPath);
        if (Files.notExists(descriptorFile)) {
            logger.info("Plugin descriptor file not found for site {0}", site);
            return;
        }
        try (Reader reader = Files.newBufferedReader(descriptorFile)) {
            PluginDescriptor descriptor = descriptorReader.read(reader);
            if (descriptor.getDescriptorVersion().equals(descriptorVersion)) {
                logger.info("Plugin descriptor already update for site " + site);
                return;
            }
            logger.info("Updating plugin descriptor for site " + site);
            doPluginDescriptorUpdates(descriptor);
            descriptor.setDescriptorVersion(descriptorVersion);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(
                    new DisableClassLoadingConstructor(),
                    new Representer() {
                        @Override
                        protected NodeTuple representJavaBeanProperty(final Object javaBean, final Property property,
                                                                      final Object propertyValue, final Tag customTag) {
                            if (propertyValue != null) {
                                return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                            }
                            return null;
                        }
                    },
                    options);
            String content = yaml.dumpAsMap(descriptor);

            Files.writeString(context.getFile(descriptorPath), content);
            trackChangedFiles(descriptorPath);
        } catch (Exception e) {
            throw new UpgradeException("Plugin descriptor can't be read for site " + site);
        }
    }

    protected abstract void doPluginDescriptorUpdates(PluginDescriptor descriptor) throws UpgradeException;

}
