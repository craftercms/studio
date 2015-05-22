package org.craftercms.studio.testing.ui.site;

import org.apache.commons.lang3.RandomStringUtils;
import org.craftercms.studio.testing.base.ui.BaseSeleniumTest;
import org.craftercms.studio.testing.base.ui.pages.HomePage;
import org.craftercms.studio.testing.base.ui.pages.LoginPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class CreateSiteTest extends BaseSeleniumTest {

    HomePage homePage = new HomePage(this);
    LoginPage loginPage = new LoginPage(this);
    private String siteName;

    @BeforeClass
    public void beforeClass() {
        siteName = RandomStringUtils.randomAlphabetic(7);
    }

    @Test
    public void testClickCreateSite() throws Exception {
        open(getBaseUrl());
        homePage.clickCreateSite();
        assertTrue(homePage.isElementPresent(HomePage.SITE_NAME_INPUT));
        assertTrue(homePage.isElementPresent(HomePage.SITE_ID_INPUT));
        assertTrue(homePage.isElementPresent(HomePage.SITE_DESCRIPTION_INPUT));
        assertTrue(homePage.isElementPresent(HomePage.SITE_BP_INPUT));
        createFillCreateSiteFrom();
        createCreateSite();
        deleteSite();
    }


    public void createFillCreateSiteFrom() {
        homePage.fillTestSiteInfo(siteName);
        assertEquals(homePage.getElementText(HomePage.SITE_NAME_INPUT), homePage.getElementText(HomePage
            .SITE_ID_INPUT));
        assertFalse(homePage.isElementDisplayedClass(HomePage.SITE_NAME_ERROR_MSG));
    }

    public void createCreateSite() {
        homePage.clickCreateSiteBtn();
        loginPage.waitElementPresent(LoginPage.LOGIN_BTN_PATH);
        loginPage.fillValidLoginInfo();
        loginPage.login();
        assertEquals(homePage.getElementText(homePage.buildPathForSite(siteName)),siteName);
    }


    public void deleteSite() throws InterruptedException {
        homePage.waitElementClickable(homePage.buildPathForSiteDelete(siteName));
        homePage.clickDeleteSite(siteName);
        homePage.waitElementVisible(HomePage.SITE_DELETE_DIALOG);
        homePage.waitElementClickable(HomePage.SITE_DELETE_OK);
        homePage.clickDeleteSiteDialog();
     //   assertFalse(homePage.isTextContained(siteName));
    }

}
