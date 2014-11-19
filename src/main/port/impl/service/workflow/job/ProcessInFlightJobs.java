/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.impl.service.workflow.job;

import org.craftercms.cstudio.api.job.Job;
import org.craftercms.cstudio.api.log.Logger;
import org.craftercms.cstudio.api.log.LoggerFactory;
import org.craftercms.cstudio.api.service.authentication.AuthenticationService;
import org.craftercms.cstudio.api.service.transaction.TransactionService;
import org.craftercms.cstudio.api.service.workflow.WorkflowJob;
import org.craftercms.cstudio.api.service.workflow.WorkflowService;
import org.craftercms.cstudio.impl.service.workflow.WorkflowManager;

import javax.transaction.UserTransaction;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Job looks at active jobs and attempts to run handler for the current state of each workflow 
 * @author russdanner
 */
public class ProcessInFlightJobs implements Job {

	private static final Logger logger = LoggerFactory.getLogger(ProcessInFlightJobs.class);

	protected final String MSG_UNABLE_TO_EXECUTE_JOB = "unable_to_execute_workflow_job_as";
    protected final String MSG_PROCESSING_ACTIVE_WORKFLOW_JOBS = "processing_active_workflow_jobs";
    protected final String MSG_PROCESSING_WORKFLOW_JOB = "processing_workflow_job";
    protected final String MSG_ERROR_PROCESSING_WORKFLOW_JOB = "err_processing_workflow_job";
    protected final String MSG_ERROR_NO_TRANSACTION_PROCESSING_WORKFLOW_JOB = "err_no_transaction_while_processing_workflow_job";

    public void execute() {
		try {
			Method processJobMethod = this.getClass().getMethod("processJobs", new Class[0]);
            String adminUser = _authenticationService.getAdministratorUser();
			_authenticationService.runAs(adminUser, this, processJobMethod);
		}
		catch(Exception err) {
			logger.error(MSG_UNABLE_TO_EXECUTE_JOB, err, "admin");
		}
	}
	
	public void processJobs() {

		logger.info(MSG_PROCESSING_ACTIVE_WORKFLOW_JOBS);
		List<WorkflowJob> allJobs = _workflowService.getJobsInState(null);

		for (WorkflowJob job : allJobs) {
			try {
				UserTransaction tx = _transactionService.getTransaction();
				try {
					tx.begin();
					logger.info(MSG_PROCESSING_WORKFLOW_JOB, job.getId());
					_workflowManager.handleJobState(job);
					tx.commit();
				}
				catch(Exception err) {
					logger.error(MSG_ERROR_PROCESSING_WORKFLOW_JOB, err, job.getId());
					tx.rollback();
				}
			}
			catch(Exception err) {
				logger.error(MSG_ERROR_NO_TRANSACTION_PROCESSING_WORKFLOW_JOB, err, job.getId());
			}
		}
	}

	/** getter WorkflowService */
	public WorkflowService getWorkflowService() { return _workflowService; }
	/** setter for Workflow service */
	public void setWorkflowService(WorkflowService service) { _workflowService = service; }

	/** getter Workflow workflow manager */
	public WorkflowManager getWorkflowManager() { return _workflowManager; }
	/** setter for Workflow workflow manager */
	public void setWorkflowManager(WorkflowManager mgr) { _workflowManager = mgr; }

	/** getter auth service */
	public AuthenticationService getAuthenticationService() { return _authenticationService; }
	/** setter for auth service */
	public void setAuthenticationService(AuthenticationService service) { _authenticationService = service; }

	/** getter transaction service */
	public TransactionService getTransactionService() { return _transactionService; }
	/** setter for transaction service */
	public void setTransactionService(TransactionService service) { _transactionService = service; }

	protected TransactionService _transactionService;
	protected WorkflowManager _workflowManager;
	protected WorkflowService _workflowService;
	protected AuthenticationService _authenticationService;
}
