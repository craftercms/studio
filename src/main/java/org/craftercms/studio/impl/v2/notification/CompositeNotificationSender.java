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
package org.craftercms.studio.impl.v2.notification;

import org.craftercms.commons.notification.NotificationException;
import org.craftercms.commons.notification.NotificationSender;
import org.craftercms.studio.api.v2.notification.StudioNotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * {@link StudioNotificationSender} implementation that aggregates multiple NotificationSender instances,
 * allowing to send a notification message through multiple channels.
 * Each NotificationSender can have its own preconfigured template and enabled flag.
 */
public class CompositeNotificationSender implements StudioNotificationSender {

	private static final Logger logger = LoggerFactory.getLogger(CompositeNotificationSender.class);

	protected Collection<SenderEntry> senders;

	public CompositeNotificationSender(Collection<SenderEntry> senders) {
		this.senders = senders;
	}

	@Override
	public void sendMessage(Object payload, Map<String, Object> model) throws NotificationException {
		Exception exception = null;
		for (SenderEntry senderDescriptor : senders) {
			if (!senderDescriptor.enabled) {
				logger.debug("Notification sender {} is disabled, skipping", senderDescriptor.sender.getClass().getSimpleName());
				continue;
			}
			NotificationSender<?> notificationSender = senderDescriptor.sender;
			String template = senderDescriptor.template;
			try {
				notificationSender.sendMessage(template, payload, model);
			} catch (Exception e) {
				logger.error("Failed to send notification with {}: {}", notificationSender.getClass().getSimpleName(), e.getMessage(), e);
				if (exception == null) {
					exception = e;
				} else {
					exception.addSuppressed(e);
				}
			}
		}
		if (exception != null) {
			throw new NotificationException("Failed to send notifications", exception);
		}
	}

	/**
	 * Represents an entry in the composite notification sender, which includes:
	 * <p>
	 * <ul>
	 * <li>NotificationSender</li>
	 * <li>The template to use for sending messages.</li>
	 * <li>Enabled flag</li>
	 * </ul>
	 */
	public static class SenderEntry {
		private final NotificationSender<?> sender;
		private final String template;
		private final boolean enabled;

		public SenderEntry(NotificationSender<?> sender, String template, boolean enabled) {
			this.sender = sender;
			this.template = template;
			this.enabled = enabled;
		}
	}
}
