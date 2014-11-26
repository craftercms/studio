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
package org.craftercms.cstudio.alfresco.util;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.craftercms.cstudio.alfresco.dm.service.api.DmTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

public class TransactionHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionHelper.class);
	
    /**
     * Reference to the TransactionService instance.
     */
    protected DmTransactionService _dmTransactionService;

    public <R> R doInTransaction(RetryingTransactionCallback<R> cb,boolean rollbackOnFailure) throws ServiceException{
        UserTransaction trx = null;
		try {
			trx = _dmTransactionService.getNonPropagatingUserTransaction();
			trx.begin();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION STARTED");
			}
			R result = cb.execute();
			if (trx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION ROLLBACK]");
				}
				trx.rollback();
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION COMMITTED]");
				}
				try {
					trx.commit();
				} catch (RollbackException e) {
					throw new ServiceException(e);
				}
			}
			return result;
		} catch (Throwable t) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(">>>>>>>>>>>> [" + cb.toString() + " Status: FAILED EXECUTION]", t);

			}
			if (trx != null) {

				try {
					// Roll back the exception
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION ROLLBACK]");
                    }
                    if (rollbackOnFailure || trx.getStatus() != Status.STATUS_ROLLEDBACK) {
                        trx.rollback();
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(">>>>>>>>>>>> [" + cb.toString() + " Status: ROLLBACKED TRANSACTION]");
                    }
				} catch (Throwable rollbackException) {
					// just dump the exception - we are already in a failure
					// state
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(">>>>>>>>>>>> [" + cb.toString() + " Status: FAILED TO ROLLBACK]", rollbackException);
					}
				}
			}
			throw new ServiceException(t);
		}
    }

    public <R> R doInTransaction(RetryingTransactionCallback<R> cb) throws ServiceException {
        return doInTransaction(cb, false);
    }


    /**
     * @param dmTransactionService the wcmTransactionService to set
     */
    public void setDmTransactionService(DmTransactionService dmTransactionService) {
        this._dmTransactionService = dmTransactionService;
    }

    public <R> R doInTransactionDm(RetryingTransactionCallback<R> cb,boolean rollbackOnFailure) throws ServiceException{
        UserTransaction trx = null;
        try {
            trx = _dmTransactionService.getNonPropagatingUserTransaction();
            trx.begin();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION STARTED");
            }
            R result = cb.execute();
            if (trx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION ROLLBACK]");
                }
                trx.rollback();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION COMMITTED]");
                }
                try {
                    trx.commit();
                } catch (RollbackException e) {
                    throw new ServiceException(e);
                }
            }
            return result;
        } catch (Throwable t) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(">>>>>>>>>>>> [" + cb.toString() + " Status: FAILED EXECUTION]", t);

            }
            if (trx != null) {

                try {
                    // Roll back the exception
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(">>>>>>>>>>>> [" + cb.toString() + " Status: TRANSACTION ROLLBACK]");
                    }
                    if (rollbackOnFailure || trx.getStatus() != Status.STATUS_ROLLEDBACK) {
                        trx.rollback();
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(">>>>>>>>>>>> [" + cb.toString() + " Status: ROLLBACKED TRANSACTION]");
                    }
                } catch (Throwable rollbackException) {
                    // just dump the exception - we are already in a failure
                    // state
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(">>>>>>>>>>>> [" + cb.toString() + " Status: FAILED TO ROLLBACK]", rollbackException);
                    }
                }
            }
            throw new ServiceException(t);
        }
    }

    public <R> R doInTransactionDm(RetryingTransactionCallback<R> cb) throws ServiceException {
        return doInTransactionDm(cb, false);
    }

}
