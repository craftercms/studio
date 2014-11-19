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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.wcm.sandbox.SandboxService;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmSearchService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmTransactionService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.util.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateCleaner {
    
    protected static final Logger logger = LoggerFactory.getLogger(StateCleaner.class);


	protected DmSearchService _dmSearchService;

	protected ServicesConfig _servicesConfig;

	protected DmTransactionService _dmTransactionService;

	protected DmWorkflowService _dmWorkflowService;

	protected SandboxService _sandboxService;

	protected SearchService _searchService;

	public SearchService getSearchService() {
		return _searchService;
	}

	public void setSearchService(SearchService searchService) {
		this._searchService = searchService;
	}

	protected PersistenceManagerService _persistenceManagerService;

	public void setPersistenceManagerService(
			PersistenceManagerService persistenceManagerService) {
		this._persistenceManagerService = persistenceManagerService;
	}

    protected ServicesManager _servicesManager;
    public ServicesManager getServicesManager() {
        return _servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this._servicesManager = servicesManager;
    }

	protected String query = "@cstudio-core-web\\:status:";

	// protected String query="@cstudio-core-web\\:status:\"Submitted\"";

	public List<String> getIncorrectItems(String site, String status)
			throws ServiceException {

		// String sandbox = _servicesConfig.getSandbox(site);
		String storeName = DmUtils.createStoreName(site);
		System.out.println("storeName = " + storeName);
		return search(status, storeName);
	}

	public List<String> search(final String status, final String storeName)
			throws ServiceException {
		TransactionHelper transactionHelper = _dmTransactionService
				.getTransactionHelper();
		return transactionHelper
				.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<List<String>>() {
					@Override
					public List<String> execute() throws Throwable {
						return _search(status, storeName);
					}
				});

	}

	protected List<String> _search(String status, String storeName) {
// PORT
		return null;

		// AuthenticationUtil.setFullyAuthenticatedUser("cstudioadmin");
		// StoreRef ref = new StoreRef(StoreRef.PROTOCOL_AVM, storeName);
		// StringBuilder builder = new StringBuilder(query).append("\"")
		// 		.append(status).append("\"");
		// System.out.println("query = " + builder);
		// SearchParameters parameters = new SearchParameters();
		// parameters.addStore(ref);
		// parameters.setQuery(builder.toString());
		// parameters.setLanguage(SearchService.LANGUAGE_LUCENE);
		// ResultSet resultSet = _dmSearchService.query(parameters);
		// List<NodeRef> refList = resultSet.getNodeRefs();
		// List<String> list = new ArrayList<String>();
  //       PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
		// for (NodeRef nodeRef : refList) {
  //           ObjectStateService.State state = persistenceManagerService.getObjectState(nodeRef);
  //           if (!ObjectStateService.State.isLive(state)) {
  //               String nodePath = getNodePath(nodeRef);
  //               list.add(nodePath);
  //           }
		// }
		// return list;
	}

	protected String getNodePath(NodeRef node) {
		StringBuilder nodePath = new StringBuilder();
		try {
			List<FileInfo> paths = _persistenceManagerService.getNamePath(
					_searchService.findNode(node.getStoreRef(),
							DmConstants.DM_WEM_PROJECT_ROOT), node);
			for (FileInfo path : paths) {
				nodePath.append("/").append(path.getName());
			}
		} catch (FileNotFoundException e) {
			logger.error(
					"Error while getting node path for node: " + node.getId(),
					e);
		}
		return nodePath.toString();
	}

	public List<String> clean(final String status, final String site,
			final String storeName) throws ServiceException {
		AuthenticationUtil.setFullyAuthenticatedUser("cstudioadmin");
		RetryingTransactionHelper helper = _dmTransactionService
				.getRetryingTransactionHelper();
		RetryingTransactionHelper.RetryingTransactionCallback<List<String>> callback = new RetryingTransactionHelper.RetryingTransactionCallback<List<String>>() {
			@Override
			public List<String> execute() throws Throwable {
				List<String> stringList = _clean(status, site, storeName);
				return stringList;
			}
		};
		return helper.doInTransaction(callback);

	}

	protected List<String> _clean(final String status, final String site,
			final String storeName) throws ServiceException {
		RetryingTransactionHelper transactionHelper = _dmTransactionService
				.getRetryingTransactionHelper();
		RetryingTransactionHelper.RetryingTransactionCallback<List<String>> callback = new RetryingTransactionHelper.RetryingTransactionCallback<List<String>>() {
			@Override
			public List<String> execute() throws Throwable {
				List<String> stringList = _search(status, site);
				List<String> relativePaths = new ArrayList<String>();
				for (String s : stringList) {
					DmPathTO path = new DmPathTO(s);
					path.setStoreName(storeName);
					NodeRef nodeDescriptor = DmUtils
							.getNodeRef(_persistenceManagerService, s);
					_persistenceManagerService.setProperty(nodeDescriptor,
							CStudioContentModel.PROP_STATUS,
							DmConstants.DM_STATUS_LIVE);
					_persistenceManagerService.removeProperty(nodeDescriptor,
							WCMWorkflowModel.PROP_LAUNCH_DATE);
					relativePaths.add(path.getAssetPath());
				}
				for (String relativePath : relativePaths) {
					System.out.println("relativePaths = " + relativePath);
				}

				return relativePaths;

			}

		};
		final List<String> relativePaths = transactionHelper.doInTransaction(
				callback, false, true);
		if (!relativePaths.isEmpty()) {
			_sandboxService.submitListAssets(storeName, relativePaths, null,
					null, "cleanup data submission", "cleanup data submission",
					null, null, false);
		}
		System.out.println("DONE");
		return relativePaths;
	}

	public void setDmSearchService(DmSearchService dmSearchService) {
		this._dmSearchService = dmSearchService;
	}

	public void setServicesConfig(ServicesConfig _servicesConfig) {
		this._servicesConfig = _servicesConfig;
	}

	public void setDmTransactionService(
			DmTransactionService dmTransactionService) {
		this._dmTransactionService = dmTransactionService;
	}

	public DmTransactionService getDmTransactionService() {
		return _dmTransactionService;
	}

	public void setDmWorkflowService(DmWorkflowService dmWorkflowService) {
		this._dmWorkflowService = dmWorkflowService;
	}

	public void setSandboxService(SandboxService sandboxService) {
		this._sandboxService = sandboxService;
	}

}
