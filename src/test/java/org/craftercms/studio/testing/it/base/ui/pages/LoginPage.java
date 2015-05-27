package org.craftercms.studio.testing.it.base.ui.pages;

import org.craftercms.studio.testing.it.base.ui.BaseSeleniumTest;
import org.craftercms.studio.testing.it.base.ui.Page;

/**
 * Created by cortiz on 5/20/15.
 */
public class LoginPage  extends Page {

    public static final String PATH = "/login";
    public static final String LOGIN_MODAL = "loginView";
    public static final String LOGIN_USER_INPUT="id:username";
    public static final String LOGIN_PWD_INPUT="id:password";
    public static final String LOGIN_BTN_PATH="css:#loginView > div > div:nth-child(4) > div > button";

    public LoginPage(final BaseSeleniumTest test) {
        super(test);
    }

    @Override
    public String getPath() {
        return PATH;
    }

    public void fillValidLoginInfo() {
        type(LOGIN_USER_INPUT,"admin");
        type(LOGIN_PWD_INPUT,"admin");
    }

    public void login() {
        click(LOGIN_BTN_PATH);
    }
}
