/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        System.setProperty("crafter.bin.dir", "target/test-resources/bin");
        System.setProperty("crafter.data.dir", "target/test-resources/data");
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
