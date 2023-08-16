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

package org.craftercms.studio.api.v2.utils;

import org.craftercms.studio.api.v2.exception.git.NoChangesForPathException;
import org.craftercms.studio.test.util.BaseRepositoryTestCase;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GitRepositoryHelperTest extends BaseRepositoryTestCase {

    @Override
    protected AutoCloseable initMocks() {
        return MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNoChangesForFile() {
        assertThrows(NoChangesForPathException.class, () ->
                helper.getDiffEntry(db, db.resolve(HEAD), NON_EXISTENT_FILE_NAME));
        assertThrows(NoChangesForPathException.class, () -> helper.getDiffEntry(db, db.resolve(HEAD), RENAMED_1_FILE_NAME));
        assertThrows(NoChangesForPathException.class, () -> helper.getDiffEntry(db, db.resolve(HEAD), ORIGINAL_FILE_NAME));
    }

    @Test
    public void testInitialCommitChanges() throws GitAPIException, NoChangesForPathException, IOException {
        DiffEntry diffEntry = helper.getDiffEntry(db, firstCommit, ORIGINAL_FILE_NAME);
        assertEquals(ORIGINAL_FILE_NAME, diffEntry.getNewPath(), "First commit should contain original file name");
        assertEquals(DiffEntry.ChangeType.ADD, diffEntry.getChangeType(), "First commit should have an ADD change type");
    }

}
