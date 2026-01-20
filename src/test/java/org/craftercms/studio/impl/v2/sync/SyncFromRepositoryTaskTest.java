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

package org.craftercms.studio.impl.v2.sync;

import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SyncFromRepositoryTaskTest {

	@InjectMocks
	protected SyncFromRepositoryTask task;

	@Test
	public void getCreatedPathsTest() {
		RepoOperation op1 = new RepoOperation(RepoOperation.Action.CREATE, "/path1",
				null, null, "123");
		RepoOperation op2 = new RepoOperation(RepoOperation.Action.MOVE, "/old-one",
				null, "/new-one", "123");

		List<String> createdPaths = task.getCreatedPaths(List.of(op1, op2));
		assertEquals(createdPaths.size(), 2);
		assertEquals(createdPaths.getFirst(), "/path1");
		assertEquals(createdPaths.getLast(), "/new-one");
	}
}
