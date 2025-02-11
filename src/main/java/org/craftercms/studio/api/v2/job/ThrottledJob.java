/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.job;

import org.craftercms.studio.api.v1.job.Job;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for a job that is throttled to execute only after a certain number of ticks have been consumed
 */
public abstract class ThrottledJob implements Job {

	private final AtomicInteger counter = new AtomicInteger(0);

	@Override
	public void execute() {
		int current = counter.updateAndGet(value -> {
			if ((value + 1) >= getPeriod()) {
				return 0;
			}
			return value + 1;
		});
		if (current == 0) {
			doExecute();
		}
	}

	/**
	 * Period is the number of tickets that must be consumed before the job is executed
	 *
	 * @return the period
	 */
	protected int getPeriod() {
		return 1;
	}

	/**
	 * Perform the actual job
	 */
	protected abstract void doExecute();
}
