package org.craftercms.studio.impl.v1.util;

import java.io.InputStream;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class ConfigUtils {

    public static HierarchicalConfiguration<ImmutableNode> readXmlConfiguration(InputStream input)
            throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<XMLConfiguration> builder =
                new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
        XMLConfiguration config = builder.configure(params.xml()).getConfiguration();
        FileHandler fileHandler = new FileHandler(config);

        fileHandler.setEncoding("UTF-8");
        fileHandler.load(input);

        return config;
    }

    private ConfigUtils() {
    }

}
