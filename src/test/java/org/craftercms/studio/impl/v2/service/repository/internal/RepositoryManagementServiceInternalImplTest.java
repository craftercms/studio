/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.service.repository.internal;

import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.dal.repository.RepositoryStatus;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_POSTSCRIPT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_PROLOGUE;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class RepositoryManagementServiceInternalImplTest {

	private static final String SITE_ID = "testSite";
	private static final String COMMIT_MESSAGE = "Test commit message";
	private static final String GIT_LOCK_KEY = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, SITE_ID);

	@Mock
	private GitRepositoryHelper gitRepositoryHelper;

	@Mock
	private GeneralLockService generalLockService;

	@Mock
	private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

	@Mock
	private UserService userServiceInternal;

	@Mock
	private StudioConfiguration studioConfiguration;

	@Mock
	private Repository repository;

	@Mock
	private StatusCommand statusCommand;

	@Mock
	private RmCommand rmCommand;

	@Mock
	private AddCommand addCommand;

	@Mock
	private CommitCommand commitCommand;

	@Mock
	private Status status;

	@Mock
	private User user;

	@Mock
	private PersonIdent personIdent;

	@InjectMocks
	private RepositoryManagementServiceInternalImpl repositoryManagementServiceInternal;

	private MockedConstruction<Git> git;

	private AutoCloseable mocks;

	@Before
	public void setUp() throws Exception {
		mocks = openMocks(this);
		when(gitRepositoryHelper.getRepository(SITE_ID, GitRepositories.SANDBOX)).thenReturn(repository);
		when(retryingRepositoryOperationFacade.call(statusCommand)).thenReturn(status);
		when(commitCommand.setCommitter(personIdent)).thenReturn(commitCommand);
		when(commitCommand.setAuthor(personIdent)).thenReturn(commitCommand);

		git = mockConstruction(Git.class, (mock, context) -> {
			when(mock.status()).thenReturn(statusCommand);
			when(mock.rm()).thenReturn(rmCommand);
			when(mock.add()).thenReturn(addCommand);
			when(mock.commit()).thenReturn(commitCommand);
		});
	}

	@After
	public void tearDown() throws Exception {
		mocks.close();
		git.close();
	}

	@Test
	public void testCommitResolutionNoUncommittedChanges() throws Exception {
		when(status.hasUncommittedChanges()).thenReturn(false);

		RepositoryStatus result = repositoryManagementServiceInternal.commitResolution(SITE_ID, COMMIT_MESSAGE);

		assertNotNull(result);
		verify(generalLockService).lock(GIT_LOCK_KEY);
		verify(generalLockService).unlock(GIT_LOCK_KEY);
	}

	@Test
	public void testCommitResolutionWithUncommittedChanges() throws Exception {
		when(status.hasUncommittedChanges()).thenReturn(true);
		when(status.getMissing()).thenReturn(new HashSet<>(List.of("file_missing_1, file_missing_2")));
		when(status.getUncommittedChanges()).thenReturn(new HashSet<>(Arrays.asList("file_missing_1", "file_missing_2", "file1", "file2")));
		when(userServiceInternal.getUserByIdOrUsername(-1, "testUser")).thenReturn(user);
		when(gitRepositoryHelper.getAuthorIdent(user)).thenReturn(personIdent);
		when(studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_PROLOGUE)).thenReturn("");
		when(studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_POSTSCRIPT)).thenReturn("");

		try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
			securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("testUser");
			RepositoryStatus result = repositoryManagementServiceInternal.commitResolution(SITE_ID, COMMIT_MESSAGE);
			assertNotNull(result);
			verify(generalLockService).lock(GIT_LOCK_KEY);
			verify(generalLockService).unlock(GIT_LOCK_KEY);
			verify(retryingRepositoryOperationFacade).call(rmCommand);
			verify(retryingRepositoryOperationFacade).call(addCommand);
			verify(retryingRepositoryOperationFacade).call(commitCommand);
		}
	}

	@Test
	public void testCommitResolutionExceptionThrown() throws UserNotFoundException, ServiceLayerException {
		try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
			securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("not_exist_user");
			when(status.hasUncommittedChanges()).thenReturn(true);
			when(userServiceInternal.getUserByIdOrUsername(-1, "not_exist_user")).thenThrow(new ServiceLayerException("Test exception"));

			assertThrows(ServiceLayerException.class, () -> repositoryManagementServiceInternal.commitResolution(SITE_ID, COMMIT_MESSAGE));

			verify(generalLockService).lock(GIT_LOCK_KEY);
			verify(generalLockService).unlock(GIT_LOCK_KEY);
		}
	}
}
