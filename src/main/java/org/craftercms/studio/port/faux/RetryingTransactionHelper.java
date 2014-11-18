package org.alfresco.repo.transaction;

public class RetryingTransactionHelper {

	public interface RetryingTransactionCallback<Result> {
		Result execute() throws Throwable;
	}

	public void setMaxRetries(int maxRetries) { }

}