/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.utils.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Utility class for database-related operations.
 */
public class DBUtils {

    private final static Logger logger = LoggerFactory.getLogger(DBUtils.class);

    /**
     * Execute a runnable in a transaction.
     * This method will use the provided transactionManager to run the given runnable in a transaction. It will
     * be automatically committed (or rolled back if an exception is thrown).
     * After transaction is complete, this method will rethrow any exception thrown by the runnable.
     *
     * @param transactionManager The transaction manager
     * @param transactionName    The name of the transaction
     * @param runnable           The runnable to execute
     * @throws RuntimeException wrapping any exception thrown by the runnable
     */
    public static void runInTransaction(final PlatformTransactionManager transactionManager,
                                        final String transactionName,
                                        final ThrowingRunnable runnable) throws Exception {
        Wrapper<Exception> exception = new Wrapper<>();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName(transactionName);
        transactionTemplate.executeWithoutResult(status -> {
            logger.trace("Starting transaction '{}'", status.getTransactionName());
            try {
                runnable.run();
            } catch (Exception e) {
                logger.trace("Error occurred during transaction '{}', rolling back", status.getTransactionName(), e);
                exception.set(e);
            }
        });
        logger.trace("Completed transaction '{}'", transactionName);
        if (exception.get() != null) {
            logger.error("Error occurred during transaction '{}', rolling back", exception);
            throw exception.get();
        }
    }

    /**
     * Runnable interface that can throw an exception.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static class Wrapper<T> {
        private T value;

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}
