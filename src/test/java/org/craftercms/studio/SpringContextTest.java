/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.*;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath*:crafter/engine/services/main-services-context.xml"})
@TestPropertySource(locations = "classpath:crafter/studio/test-application.yaml")
public class SpringContextTest extends AbstractTestNGSpringContextTests{

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
