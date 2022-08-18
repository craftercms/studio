/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.dal;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.function.Supplier;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:crafter/studio/retry-db-operation-context.xml")
public class RetryingDatabaseOperationFacadeImplTest {

    @Autowired
    private RetryingDatabaseOperationFacadeImpl facade;

    @Test
    public void retryVoidOperationTest() {
        Runnable operation = spy(new DBOperation(2));
        facade.retry(operation);
        verify(operation, times(3)).run();
    }

    @Test
    public void retrySucceedingVoidOperationTest() {
        Runnable operation = spy(new DBOperation(0));
        facade.retry(operation);
        verify(operation, times(1)).run();
    }

    @Test
    public void retryReturningOperationTest() {
        Supplier<Integer> operation = spy(new ReturningDBOperation<>(5, 2));
        int value = facade.retry(operation);
        verify(operation, times(3)).get();
        Assertions.assertEquals(5, value, "Returning values do not match");
    }

    @Test
    public void retrySucceedingReturningOperationTest() {
        Supplier<Integer> operation = spy(new ReturningDBOperation<>(5, 0));
        int value = facade.retry(operation);
        verify(operation, times(1)).get();
        Assertions.assertEquals(5, value, "Returning values do not match");
    }

    private static class ReturningDBOperation<T> extends FailingDBOperation implements Supplier<T> {

        private final T value;

        public ReturningDBOperation(final T value, final int failingAttempts) {
            super(failingAttempts);
            this.value = value;
        }

        @Override
        public T get() {
            tryOperation();
            return value;
        }
    }

    private static class DBOperation extends FailingDBOperation implements Runnable {
        public DBOperation(final int failingAttempts) {
            super(failingAttempts);
        }

        public void run() {
            tryOperation();
        }
    }

    private static abstract class FailingDBOperation {
        private static final Logger logger = LoggerFactory.getLogger(FailingDBOperation.class);

        private int counter = 0;
        private final int failingAttempts;

        public FailingDBOperation(final int failingAttempts) {
            this.failingAttempts = failingAttempts;
        }

        protected void tryOperation() {
            logger.debug("Trying test database operation. Try #{}", counter);
            if (counter++ < failingAttempts) {
                throw new DeadlockLoserDataAccessException("Test exception", null);
            }
        }
    }
}
