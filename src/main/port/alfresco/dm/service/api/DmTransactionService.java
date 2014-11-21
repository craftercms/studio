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
package org.craftercms.cstudio.alfresco.dm.service.api;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.craftercms.cstudio.alfresco.util.TransactionHelper;

import javax.transaction.UserTransaction;

/**
 * @author Dejan Brkic
 */
public interface DmTransactionService {

    /**
     * Get the standard instance of the helper object that supports transaction retrying.
     *
     * @return
     *      Returns a helper object that executes units of work transactionally.  The helper
     *      can be reused or altered as required.
     */
    public TransactionHelper getTransactionHelper();

    public RetryingTransactionHelper getRetryingTransactionHelper();

    /**
     * Gets a user transaction that ensures a new transaction is created.
     * Any enclosing transaction is not propagated.
     * This is like the EJB <b>REQUIRES_NEW</b> transaction attribute -
     * when the transaction is started, the current transaction will be
     * suspended and a new one started.
     *
     * @return Returns a non-propagating user transaction
     */
    public UserTransaction getNonPropagatingUserTransaction();
}
