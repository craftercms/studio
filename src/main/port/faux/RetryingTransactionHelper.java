package org.alfresco.repo.transaction;

public class RetryingTransactionHelper {

	public interface RetryingTransactionCallback<Result> {
		Result execute() throws Throwable;
	}

	public void setMaxRetries(int maxRetries) { }
	public void doInTransaction(RetryingTransactionCallback<Object> cb, boolean a, boolean b) { }

}