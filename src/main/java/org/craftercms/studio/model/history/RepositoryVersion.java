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

package org.craftercms.studio.model.history;

import org.craftercms.studio.model.rest.Person;
import org.eclipse.jgit.revwalk.RevCommit;

import java.time.Instant;

/**
 * Represents a commit of a git repository.
 */
public class RepositoryVersion {
	// Commit id
	private final String versionNumber;
	// Committer comes from the log command
	private final String comment;
	private final Instant modifiedDate;

	private final String committer;
	// Author corresponds to the Studio user. This field is
	// null when the commit was not made via Studio API.
	private Person author;

	public RepositoryVersion(final RevCommit revCommit) {
		this.versionNumber = revCommit.getName();
		this.committer = revCommit.getAuthorIdent().getName();
		this.modifiedDate = Instant.ofEpochSecond(revCommit.getCommitTime());
		this.comment = revCommit.getFullMessage();
	}

	public void setAuthor(Person author) {
		this.author = author;
	}

	public Person getAuthor() {
		return author;
	}

	public String getComment() {
		return comment;
	}

	public String getCommitter() {
		return committer;
	}

	public Instant getModifiedDate() {
		return modifiedDate;
	}

	public String getVersionNumber() {
		return versionNumber;
	}
}
