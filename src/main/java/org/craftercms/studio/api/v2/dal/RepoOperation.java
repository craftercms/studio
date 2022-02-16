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

package org.craftercms.studio.api.v2.dal;

import java.time.ZonedDateTime;

public class RepoOperation {
	protected Action action;
	protected String path;
	protected ZonedDateTime dateTime;
	protected String moveToPath;
	protected String author;
	protected String publisher;
	protected String comment;
	protected String commitId;

	public RepoOperation(final Action action, final String path, final ZonedDateTime dateTime, final String moveToPath,
						 final String commitId) {
		this.action = action;
		this.path = path;
		this.dateTime = dateTime;
		this.moveToPath = moveToPath;
		this.commitId = commitId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public ZonedDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(final ZonedDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getMoveToPath() {
		return moveToPath;
	}

	public void setMoveToPath(final String moveToPath) {
		this.moveToPath = moveToPath;
	}

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

	public enum Action {
		CREATE,
		DELETE,
		MOVE,
		UPDATE,
		COPY
	}
}
