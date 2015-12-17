package org.craftercms.studio.testing.it.ui;

import org.testng.annotations.Test;
import org.craftercms.studio.testing.it.base.ui.BaseSeleniumTest;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;


public class SiteAvailabilityTest extends BaseSeleniumTest {

    @Test
    public void siteAvailable() {
        open(getBaseUrl());
        String title = getDriver().getTitle();
        assertNotNull("Invalid Page title (null), site down?,", title);
        assertNotSame(0,title.length(),"Invalid Page title (empty), site down?,");
    }
}
