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
package org.craftercms.cstudio.alfresco.executor;

import java.io.Serializable;
import java.util.Map;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.service.transaction.TransactionService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;

/**
 * an abstract base class that support execution within a transaction
 * 
 * @author hyanghee
 *
 */
public abstract class AbstractTransactionalExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransactionalExecutor.class);
	
	/** alfresco transactionService **/
	protected PersistenceManagerService persistenceManagerService;
	
	/**
	 * constructor
	 * 
	 * @param persistenceManagerService
	 */
	public AbstractTransactionalExecutor(PersistenceManagerService persistenceManagerService) {
		this.persistenceManagerService = persistenceManagerService;
	}
	

	/**
	 * execute an action with the given parameters
	 * 
	 * @param params
	 * @return result
	 * @throws Exception
	 */
	public ResultTO execute(Map<String, Serializable> params) throws Exception {
		UserTransaction trx = null;
		try {
			trx = persistenceManagerService.getNonPropagatingUserTransaction();
			trx.begin();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(">>>>>>>>>>>> [" + this.toString() + " Status: TRANSACTION STARTED, PARAMS: " + params + "]");
			}
			ResultTO result = executeInternal(params);
			if (trx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(">>>>>>>>>>>> [" + this.toString() + " Status: TRANSACTION ROLLBACK]");
				}
				trx.rollback();
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(">>>>>>>>>>>> [" + this.toString() + " Status: TRANSACTION COMITTED]");
				}
				try {
					trx.commit();
				} catch (RollbackException e) {
					throw new Exception(e);
				}
			}
			return result;
		} catch (Throwable t) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(">>>>>>>>>>>> [" + this.toString() + " Status: FAILED EXECUTION, PARAMS: " + params + "]", t);
			}
			if (trx != null) {
				try {
					// Roll back the exception
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(">>>>>>>>>>>> [" + this.toString() + " Status: TRANSACTION ROLLBACK]");
					}
					if (trx.getStatus() != Status.STATUS_ROLLEDBACK) {
						trx.rollback();
					}
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(">>>>>>>>>>>> [" + this.toString() + " Status: ROLLBACKED TRANSACTION]");
					}
				} catch (Throwable rollbackException) {
					// just dump the exception - we are already in a failure state
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(">>>>>>>>>>>> [" + this.toString() + " Status: FAILED TO ROLLBACK]", rollbackException);
					}
				}
			}
			throw new Exception(t);
		}
	}
	
	/**
	 * execute an action with the given params
	 * 
	 * @param params
	 * @return result
	 * @throws ServiceException 
	 */
	protected abstract ResultTO executeInternal(Map<String, Serializable> params);
}
