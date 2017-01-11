package org.craftercms.studio.api.v1.to;

import java.util.Date;

import org.craftercms.studio.api.v1.constant.RepoOperation;

/**
 * Created by Sumer Jabri on 1/11/17.
 */
public class RepoOperationTO {
	protected RepoOperation operation;
	protected String path;
	protected Date dateTime;
	protected String moveToPath;
	protected String author;
	protected String publisher;
	protected String comment;
	protected String commitId;

	public RepoOperationTO(final RepoOperation operation, final String path, final Date dateTime, final String
		moveToPath) {
		this.operation = operation;
		this.path = path;
		this.dateTime = dateTime;
		this.moveToPath = moveToPath;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(final Date dateTime) {
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
}
