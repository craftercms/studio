/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal.repository;

import java.util.Set;

public class RepositoryStatus {

	private Set<String> conflicting;
	private Set<String> uncommittedChanges;
	private Set<String> untracked;
	private boolean clean;

	public Set<String> getConflicting() {
		return conflicting;
	}

	public void setConflicting(Set<String> conflicting) {
		this.conflicting = conflicting;
	}

	public Set<String> getUncommittedChanges() {
		return uncommittedChanges;
	}

	public void setUncommittedChanges(Set<String> uncommittedChanges) {
		this.uncommittedChanges = uncommittedChanges;
	}

	public Set<String> getUntracked() {
		return untracked;
	}

	public void setUntracked(Set<String> untracked) {
		this.untracked = untracked;
	}

	public boolean isClean() {
		return clean;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}
}
