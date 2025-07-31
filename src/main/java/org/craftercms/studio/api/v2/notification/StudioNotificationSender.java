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
package org.craftercms.studio.api.v2.notification;

import org.craftercms.commons.notification.NotificationException;

import java.util.Map;

/**
 * Interface for sending notifications in Crafter Studio.
 * This interface defines a method to send notification messages with a payload and a model.
 * Implementations of this interface can be used to send notifications through various channels.
 */
public interface StudioNotificationSender {
	/**
	 * Sends a notification message provided payload and model.
	 *
	 * @param payload the payload to pass to the notification senders
	 * @param model   the model to use for the notification template
	 * @throws NotificationException if there is an error sending the notification
	 */
	void sendMessage(Object payload, Map<String, Object> model) throws NotificationException;
}
