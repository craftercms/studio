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

package org.craftercms.studio.impl.v2.service.security.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceInternalImplTest {

	private static final String INSTANCE_ID = "TEST INSTANCE ID";
	private static final String INVALID_INSTANCE_ID = "INVALID INSTANCE ID";
	private static final String EXTERNALLY_MANAGED_USERNAME = "jwickex";
	private static final String STUDIO_MANAGED_USERNAME = "jwick";
	private static final String NON_EXISTENT_USERNAME = "nonjwick";
	private static final String SALT = "CIPHER_SALT";
	private static final String ENCRTYPED_EMPTY_TOKEN = "THIS IS ACTUALLY EMPTY";

	@Mock
	private InstanceService instanceService;

	@Mock
	private UserDAO userDAO;

	@Spy
	@InjectMocks
	private UserServiceInternalImpl userService;

	@Before
	public void setup() throws UserNotFoundException, ServiceLayerException {
		when(instanceService.getInstanceId()).thenReturn(INSTANCE_ID);

		User managedUser = new User();
		managedUser.setExternallyManaged(false);
		User externallyManagedUser = new User();
		externallyManagedUser.setExternallyManaged(true);
		when(userDAO.getUserByIdOrUsername(any()))
			.thenAnswer(invocation -> {
				Map<String, Object> map = invocation.getArgument(0);
				if (STUDIO_MANAGED_USERNAME.equals(map.get(USERNAME))) {
					return managedUser;
				}
				if (EXTERNALLY_MANAGED_USERNAME.equals(map.get(USERNAME))) {
					return externallyManagedUser;
				}
				return null;
			});

	}

	@Test
	public void emptyTokenIsInvalidTest() throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		doReturn("").when(userService).decryptToken(ENCRTYPED_EMPTY_TOKEN);
		assertFalse(userService.validateToken(ENCRTYPED_EMPTY_TOKEN));
	}

	@Test
	public void onlyTokensWith4ElementsAreValidTest() throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		Long timestamp = System.currentTimeMillis();
		String token = format("%s|%s|%s|%s|something_else", STUDIO_MANAGED_USERNAME, INSTANCE_ID, timestamp, SALT);
		assertFalse(userService.validateDecryptedToken(token));

		token = format("%s|%s|%s", STUDIO_MANAGED_USERNAME, INSTANCE_ID, timestamp);
		assertFalse(userService.validateDecryptedToken(token));
	}

	@Test
	public void tokenIsInvalidIfUserDoesNotExistTest() throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		Long timestamp = System.currentTimeMillis();
		String token = format("%s|%s|%s|%s", NON_EXISTENT_USERNAME, INSTANCE_ID, timestamp, SALT);
		assertThrows(UserNotFoundException.class, () -> userService.validateDecryptedToken(token));
	}

	@Test
	public void tokenIsInvalidIfUserIsExternallyManagedTest() {
		Long timestamp = System.currentTimeMillis();
		String token = format("%s|%s|%s|%s", EXTERNALLY_MANAGED_USERNAME, INSTANCE_ID, timestamp, SALT);
		assertThrows(UserExternallyManagedException.class, () -> userService.validateDecryptedToken(token));
	}

	@Test
	public void tokenIsInvalidIfInstanceIdDoesNotMatchTest() throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		Long timestamp = System.currentTimeMillis();
		String token = format("%s|%s|%s|%s", STUDIO_MANAGED_USERNAME, INVALID_INSTANCE_ID, timestamp, SALT);
		assertFalse(userService.validateDecryptedToken(token));
	}

	@Test
	public void tokenIsInvalidIfTimestampIsInThePastTest() throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		Long timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1);
		String token = format("%s|%s|%s|%s", STUDIO_MANAGED_USERNAME, INSTANCE_ID, timestamp, SALT);
		assertFalse(userService.validateDecryptedToken(token));
	}

	@Test
	public void validTokenTest() throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		Long timestamp = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
		String token = format("%s|%s|%s|%s", STUDIO_MANAGED_USERNAME, INSTANCE_ID, timestamp, SALT);
		assertTrue(userService.validateDecryptedToken(token));
	}
}
