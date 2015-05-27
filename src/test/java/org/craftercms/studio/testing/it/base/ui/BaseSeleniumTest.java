package org.craftercms.studio.testing.it.base.ui;


import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class BaseSeleniumTest {
    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties properties;
    private static final String phantomJsBinary;
    private static final String baseUrl;
    private static final int timeOut;
    private static WebDriver driver;

    @BeforeSuite
    public  void beforeClass() {
        String browser = properties.getProperty("selenium.browser");
        if (browser == null) browser = "phantom";
        assertNotNull("PhantomJS should be installed using 'mvn verify' before testing", phantomJsBinary);
        assertTrue(new File(phantomJsBinary).exists());
        driver = createDriver(browser);
        assertNotNull(driver,"Invalid browser: " + browser);
        driver.manage().window().maximize();
    }

    @AfterSuite
    public static void afterClass() {
        Iterator<String> it = driver.getWindowHandles().iterator();
        while (it.hasNext()) {
            driver.switchTo().window(it.next());
            driver.close();
        }
        driver.quit();
    }

    public BaseSeleniumTest() {

    }

    public WebDriver  getDriver ()      { return driver; }
    public Properties getProperties ()  { return properties; }
    public String     getBaseUrl ()     { return baseUrl; }
    public int        getTimeOut()      {return timeOut;}

    public void open(String url) {
        getDriver().get(url);
    }

    private static void initProperties() {
        InputStream is = BaseSeleniumTest.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (is != null) {
            try {
                properties.load(is);
            } catch (Exception ignored) {
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    private static WebDriver createPhantomDriver() {
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability("takesScreenshot", false);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsBinary
        );
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                new String[]{"--web-security=false", "--ssl-protocol=any", "--ignore-ssl-errors=yes"}
        );
        return new PhantomJSDriver(capabilities);
    }

    //In order to use Safari, a selenium extension should be added to it
    //Go to https://github.com/SeleniumHQ/selenium/raw/master/javascript/safari-driver/prebuilt/SafariDriver.safariextz to download it
    private static WebDriver createSafariDriver() {
        DesiredCapabilities capabilities = DesiredCapabilities.safari();
        capabilities.setCapability(CapabilityType. ACCEPT_SSL_CERTS,true);
        return new SafariDriver(capabilities);
    }

    // In order to run using chrome, the corresponding chromedriver file should be in the PATH
    // http://chromedriver.storage.googleapis.com/index.html?path=2.13/
    private static WebDriver createChromeDriver() {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        return new ChromeDriver(capabilities);
    }

    // Don't work on latest version, please downgrade or test a new one :)
    // https://ftp.mozilla.org/pub/mozilla.org/firefox/releases/31.4.0esr/mac/en-US/Firefox%2031.4.0esr.dmg
    private static WebDriver createFirefoxDriver() {
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        return new FirefoxDriver(capabilities);
    }

    private static WebDriver createIEDriver() {
        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
        return new InternetExplorerDriver(capabilities);
    }

    private static WebDriver createDriver(String browser) {
        try {
            if ("phantom".equals(browser)) {
                return createPhantomDriver();
            } else if ("safari".equals(browser)) {
                return createSafariDriver();
            } else if ("chrome".equals(browser)) {
                return createChromeDriver();
            } else if ("firefox".equals(browser)) {
                return createFirefoxDriver();
            } else if ("ie".equals(browser)) {
                return createIEDriver();
            }
        } catch (Exception e) {e.printStackTrace();}
        return null;
    }



    static {
        properties = new Properties();
        initProperties();
        phantomJsBinary = properties.getProperty("phantomjs.binary");
        baseUrl = properties.getProperty("selenium.baseUrl");
        timeOut= Integer.parseInt(properties.get("selenium.componentTimeOut").toString());
    }

}
