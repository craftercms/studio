package org.craftercms.studio;

import org.craftercms.engine.service.impl.SiteItemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.*;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

@Test
@ContextConfiguration(locations = {"classpath:crafter/engine/services/main-services-context.xml"})
@WebAppConfiguration
public class SpringContextTest extends AbstractTestNGSpringContextTests{

    @BeforeTest
    public static void beforeTest(){
        System.setProperty("crafter.engine.extension.base", "classpath*:crafter/studio/extension");
    }

    @AfterTest
    public static void afterTest(){
        System.clearProperty("crafter.engine.extension.base");
    }

    @Autowired
    SiteItemServiceImpl siteItemServiceImpl;

    @Test
    public void TestOk(){
        assertTrue(true);
        assertNotNull(siteItemServiceImpl);
    }
}
