package org.craftercms.studio;

import org.craftercms.engine.service.impl.SiteItemServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

@Test
@ContextConfiguration(locations = { "classpath:crafter/engine/rendering/main-rendering-context.xml","classpath:crafter/engine/services/main-services-context.xml" })
public class SpringContextTest extends AbstractTestNGSpringContextTests{

    @Autowired
    SiteItemServiceImpl siteItemServiceImpl;

    @Test
    public void TestOk(){
        assertTrue(true);
        assertNotNull(siteItemServiceImpl);
    }
}
