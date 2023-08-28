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

package org.craftercms.studio.test.util;

import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.junit.RepositoryTestCase;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockMakers;
import org.mockito.Spy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class BaseRepositoryTestCase extends RepositoryTestCase {
    protected static final String ORIGINAL_FILE_NAME = "test.txt";
    protected static final String RENAMED_1_FILE_NAME = "test2.txt";
    protected static final String RENAMED_2_FILE_NAME = "test3.txt";
    protected static final String NON_EXISTENT_FILE_NAME = "non-existent.txt";
    protected static final String HEAD = "HEAD";
    protected static final String MASTER = "master";

    @Mock(mockMaker = MockMakers.SUBCLASS)
    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    @Spy
    @InjectMocks
    protected GitRepositoryHelper helper;

    private AutoCloseable mocks;

    protected RevCommit firstCommit;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mocks = initMocks();
        when(retryingRepositoryOperationFacade.call(any(GitCommand.class))).thenAnswer(invocation -> {
            GitCommand<?> gitCommand = invocation.getArgument(0);
            return gitCommand.call();
        });
        firstCommit = commitNewVersion(ORIGINAL_FILE_NAME, "v1");
        commitNewVersion(ORIGINAL_FILE_NAME, "v2");
        commitNewVersion(ORIGINAL_FILE_NAME, "v3");
        rename(ORIGINAL_FILE_NAME, RENAMED_1_FILE_NAME, "v3");
        commitNewVersion(RENAMED_1_FILE_NAME, "v4");
        commitNewVersion(RENAMED_1_FILE_NAME, "v5");
        rename(RENAMED_1_FILE_NAME, RENAMED_2_FILE_NAME, "v5");
        commitNewVersion(RENAMED_2_FILE_NAME, "v6");
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mocks.close();
    }

    private void rename(String oldName, String newName, String version) throws Exception {
        writeTrashFile(newName, "This is a test file " + version);
        deleteTrashFile(oldName);
        try (Git git = new Git(db)) {
            git.add().addFilepattern(newName).call();
            git.rm().addFilepattern(oldName).call();
            git.commit()
                    .setMessage("Renamed " + oldName + " to " + newName)
                    .call();
        }
    }

    private RevCommit commitNewVersion(String path, String version) {
        tick();
        RevCommit commit = commitFile(path, "This is a test file " + version, MASTER);
        return commit;
    }

    protected abstract AutoCloseable initMocks();
}
