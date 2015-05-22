package org.craftercms.studio.testing.it.base.rest;


import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeSuite;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public abstract class BaseRestTest {

    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties properties;

    private static final String baseUrl;

    static {
        properties = new Properties();
        initProperties();
        baseUrl = properties.getProperty("selenium.baseUrl");
    }

    @BeforeSuite
    public  void beforeClass() {

    }

    protected static String getBaseUrl() {
        return baseUrl;
    }

    private static void initProperties() {
        InputStream is = BaseRestTest.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (is != null) {
            try {
                properties.load(is);
            } catch (Exception ignored) {
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }
}
