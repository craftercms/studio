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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.util.transaction.SpringAwareUserTransaction;
import org.craftercms.cstudio.alfresco.dm.service.api.DmTransactionService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.util.TransactionHelper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

public class DmTransactionServiceImpl extends AbstractRegistrableService implements DmTransactionService {

    protected PlatformTransactionManager _transactionManager;

    public PlatformTransactionManager getTransactionManager() {
        return this._transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this._transactionManager = transactionManager;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmTransactionService.class, this);
    }

    @Override
    public TransactionHelper getTransactionHelper() {
        TransactionHelper helper = new TransactionHelper();
        helper.setDmTransactionService(this);
        return helper;
    }

    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper() {
        RetryingTransactionHelper transactionHelper = getService(PersistenceManagerService.class).getRetryingTransactionHelper();
        transactionHelper.setMaxRetries(10);
        return transactionHelper;
    }

    @Override
    public UserTransaction getNonPropagatingUserTransaction() {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(_transactionManager, isReadOnly(),
                TransactionDefinition.ISOLATION_DEFAULT, TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }

    /**
     * check if the system is in read only mode
     *
     * @return true if read only
     */
    public boolean isReadOnly() {
        
        return getService(PersistenceManagerService.class).isReadOnly();
    }
}
