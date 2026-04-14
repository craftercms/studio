/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.exception.publish;

/**
 * Exception thrown when a publish operation is attempted with an invalid publishing target.
 */
public class InvalidTargetException extends PublishException {
	private final String[] validTargets;

	public InvalidTargetException(String message, String... validTargets) {
		super(message);
		this.validTargets = validTargets;
	}

	public String[] getValidTargets() {
		return validTargets;
	}
}
