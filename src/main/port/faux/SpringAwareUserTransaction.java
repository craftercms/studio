package org.alfresco.util.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import javax.transaction.*;
import java.lang.reflect.Method;

public class SpringAwareUserTransaction extends TransactionAspectSupport implements UserTransaction, TransactionAttributeSource, TransactionAttribute {
    @Override
    public boolean rollbackOn(Throwable throwable) {
        return false;
    }

    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class aClass) {
        return null;
    }

    @Override
    public int getPropagationBehavior() {
        return 0;
    }

    @Override
    public int getIsolationLevel() {
        return 0;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {

    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {

    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {

    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {

    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {

    }

    public SpringAwareUserTransaction(PlatformTransactionManager transactionManager, boolean readOnly, int isolationLevel, int propagationBehaviour, int timeout) { }
}
