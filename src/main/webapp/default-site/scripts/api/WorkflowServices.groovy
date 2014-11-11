package scripts.api

/** 
 * workflow services
 */
Class WorkflowServices {

	/** 
	 * get the deployment history for a site
	 * @param site - the project ID
	 * @param filter - filters to apply to history
	 */
	def getDeploymentHistory(site, filter) {

	}

	/** 
	 * get the scheduled items for a site
	 * @param site - the project ID
	 * @param filter - filters to apply to listing
	 */	
	def getScheduledItems(site, filter) {

	}

	/** 
	 * get the items that are in flight, waiting for approval for a site
	 * @param site - the project ID
	 * @param filter - filters to apply to listing
	 */	
	def getItemsWaitingReview(site, filter) {

	}

	/** 
	 * submit content in to workflow
	 * @param site - the project ID
	 * @param items - items to submit
	 * @param workflowID - id of workflow
	 * @param deploymentOptions - deployment options 	
	 */ 
	def submitContent(site, contentItems, workflowId, deploymentOptions) {
	}

	/**
	 * reject content in workflow (workflow controls step, this method simply sends signal)
	 * @param site - the project ID
	 * @param items - items to submit
	 * @param workflowID - id of workflow
	 */
	def rejectContent(site, contentItems, workflowID) {
	}

	/**
	 * approve content in workflow (workflow controls step, this method simply sends signal)
	 * @param site - the project ID
	 * @param items - items to submit
	 * @param workflowID - id of workflow
	 */
	def approveContent(site, contentItems, workflowID) {
	}

	/**
	 * set the state of an object
	 * @param site - the project ID
	 * @param path - path of item
	 * @param state - state to set	 
     */
	def setObjectState(site, path, state) {

	}

	/**
	 * get workflow jobs
	 * @param site - the project ID
     */
	def getWorkflowJobs(site) {

	}

	/**
	 * crate a workflow job
	 * @param site - the project ID
	 * @param items - collection of items
	 * @param workflowID - id of workflow
     */
	def createWorkflowJob(site, items, workflowId) {

	}

	/**
	 * get a user's activity history
	 * @param site - the project ID
	 * @param userId - id of the user
     */
	def getUserActivities(site, userId) {

	}		
}