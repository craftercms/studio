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

package org.craftercms.studio.api.v2.dal.item;

import org.craftercms.studio.model.rest.Person;

import java.time.ZonedDateTime;

/**
 * Publishing environment (target) related metadata for an item.
 */
public class PublishTargetStatus {
	private ZonedDateTime dateLastPublished;
	private ZonedDateTime dateScheduled;
	private Person publisher;

	public ZonedDateTime getDateScheduled() {
		return dateScheduled;
	}

	public void setDateScheduled(ZonedDateTime dateScheduled) {
		this.dateScheduled = dateScheduled;
	}

	public ZonedDateTime getDateLastPublished() {
		return dateLastPublished;
	}

	public void setDateLastPublished(ZonedDateTime dateLastPublished) {
		this.dateLastPublished = dateLastPublished;
	}

	public Person getPublisher() {
		return publisher;
	}

	public void setPublisher(Person publisher) {
		this.publisher = publisher;
	}
}
