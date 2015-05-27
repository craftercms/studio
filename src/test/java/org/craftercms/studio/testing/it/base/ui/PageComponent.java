package org.craftercms.studio.testing.it.base.ui;
import org.openqa.selenium.WebDriver;

public class PageComponent {
    private final BaseSeleniumTest currentSeleniumTest;

    public PageComponent(BaseSeleniumTest test) {
        currentSeleniumTest = test;
    }

    public BaseSeleniumTest getCurrentSeleniumTest() {
        return currentSeleniumTest;
    }

    public WebDriver getDriver() {
        return getCurrentSeleniumTest().getDriver();
    }

}
