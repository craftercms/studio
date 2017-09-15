package org.craftercms.studio.api.v1.to;

import java.time.ZonedDateTime;

import org.craftercms.studio.api.v1.constant.RepoOperation;

/**
 * Created by Sumer Jabri on 1/11/17.
 */
public class RepoOperationTO {
	protected RepoOperation operation;
	protected String path;
	protected ZonedDateTime dateTime;
	protected String moveToPath;
	protected String author;
	protected String publisher;
	protected String comment;
	protected String commitId;

	public RepoOperationTO(final RepoOperation operation, final String path, final ZonedDateTime dateTime, final String
		moveToPath, String commitId) {
		this.operation = operation;
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

	public RepoOperation getOperation() {
		return operation;
	}

	public void setOperation(final RepoOperation operation) {
		this.operation = operation;
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
}
