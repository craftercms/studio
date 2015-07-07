package org.craftercms.studio.testing.it.base.ui.pages;

import org.craftercms.studio.testing.it.base.ui.BaseSeleniumTest;
import org.craftercms.studio.testing.it.base.ui.Page;


/**

 */
public class HomePage extends Page{

    public static final String PATH = "/#/sites";

    public static final String CREATE_SITE_BUTTON="css:#container > div > div > div.nav-panel > div.m10 > button";
    public static final String CREATE_SITE_FORM_BTN="css:#container > div > div > div.action-area.ng-scope > div > div"
        + " > form > div:nth-child(5) > button.btn.btn-primary";
    public static final String SITE_NAME_ERROR_MSG="css:#container > div > div > div.action-area.ng-scope > div > div"
        + " > "
        + "form > div:nth-child(1) > div > small";
    private static final String SITES_TABLE_ID="xpath://td[text()=\"#\"]";
    public static final String SITES_TABLE_DELETE="xpath://td[text()=\"#\"]/following-sibling::td[4]/a";
    public static final String SITE_DELETE_DIALOG="css:body > div.modal.fade.ng-isolate-scope.in > div > div";
    public static final String SITE_DELETE_OK="css:body > div.modal.fade.ng-isolate-scope.in > div > div > div"
        + ".modal-footer.ng-scope > button.btn.btn-primary";
    public static final String SITE_NAME_INPUT = "id:name" ;
    public static final String SITE_DESCRIPTION_INPUT = "id:description" ;
    public static final String  SITE_ID_INPUT = "id:siteId";
    public static final String SITE_BP_INPUT = "blueprint";
    private static final String TEST_SITE_DESC = "Crafter Selenium Test";
    private static final String TEST_SITE_BP = "1"; // Corporate Bp;

    public String getPath(){
        return PATH;
    }

    public HomePage(BaseSeleniumTest test) {
        super(test);
    }

    public void clickCreateSite() {
        click(CREATE_SITE_BUTTON);
    }

    public void fillTestSiteInfo(final String siteName) {
        type(SITE_NAME_INPUT,siteName);
        type(SITE_DESCRIPTION_INPUT,TEST_SITE_DESC);
        selectItem(SITE_BP_INPUT,TEST_SITE_BP);
    }

    public void clickCreateSiteBtn() {
        click(CREATE_SITE_FORM_BTN);
    }

    public void clickDeleteSite(final String siteName) {
        click(buildPathForSiteDelete(siteName));
    }

    public void clickDeleteSiteDialog() {
        click(SITE_DELETE_OK);
    }

    public String buildPathForSite(String siteName){
        return SITES_TABLE_ID.replaceAll("#",siteName);
    }

    public String buildPathForSiteDelete(String siteName){
        return SITES_TABLE_DELETE.replaceAll("#",siteName);
    }

}
