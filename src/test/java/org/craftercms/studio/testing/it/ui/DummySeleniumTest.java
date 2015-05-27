package org.craftercms.studio.testing.it.ui;

import org.craftercms.studio.testing.it.base.ui.BaseSeleniumTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;


public class DummySeleniumTest extends BaseSeleniumTest {
    @Test
    public void alwaysOk() {
        assertTrue(true);
    }
}
