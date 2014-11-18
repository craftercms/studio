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
package org.craftercms.cstudio.alfresco.dm.executor;

import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.wcm.sandbox.SandboxService;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.executor.AbstractTransactionalExecutor;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ProfileService;
import org.craftercms.cstudio.alfresco.to.ResultTO;

import java.io.Serializable;
import java.util.*;

public class SubmitToWorkflowExecutor extends AbstractTransactionalExecutor {

	/** dm workflow property qnames **/
	protected static final QName WF_PROP_REVIEW_TYPE = QName.createQName(org.alfresco.service.namespace.NamespaceService.WCMWF_MODEL_1_0_URI, "reviewType");
	protected static final QName WF_PROP_REVIEWER_CNT = QName.createQName(org.alfresco.service.namespace.NamespaceService.WCMWF_MODEL_1_0_URI, "reviewerCnt");
	protected static final QName WF_PROP_APPROVE_CNT = QName.createQName(org.alfresco.service.namespace.NamespaceService.WCMWF_MODEL_1_0_URI, "approveCnt");
	protected static final QName WF_ASSIGNEES = QName.createQName(org.alfresco.service.namespace.NamespaceService.BPM_MODEL_1_0_URI, "assignees");
	
	/** wcm workflow proerty values **/
	protected static final String REVIEW_TYPE_SERIAL = "Serial";

	/**
	 * default submit direct workflow name to submit to
	 */
	protected String _submitDirectWorkflowName;
	
	/**
	 * default review worfklow name to submit to
	 */
	protected String _reviewWorkflowName;

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /*
      * constructor
      */
	public SubmitToWorkflowExecutor(PersistenceManagerService persistenceManagerService) {
		super(persistenceManagerService);
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.executor.AbstractTransactionalExecutor#executeInternal(java.util.Map)
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	protected ResultTO executeInternal(Map<String, Serializable> params) {
		String site = (String) params.get(DmConstants.KEY_SITE);
		String sub = (String) params.get(DmConstants.KEY_SUB);
		//String sandbox = (String) params.get(DmConstants.KEY_SANDBOX);
		String assignee = (String) params.get(DmConstants.KEY_ASSIGNEE);
		String label = (String) params.get(DmConstants.KEY_LABEL);
		String description = (String) params.get(DmConstants.KEY_DESCRIPTION);
		String workflowName = (String) params.get(DmConstants.KEY_WORKFLOW_NAME);
		Date launchDate = (Date) params.get(DmConstants.KEY_LAUNCH_DATE);
		List<String> paths = (List<String>) params.get(DmConstants.KEY_PATHS);
		Boolean autoDeploy = (Boolean) params.get(DmConstants.KEY_AUTO_DEPLOY);
		Integer priority = (Integer) params.get(DmConstants.KEY_PRIORITY);
		Boolean submitDirect = (Boolean) params.get(DmConstants.KEY_SUBMIT_DIRECT);
		// TODO: might be accepting these params in future
		Map<String, Date> expirationDates = null;
		boolean validateLinks = false;
		
		String storeId = DmUtils.createStoreName(site);
		workflowName = (StringUtils.isEmpty(workflowName)) ? getWorkflowName(site, sub, submitDirect) : workflowName;
		assignee = StringUtils.isEmpty(assignee) ? getAssignee(site, sub) : assignee;
		
		// create parameter maps
		Map<QName, Serializable> workflowParameters = getWorkflowParameters(
				((priority == null) ? 3 : priority.intValue()), assignee, true);
		workflowParameters.put(WF_PROP_REVIEW_TYPE, REVIEW_TYPE_SERIAL);
		workflowParameters.put(WF_PROP_REVIEWER_CNT, 1);
		workflowParameters.put(WF_PROP_APPROVE_CNT, 1);
		
		// submit to workflow
        SandboxService sandboxService = getServicesManager().getService(SandboxService.class);
		sandboxService.submitListAssets(storeId, paths, workflowName, workflowParameters,
				label, description, expirationDates, launchDate, 
				((autoDeploy== null) ? false : autoDeploy.booleanValue()));
		ResultTO result = new ResultTO();
		result.setSuccess(true);
		return result;
	}

	/**
	 * Gets the workflow parameters for the given asset
	 * 
	 * @param priority
	 * @param assignee
	 * @param autoDeploy
	 * @return map of workflow parameters
	 */
	protected Map<QName, Serializable> getWorkflowParameters(int priority, String assignee, boolean autoDeploy) {
		Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
		List<NodeRef> assignees = new ArrayList<NodeRef>(1);
		parameters.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, priority);
        ProfileService profileService = getServicesManager().getService(ProfileService.class);
		NodeRef personRef = profileService.getUserRef(assignee);
		parameters.put(WorkflowModel.ASSOC_ASSIGNEE, personRef);
		assignees.add(personRef);
		parameters.put(WF_ASSIGNEES, (Serializable) assignees);
		parameters.put(WCMWorkflowModel.PROP_AUTO_DEPLOY, autoDeploy);
		return parameters;
	}
	
	/**
	 * get workflow name from configuration based on the given site and the sub
	 * 
	 * @param site
	 * @param sub
	 * @return workflow name
	 */
	protected String getWorkflowName(String site, String sub, Boolean submitDirect) {
		if (submitDirect != null && submitDirect == true) {
			return _submitDirectWorkflowName;
		} else {
			return _reviewWorkflowName;
		}
	}

	/**
	 * get assignee from configuration based on the given site and the sub
	 * 
	 * @param site
	 * @param sub
	 * @return assignee
	 */
	protected String getAssignee(String site, String sub) {
		// TODO: find assignee from configuration
		return AuthenticationUtil.getAdminUserName();
	}

	
	/**
	 * @param submitDirectWorkflowName the submitDirectWorkflowName to set
	 */
	public void setSubmitDirectWorkflowName(String submitDirectWorkflowName) {
		this._submitDirectWorkflowName = submitDirectWorkflowName;
	}

	/**
	 * @param reviewWorkflowName the reviewWorkflowName to set
	 */
	public void setReviewWorkflowName(String reviewWorkflowName) {
		this._reviewWorkflowName = reviewWorkflowName;
	}

}
