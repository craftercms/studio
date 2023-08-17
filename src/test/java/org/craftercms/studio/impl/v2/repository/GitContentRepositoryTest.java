/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.repository;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.test.util.BaseRepositoryTestCase;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;

public class GitContentRepositoryTest extends BaseRepositoryTestCase {

    public static final String SITE_NAME = "site1";

    @Mock
    protected GeneralLockService generalLockService;
    @Mock
    protected StudioConfiguration studioConfiguration;

    @InjectMocks
    GitContentRepository gitContentRepository;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        doReturn(db).when(helper).getRepository(SITE_NAME, SANDBOX);
    }

    @Override
    protected AutoCloseable initMocks() {
        return MockitoAnnotations.openMocks(this);
    }

    @Test
    public void contentHistoryFollowsRenameTest() throws GitAPIException, IOException {
        List<ItemVersion> history = gitContentRepository.getContentItemHistory(SITE_NAME, RENAMED_2_FILE_NAME);
        assertEquals("Most recent version name must be the same as the request path", history.get(0).getPath(), RENAMED_2_FILE_NAME);
        assertEquals("Oldest version name should be the original path", history.get(history.size() - 1).getPath(), ORIGINAL_FILE_NAME);
    }

    @Test
    public void cannotRevertBeforeRenamesTest() throws GitAPIException, IOException {
        List<ItemVersion> history = gitContentRepository.getContentItemHistory(SITE_NAME, RENAMED_2_FILE_NAME);
        boolean renameFound = false;
        for (ItemVersion itemVersion : history) {
            if (!StringUtils.equals(itemVersion.getPath(), RENAMED_2_FILE_NAME)) {
                renameFound = true;
            }
            if (renameFound) {
                assertFalse("Revertible versions should be the ones after the rename", itemVersion.isRevertible());
            }
        }
    }


}
