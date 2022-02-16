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
package org.craftercms.studio.impl.v2.upgrade;

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Base64;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext.COMMIT_IDENTIFIER_FORMAT;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

/**
 * @author joseross
 */
public class StudioUpgradeContextTest {

    private static final String INSTANCE_ID = "944c1a74-c2e4-4491-84db-9a0b2077b5b9";

    private static final String ENVIRONMENT = "testEnv";

    @Mock
    private StudioConfiguration studioConfiguration;

    @Mock
    private InstanceService instanceService;

    @InjectMocks
    private StudioUpgradeContext upgradeContext;

    @BeforeMethod
    public void setUp() throws ConfigurationException {
        initMocks(this);

        when(instanceService.getInstanceId()).thenReturn(INSTANCE_ID);
        when(studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE)).thenReturn(ENVIRONMENT);
    }

    @Test
    public void testCommitIdentifier() {
        var expectedIdentifier = Base64.getEncoder().encodeToString(format(
                COMMIT_IDENTIFIER_FORMAT, INSTANCE_ID, ENVIRONMENT, System.getProperty("user.name")).getBytes(UTF_8));

        assertEquals(upgradeContext.getIdentifier(), expectedIdentifier);
    }

}
