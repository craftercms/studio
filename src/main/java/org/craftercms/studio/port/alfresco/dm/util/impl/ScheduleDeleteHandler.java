/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.cstudio.alfresco.dm.util.impl;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javolution.util.FastList;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmTransactionService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.to.DmDependencyTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.util.ScheduleItem;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.util.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA. User: dejan Date: 3/21/12 Time: 11:44 AM To change
 * this template use File | Settings | File Templates.
 */
public class ScheduleDeleteHandler implements Runnable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ScheduleDeleteHandler.class);


	protected LinkedBlockingQueue<ScheduleItem> queue = new LinkedBlockingQueue<ScheduleItem>();

	protected boolean isStop = false;

	protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public ScheduleDeleteHandler() {
	}

	public void run() {
		while (!isStop) {
			try {
                DmTransactionService dmTransactionService = getServicesManager().getService(DmTransactionService.class);
				final ScheduleItem item = queue.take();
				final List<String> paths = item.getPaths();
				System.out.println("schedule items : " + paths);
				long now = System.currentTimeMillis();
				long pickUpTime = item.getPickUpTime();
				long diff = pickUpTime - now;
				// sleep for some time to finish workflow
				if (diff > 0) {
					Thread.sleep(diff);
				}
				final List<DmDependencyTO> dmDependencyTOs = new FastList<DmDependencyTO>();
				TransactionHelper transactionHelper = dmTransactionService
						.getTransactionHelper();
				RetryingTransactionHelper.RetryingTransactionCallback emailSetTrans = new RetryingTransactionHelper.RetryingTransactionCallback() {
					public Object execute() throws Throwable {
						for (String path : paths) {
							DmDependencyTO dmDependencyTO = new DmDependencyTO();
							if (!DmUtils.isContentXML(path)) { // $Review use
																// constants
								path = path + "/" + DmConstants.INDEX_FILE;
							}

							dmDependencyTO.setUri(path);
							dmDependencyTO.setNow(true);
							dmDependencyTOs.add(dmDependencyTO);
							setIsEmailRequired(item.getSite(), dmDependencyTO);
						}
						return null;
					}
				};
				transactionHelper.doInTransaction(emailSetTrans);
				String approver = getApprover(item.getSite(), dmDependencyTOs);

				deleteInTransaction(item.getSite(), dmDependencyTOs, approver);
			} catch (Exception e) {
				LOGGER.error("error during deleting scheduled items " + e);
			}
		}
	}


	protected String getApprover(final String site,
			final List<DmDependencyTO> dmDependencyTOs) {
		try {
            DmTransactionService dmTransactionService = getServicesManager().getService(DmTransactionService.class);
            final PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
			TransactionHelper transactionHelper = dmTransactionService
					.getTransactionHelper();
			RetryingTransactionHelper.RetryingTransactionCallback<String> emailSetTrans = new RetryingTransactionHelper.RetryingTransactionCallback<String>() {

				public String execute() throws Throwable {
					for (DmDependencyTO dmDependencyTO : dmDependencyTOs) {
						String uri = dmDependencyTO.getUri();
						// String sandbox = _servicesConfig.getSandbox(site);
						// AuthenticationUtil.setFullyAuthenticatedUser(sandbox);
						String fullPath = getContentFullPath(site, uri);
						// PropertyValue propApprover =
						// _avmService.getNodeProperty(-1, fullPath,
						// CStudioContentModel.PROP_WEB_APPROVED_BY);
						NodeRef node = persistenceManagerService.getNodeRef(fullPath);
						Serializable propApprover = persistenceManagerService.getProperty(
								node, CStudioContentModel.PROP_WEB_APPROVED_BY);
						if (propApprover != null)
							return (String) propApprover;

					}
					return null;
				}
			};
			return transactionHelper.doInTransaction(emailSetTrans);
		} catch (ServiceException e) {
		}
		return null;
	}

	protected void setIsEmailRequired(final String site,
			final DmDependencyTO dmDependencyTO) {
		String uri = dmDependencyTO.getUri();
		// String sandbox = _servicesConfig.getSandbox(site);
		// AuthenticationUtil.setFullyAuthenticatedUser(sandbox);
		String fullPath = getContentFullPath(site, uri);
		// PropertyValue sendEmailValue = _avmService.getNodeProperty(-1,
		// fullPath, CStudioContentModel.PROP_WEB_WF_SEND_EMAIL);
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		NodeRef node = persistenceManagerService.getNodeRef(fullPath);
		Serializable sendEmailValue = persistenceManagerService.getProperty(node,
				CStudioContentModel.PROP_WEB_WF_SEND_EMAIL);
		boolean sendEmail = (sendEmailValue != null)
				&& (Boolean) sendEmailValue;
		dmDependencyTO.setSendEmail(sendEmail);
	}

	protected String getContentFullPath(String site, String uri) {
        DmContentService dmContentService = getServicesManager().getService(DmContentService.class);
		return dmContentService.getContentFullPath(site, uri);
	}

	public void addToQueue(ScheduleItem item) {
		queue.offer(item);
	}

	public void stop() {
		isStop = true;
	}

	protected void deleteInTransaction(final String site,
			final List<DmDependencyTO> scheduleDeleteItems,
			final String approver) throws ServiceException {
        DmTransactionService dmTransactionService = getServicesManager().getService(DmTransactionService.class);
        final DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
		RetryingTransactionHelper txnHelper = dmTransactionService
				.getRetryingTransactionHelper();
		RetryingTransactionHelper.RetryingTransactionCallback cancelWorkflowCallBack = new RetryingTransactionHelper.RetryingTransactionCallback() {
			public Object execute() throws Throwable {
				dmWorkflowService.doDelete(site, "", scheduleDeleteItems,
						approver);
				return null;
			}
		};
		txnHelper.doInTransaction(cancelWorkflowCallBack, false, true);
	}

}
